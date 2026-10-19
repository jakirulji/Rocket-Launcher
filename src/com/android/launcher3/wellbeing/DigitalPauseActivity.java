/*
 * Digital pause screen adapted from Dragon Launcher Digital Wellbeing.
 * This file is modified for Rocket Launcher; see THIRD_PARTY_LICENSES/DRAGON_LAUNCHER.md.
 * Licensed under GPLv3 as part of this derivative feature.
 */
package com.android.launcher3.wellbeing;

import android.app.Activity;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DigitalPauseActivity extends Activity {
    private Intent originalIntent;
    private String targetPackage;
    private boolean dailyLimitReached;
    private int remainingSeconds;
    private TextView countdownView;
    private TextView usageView;
    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            if (isFinishing()) return;
            remainingSeconds--;
            if (remainingSeconds <= 0) {
                remainingSeconds = 0;
                countdownView.setText("You're ready");
                openButton.setEnabled(true);
                return;
            }
            countdownView.setText(String.format(Locale.getDefault(), "%d", remainingSeconds));
            handler.postDelayed(this, 1000);
        }
    };
    private Button openButton;
    private TextView lotusView;
    private ObjectAnimator breathingAnimation;
    private ObjectAnimator breathingYAnimation;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        originalIntent = getIntent().getParcelableExtra(DigitalWellbeing.EXTRA_ORIGINAL_INTENT);
        targetPackage = getIntent().getStringExtra(DigitalWellbeing.EXTRA_TARGET_PACKAGE);
        dailyLimitReached = getIntent().getBooleanExtra(DigitalWellbeing.EXTRA_LIMIT_REACHED, false);
        if (originalIntent == null) { finish(); return; }
        remainingSeconds = DigitalWellbeing.preferences(this)
                .getInt(DigitalWellbeing.KEY_PAUSE_SECONDS, 10);
        remainingSeconds = Math.max(3, Math.min(60, remainingSeconds));
        buildScreen();
        if (!dailyLimitReached) handler.postDelayed(ticker, 1000);
    }

    @Override protected void onResume() {
        super.onResume();
        // Refresh usage-access status after the user returns from Android Settings.
        if (usageView != null) updateUsage();
    }

    private void buildScreen() {
        int bgTop = Color.rgb(15, 32, 39);
        int bgBottom = Color.rgb(32, 58, 67);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(26), dp(28), dp(26), dp(28));
        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{bgTop, bgBottom});
        root.setBackground(background);

        TextView appName = new TextView(this);
        appName.setText(getAppLabel(targetPackage).toUpperCase(Locale.getDefault()));
        appName.setTextColor(Color.rgb(0, 206, 201));
        appName.setTextSize(13);
        appName.setLetterSpacing(0.16f);
        appName.setGravity(Gravity.CENTER);
        root.addView(appName, matchWrap());

        lotusView = new TextView(this);
        lotusView.setText("✿");
        lotusView.setContentDescription(getString(R.string.dw_breathing_visual));
        lotusView.setTextColor(Color.rgb(108, 92, 231));
        lotusView.setTextSize(104);
        lotusView.setGravity(Gravity.CENTER);
        lotusView.setPadding(0, dp(18), 0, dp(10));
        root.addView(lotusView, matchWrap());
        // A gentle breathing pulse makes the pause screen feel calm without blocking input.
        breathingAnimation = ObjectAnimator.ofFloat(lotusView, View.SCALE_X, 1f, 1.10f, 1f);
        breathingAnimation.setDuration(4200);
        breathingAnimation.setRepeatCount(ValueAnimator.INFINITE);
        breathingAnimation.setRepeatMode(ValueAnimator.RESTART);
        breathingAnimation.start();
        breathingYAnimation = ObjectAnimator.ofFloat(lotusView, View.SCALE_Y, 1f, 1.10f, 1f);
        breathingYAnimation.setDuration(4200);
        breathingYAnimation.setRepeatCount(ValueAnimator.INFINITE);
        breathingYAnimation.setRepeatMode(ValueAnimator.RESTART);
        breathingYAnimation.start();

        TextView title = new TextView(this);
        title.setText(R.string.dw_pause_title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        TextView message = new TextView(this);
        message.setText(dailyLimitReached ? R.string.dw_daily_limit_reached_message
                : R.string.dw_pause_message);
        message.setTextColor(Color.rgb(210, 220, 225));
        message.setTextSize(15);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, dp(12), 0, dp(20));
        root.addView(message, matchWrap());

        countdownView = new TextView(this);
        countdownView.setText(dailyLimitReached ? getString(R.string.dw_limit_reached_short)
                : String.valueOf(remainingSeconds));
        if (dailyLimitReached) countdownView.setTextSize(24);
        countdownView.setTextColor(Color.WHITE);
        countdownView.setTextSize(54);
        countdownView.setGravity(Gravity.CENTER);
        countdownView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        root.addView(countdownView, matchWrap());

        usageView = new TextView(this);
        usageView.setTextColor(Color.rgb(178, 190, 195));
        usageView.setTextSize(13);
        usageView.setGravity(Gravity.CENTER);
        usageView.setPadding(0, dp(12), 0, dp(18));
        root.addView(usageView, matchWrap());

        openButton = new Button(this);
        openButton.setText(dailyLimitReached ? R.string.dw_daily_limit_reached
                : R.string.dw_open_anyway);
        openButton.setEnabled(!dailyLimitReached && remainingSeconds <= 0);
        openButton.setOnClickListener(v -> launchOriginal());
        root.addView(openButton, matchWrap());

        Button backButton = new Button(this);
        backButton.setText(R.string.dw_back_home);
        backButton.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams backParams = matchWrap();
        backParams.topMargin = dp(8);
        root.addView(backButton, backParams);
        setContentView(root);
    }

    private void updateUsage() {
        if (!DigitalWellbeing.preferences(this).getBoolean(DigitalWellbeing.KEY_GUILT_MODE, false)) {
            usageView.setVisibility(View.GONE);
            return;
        }
        if (!hasUsageAccess()) {
            usageView.setText(R.string.dw_usage_permission);
            usageView.setOnClickListener(v -> startActivity(new Intent(
                    Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:" + getPackageName()))));
            return;
        }
        try {
            UsageStatsManager manager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0);
            List<UsageStats> stats = manager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, start.getTimeInMillis(),
                    System.currentTimeMillis());
            long total = 0;
            if (stats != null) for (UsageStats stat : stats) {
                if (targetPackage.equals(stat.getPackageName())) total += stat.getTotalTimeInForeground();
            }
            long mins = total / 60000;
            usageView.setText(getString(R.string.dw_usage_time_today)
                    + (mins < 60 ? mins + " min" : (mins / 60) + "h " + (mins % 60) + "m"));
        } catch (RuntimeException ignored) {
            usageView.setVisibility(View.GONE);
        }
    }

    private boolean hasUsageAccess() {
        try {
            AppOpsManager ops = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            int mode = ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (RuntimeException e) { return false; }
    }

    private String getAppLabel(String packageName) {
        try { return getPackageManager().getApplicationLabel(
                getPackageManager().getApplicationInfo(packageName, 0)).toString(); }
        catch (Exception e) { return packageName == null ? "App" : packageName; }
    }

    private void launchOriginal() {
        if (dailyLimitReached || remainingSeconds > 0) return;
        try {
            // Start enforcement before launching the target so its foreground usage is counted
            // from the beginning of this session.
            DigitalWellbeingTimerService.startIfNeeded(this, targetPackage);
            startActivity(originalIntent);
        }
        catch (RuntimeException e) { android.widget.Toast.makeText(this,
                R.string.dw_no_launch_intent, android.widget.Toast.LENGTH_SHORT).show(); }
        finish();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (breathingAnimation != null) breathingAnimation.cancel();
        if (breathingYAnimation != null) breathingYAnimation.cancel();
        super.onDestroy();
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }
    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
