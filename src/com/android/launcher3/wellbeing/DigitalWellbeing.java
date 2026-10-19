/*
 * Digital Wellbeing launch-gate adaptation for Rocket Launcher.
 * Based on the Digital Wellbeing feature in Dragon Launcher.
 * Copyright notices and GPLv3 terms: see THIRD_PARTY_LICENSES/DRAGON_LAUNCHER.md and COPYING.
 * This file is a modified adaptation; it is not endorsed by the Dragon Launcher authors.
 */
package com.android.launcher3.wellbeing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.os.Process;

import java.util.Calendar;
import java.util.List;
import android.text.TextUtils;

import java.util.Set;

public final class DigitalWellbeing {
    static final String PREFS = "rocket_digital_wellbeing";
    static final String KEY_ENABLED = "pause_enabled";
    static final String KEY_PAUSED_APPS = "paused_apps";
    static final String KEY_PAUSE_SECONDS = "pause_seconds";
    static final String KEY_GUILT_MODE = "guilt_mode";
    static final String KEY_DAILY_LIMIT_MINUTES = "daily_limit_minutes";
    static final String KEY_REMINDER_INTERVAL_MINUTES = "reminder_interval_minutes";
    static final String EXTRA_LIMIT_REACHED = "digital_wellbeing_limit_reached";
    static final String EXTRA_ORIGINAL_INTENT = "digital_wellbeing_original_intent";
    static final String EXTRA_TARGET_PACKAGE = "digital_wellbeing_target_package";

    private DigitalWellbeing() {}

    public static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Returns true only when usage access is granted and the configured daily cap is reached. */
    public static boolean isDailyLimitReached(Context context, String packageName) {
        SharedPreferences prefs = preferences(context);
        int limitMinutes = prefs.getInt(KEY_DAILY_LIMIT_MINUTES, 0);
        if (limitMinutes <= 0 || !hasUsageAccess(context)) return false;
        try {
            UsageStatsManager manager = (UsageStatsManager)
                    context.getSystemService(Context.USAGE_STATS_SERVICE);
            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            List<UsageStats> stats = manager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, start.getTimeInMillis(),
                    System.currentTimeMillis());
            long usedMillis = 0;
            if (stats != null) {
                for (UsageStats stat : stats) {
                    if (packageName.equals(stat.getPackageName())) {
                        usedMillis += stat.getTotalTimeInForeground();
                    }
                }
            }
            return usedMillis >= limitMinutes * 60_000L;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static boolean hasUsageAccess(Context context) {
        try {
            AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(), context.getPackageName()) == AppOpsManager.MODE_ALLOWED;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static boolean tryInterceptLaunch(Context context, Intent intent) {
        if (intent == null || intent.getComponent() == null) return false;
        String targetPackage = intent.getComponent().getPackageName();
        if (TextUtils.isEmpty(targetPackage) || targetPackage.equals(context.getPackageName())) {
            return false;
        }
        SharedPreferences prefs = preferences(context);
        if (!prefs.getBoolean(KEY_ENABLED, false)) return false;
        Set<String> pausedApps = prefs.getStringSet(KEY_PAUSED_APPS, null);
        if (pausedApps == null || !pausedApps.contains(targetPackage)) return false;

        boolean limitReached = isDailyLimitReached(context, targetPackage);
        Intent pause = new Intent(context, DigitalPauseActivity.class);
        pause.putExtra(EXTRA_ORIGINAL_INTENT, new Intent(intent));
        pause.putExtra(EXTRA_TARGET_PACKAGE, targetPackage);
        pause.putExtra(EXTRA_LIMIT_REACHED, limitReached);
        pause.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(pause);
        return true;
    }
}
