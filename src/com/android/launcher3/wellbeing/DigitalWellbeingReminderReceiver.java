/*
 * Digital Wellbeing reminder adaptation for Rocket Launcher.
 * See THIRD_PARTY_LICENSES/DRAGON_LAUNCHER.md and COPYING.
 * Modified derivative; not endorsed by Dragon Launcher authors.
 */
package com.android.launcher3.wellbeing;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class DigitalWellbeingReminderReceiver extends BroadcastReceiver {
    private static final String ACTION_REMINDER = "com.android.launcher3.wellbeing.REMINDER";
    private static final String CHANNEL_ID = "digital_wellbeing_reminders";
    private static final int NOTIFICATION_ID_BASE = 47000;

    public static void schedule(Context context, int intervalMinutes) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pending = PendingIntent.getBroadcast(context, 47001,
                new Intent(context, DigitalWellbeingReminderReceiver.class).setAction(ACTION_REMINDER),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (intervalMinutes <= 0) {
            alarmManager.cancel(pending);
            pending.cancel();
            return;
        }
        long interval = Math.max(15, intervalMinutes) * 60_000L;
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + interval, interval, pending);
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            schedule(context, DigitalWellbeing.preferences(context)
                    .getInt(DigitalWellbeing.KEY_REMINDER_INTERVAL_MINUTES, 0));
            return;
        }
        int interval = DigitalWellbeing.preferences(context)
                .getInt(DigitalWellbeing.KEY_REMINDER_INTERVAL_MINUTES, 0);
        if (interval <= 0 || !DigitalWellbeing.preferences(context)
                .getBoolean(DigitalWellbeing.KEY_ENABLED, false)
                || !DigitalWellbeing.hasUsageAccess(context)) return;

        Set<String> selected = DigitalWellbeing.preferences(context)
                .getStringSet(DigitalWellbeing.KEY_PAUSED_APPS, null);
        if (selected == null || selected.isEmpty()) return;
        UsageStatsManager usage = (UsageStatsManager)
                context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        List<UsageStats> stats;
        try {
            stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    start.getTimeInMillis(), System.currentTimeMillis());
        } catch (RuntimeException ignored) {
            return;
        }
        if (stats == null) return;
        long intervalMillis = interval * 60_000L;
        long now = System.currentTimeMillis();
        for (UsageStats stat : stats) {
            String pkg = stat.getPackageName();
            if (!selected.contains(pkg)) continue;
            long used = stat.getTotalTimeInForeground();
            if (used < intervalMillis) continue;
            long lastAt = DigitalWellbeing.preferences(context)
                    .getLong("reminder_last_wall_" + pkg, 0L);
            if (lastAt > 0 && now - lastAt < intervalMillis) continue;
            showReminder(context, pkg);
            DigitalWellbeing.preferences(context).edit()
                    .putLong("reminder_last_wall_" + pkg, now).apply();
        }
    }

    private void showReminder(Context context, String packageName) {
        String label = packageName;
        try {
            label = context.getPackageManager().getApplicationLabel(
                    context.getPackageManager().getApplicationInfo(packageName, 0)).toString();
        } catch (PackageManager.NameNotFoundException ignored) {}
        if (DigitalWellbeing.preferences(context).getBoolean(
                DigitalWellbeing.KEY_OVERLAY_REMINDERS, false)
                && Build.VERSION.SDK_INT >= 23
                && android.provider.Settings.canDrawOverlays(context)) {
            DigitalWellbeingOverlayService.show(context, label);
            return;
        }
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                    context.getString(R.string.dw_reminder_channel),
                    NotificationManager.IMPORTANCE_DEFAULT));
        }
        Intent openSettings = new Intent(context, DigitalWellbeingSettingsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, packageName.hashCode(),
                openSettings, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        Notification notification = builder
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(context.getString(R.string.dw_reminder_title))
                .setContentText(context.getString(R.string.dw_reminder_message, label))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();
        manager.notify(NOTIFICATION_ID_BASE + (packageName.hashCode() & 0x7fff), notification);
    }
}