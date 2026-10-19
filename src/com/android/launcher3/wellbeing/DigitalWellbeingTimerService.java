/*
 * Session enforcement service for Rocket Launcher's Digital Wellbeing adaptation.
 * Derived feature notices and GPLv3 terms: COPYING and THIRD_PARTY_LICENSES/DRAGON_LAUNCHER.md.
 */
package com.android.launcher3.wellbeing;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.Calendar;
import java.util.List;

/**
 * Monitors a selected app while it is in the foreground and returns to Home when its daily cap
 * is consumed. Usage Access is required. The launcher-side launch gate remains the fallback.
 */
public class DigitalWellbeingTimerService extends Service {
    private static final String CHANNEL_ID = "rocket_digital_wellbeing_timer";
    private static final int NOTIFICATION_ID = 47010;
    private static final String EXTRA_PACKAGE = "tracked_package";
    private static final long CHECK_INTERVAL_MS = 2000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String trackedPackage;
    private long startedAt;
    private long usageBeforeSession;
    private long lastForegroundCheck;
    private int awayChecks;
    private boolean limitHandled;

    private final Runnable monitor = new Runnable() {
        @Override public void run() {
            if (trackedPackage == null || limitHandled) {
                stopSelf();
                return;
            }
            long now = System.currentTimeMillis();
            if (isTrackedAppForeground(now)) {
                awayChecks = 0;
                long sessionUsage = Math.max(0L, now - startedAt);
                int limitMinutes = DigitalWellbeing.preferences(
                        DigitalWellbeingTimerService.this).getInt(
                        DigitalWellbeing.KEY_DAILY_LIMIT_MINUTES, 0);
                if (limitMinutes > 0
                        && usageBeforeSession + sessionUsage >= limitMinutes * 60_000L) {
                    limitHandled = true;
                    returnHome();
                    return;
                }
            } else if (++awayChecks >= 6) {
                // The user left the tracked app for about 12 seconds; stop monitoring.
                stopSelf();
                return;
            }
            handler.postDelayed(this, CHECK_INTERVAL_MS);
        }
    };

    public static void startIfNeeded(Context context, String packageName) {
        int limit = DigitalWellbeing.preferences(context).getInt(
                DigitalWellbeing.KEY_DAILY_LIMIT_MINUTES, 0);
        if (limit <= 0 || !DigitalWellbeing.hasUsageAccess(context)) return;
        Intent intent = new Intent(context, DigitalWellbeingTimerService.class);
        intent.putExtra(EXTRA_PACKAGE, packageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                    "Digital Wellbeing session limit", NotificationManager.IMPORTANCE_LOW));
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        trackedPackage = intent == null ? null : intent.getStringExtra(EXTRA_PACKAGE);
        if (trackedPackage == null || trackedPackage.equals(getPackageName())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        Notification notification = builder
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Digital Wellbeing is active")
                .setContentText("Monitoring the selected app's daily limit")
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
        startedAt = System.currentTimeMillis();
        usageBeforeSession = getTodayUsageMillis(trackedPackage);
        awayChecks = 0;
        limitHandled = false;
        handler.removeCallbacks(monitor);
        handler.postDelayed(monitor, CHECK_INTERVAL_MS);
        return START_NOT_STICKY;
    }

    private long getTodayUsageMillis(String packageName) {
        try {
            UsageStatsManager manager = (UsageStatsManager)
                    getSystemService(Context.USAGE_STATS_SERVICE);
            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            List<UsageStats> stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    start.getTimeInMillis(), System.currentTimeMillis());
            long total = 0L;
            if (stats != null) for (UsageStats stat : stats) {
                if (packageName.equals(stat.getPackageName())) {
                    total = Math.max(total, stat.getTotalTimeInForeground());
                }
            }
            return total;
        } catch (RuntimeException ignored) {
            return 0L;
        }
    }

    private boolean isTrackedAppForeground(long now) {
        try {
            UsageStatsManager manager = (UsageStatsManager)
                    getSystemService(Context.USAGE_STATS_SERVICE);
            UsageEvents events = manager.queryEvents(startedAt, now);
            UsageEvents.Event event = new UsageEvents.Event();
            String lastForeground = null;
            while (events != null && events.hasNextEvent()) {
                events.getNextEvent(event);
                int type = event.getEventType();
                if (type == UsageEvents.Event.MOVE_TO_FOREGROUND
                        || (Build.VERSION.SDK_INT >= 29
                        && type == UsageEvents.Event.ACTIVITY_RESUMED)) {
                    lastForeground = event.getPackageName();
                } else if (type == UsageEvents.Event.MOVE_TO_BACKGROUND
                        || (Build.VERSION.SDK_INT >= 29
                        && type == UsageEvents.Event.ACTIVITY_PAUSED)) {
                    if (event.getPackageName().equals(lastForeground)) {
                        lastForeground = null;
                    }
                }
            }
            lastForegroundCheck = now;
            return trackedPackage.equals(lastForeground);
        } catch (RuntimeException ignored) {
            // Fail closed for enforcement: if Usage Access temporarily cannot read events,
            // the launch gate still checks the accumulated daily usage on the next launch.
            return false;
        }
    }

    private void returnHome() {
        try {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.cancel(NOTIFICATION_ID);
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(home);
        } finally {
            stopSelf();
        }
    }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
