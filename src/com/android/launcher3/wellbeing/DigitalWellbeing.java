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
import android.text.TextUtils;

import java.util.Set;

public final class DigitalWellbeing {
    static final String PREFS = "rocket_digital_wellbeing";
    static final String KEY_ENABLED = "pause_enabled";
    static final String KEY_PAUSED_APPS = "paused_apps";
    static final String KEY_PAUSE_SECONDS = "pause_seconds";
    static final String KEY_GUILT_MODE = "guilt_mode";
    static final String EXTRA_ORIGINAL_INTENT = "digital_wellbeing_original_intent";
    static final String EXTRA_TARGET_PACKAGE = "digital_wellbeing_target_package";

    private DigitalWellbeing() {}

    public static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
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

        Intent pause = new Intent(context, DigitalPauseActivity.class);
        pause.putExtra(EXTRA_ORIGINAL_INTENT, new Intent(intent));
        pause.putExtra(EXTRA_TARGET_PACKAGE, targetPackage);
        pause.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(pause);
        return true;
    }
}
