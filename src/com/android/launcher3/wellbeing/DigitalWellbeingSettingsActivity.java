/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Digital Wellbeing settings adaptation for Rocket Launcher.
 * Based on Dragon Launcher Digital Wellbeing; modifications are noted here.
 * Modified for Rocket Launcher during 2026-10-19–2026-10-20.
 * Licensed under GPLv3 as part of this derivative feature; see THIRD_PARTY_LICENSES/DRAGON_LAUNCHER.md.
 */
package com.android.launcher3.wellbeing;

import com.android.launcher3.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DigitalWellbeingSettingsActivity extends Activity {
    private static final int BG = Color.rgb(13, 19, 27);
    private static final int SURFACE = Color.rgb(25, 34, 43);
    private static final int SURFACE_ALT = Color.rgb(31, 43, 51);
    private static final int TEXT = Color.rgb(239, 246, 244);
    private static final int MUTED = Color.rgb(159, 177, 178);
    private static final int ACCENT = Color.rgb(139, 211, 177);
    private static final int ACCENT_DARK = Color.rgb(37, 91, 73);

    private android.content.SharedPreferences prefs;
    private Switch enabled;
    private Switch guiltMode;
    private Switch overlayReminders;
    private TextView durationLabel;
    private SeekBar duration;
    private TextView limitLabel;
    private SeekBar dailyLimit;
    private TextView reminderLabel;
    private SeekBar reminderInterval;
    private LinearLayout appList;
    private final Set<String> selectedApps = new HashSet<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = DigitalWellbeing.preferences(this);
        Set<String> stored = prefs.getStringSet(
                DigitalWellbeing.KEY_PAUSED_APPS, Collections.emptySet());
        if (stored != null) selectedApps.addAll(stored);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(14), dp(20), dp(28));
        scroll.addView(root);

        TextView eyebrow = text("ROCKET  /  WELLBEING", 11, ACCENT, true);
        eyebrow.setLetterSpacing(0.14f);
        root.addView(eyebrow, margins(0, 0, 0, 9));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(22), dp(22), dp(20), dp(20));
        hero.setBackground(gradient(new int[] {
                Color.rgb(39, 83, 70), Color.rgb(26, 51, 57)
        }, dp(24)));
        root.addView(hero, margins(0, 0, 0, 24));

        TextView title = text(getString(R.string.dw_settings_title), 29, TEXT, true);
        hero.addView(title);
        TextView summary = text(getString(R.string.digital_wellbeing_summary), 14,
                Color.rgb(202, 222, 215), false);
        summary.setLineSpacing(dp(3), 1f);
        hero.addView(summary, margins(0, 7, 0, 16));

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.HORIZONTAL);
        status.setGravity(Gravity.CENTER_VERTICAL);
        status.setPadding(dp(11), dp(8), dp(12), dp(8));
        status.setBackground(rounded(Color.rgb(48, 105, 82), dp(30)));
        TextView statusDot = text("●", 10, ACCENT, true);
        status.addView(statusDot, wrapMargins(0, 0, 7, 0));
        TextView statusText = text("A more intentional digital routine", 12,
                Color.rgb(224, 241, 232), true);
        status.addView(statusText);
        hero.addView(status, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        addSectionLabel(root, "PAUSE EXPERIENCE", "01");
        LinearLayout pauseCard = card(root);
        enabled = addSwitchRow(pauseCard, R.string.dw_enable_pause,
                "Pause selected apps before opening them.",
                prefs.getBoolean(DigitalWellbeing.KEY_ENABLED, false), null);
        addDivider(pauseCard);
        guiltMode = addSwitchRow(pauseCard, R.string.dw_guilt_mode,
                "Show your usage reflection during the pause.",
                prefs.getBoolean(DigitalWellbeing.KEY_GUILT_MODE, false), null);
        addDivider(pauseCard);

        durationLabel = text("", 14, TEXT, true);
        pauseCard.addView(durationLabel, margins(0, 13, 0, 2));
        duration = new SeekBar(this);
        duration.setMax(57);
        duration.setProgress(Math.max(3, Math.min(60,
                prefs.getInt(DigitalWellbeing.KEY_PAUSE_SECONDS, 10))) - 3);
        styleSeekBar(duration);
        duration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                updateDurationLabel(value + 3);
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });
        pauseCard.addView(duration, margins(-dp(5), 0, -dp(5), 0));
        updateDurationLabel(duration.getProgress() + 3);

        limitLabel = text("", 14, TEXT, true);
        pauseCard.addView(limitLabel, margins(0, 13, 0, 2));
        dailyLimit = new SeekBar(this);
        dailyLimit.setMax(240);
        dailyLimit.setProgress(Math.max(0, Math.min(240,
                prefs.getInt(DigitalWellbeing.KEY_DAILY_LIMIT_MINUTES, 0))));
        styleSeekBar(dailyLimit);
        dailyLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                updateDailyLimitLabel(value);
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });
        pauseCard.addView(dailyLimit, margins(-dp(5), 0, -dp(5), 0));
        updateDailyLimitLabel(dailyLimit.getProgress());

        addSectionLabel(root, "REMINDERS", "02");
        LinearLayout reminderCard = card(root);
        reminderLabel = text("", 14, TEXT, true);
        reminderCard.addView(reminderLabel, margins(0, 3, 0, 2));
        reminderInterval = new SeekBar(this);
        reminderInterval.setMax(8);
        int savedReminder = prefs.getInt(DigitalWellbeing.KEY_REMINDER_INTERVAL_MINUTES, 0);
        reminderInterval.setProgress(savedReminder <= 0 ? 0
                : Math.max(1, Math.min(8, savedReminder / 15)));
        styleSeekBar(reminderInterval);
        reminderInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                updateReminderLabel(value);
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });
        reminderCard.addView(reminderInterval, margins(-dp(5), 0, -dp(5), 7));
        updateReminderLabel(reminderInterval.getProgress());
        addDivider(reminderCard);
        overlayReminders = addSwitchRow(reminderCard, R.string.dw_overlay_reminders,
                "Show reminders as a floating overlay instead of a standard notification.",
                prefs.getBoolean(DigitalWellbeing.KEY_OVERLAY_REMINDERS, false), null);

        addSectionLabel(root, "APPS & PERMISSIONS", "03");
        LinearLayout appsCard = card(root);
        TextView appsHeading = text(getString(R.string.dw_select_apps), 16, TEXT, true);
        appsCard.addView(appsHeading, margins(0, 0, 0, 4));
        TextView appsHint = text("Choose which apps should include a mindful pause.",
                12, MUTED, false);
        appsCard.addView(appsHint, margins(0, 0, 0, 10));
        appList = new LinearLayout(this);
        appList.setOrientation(LinearLayout.VERTICAL);
        appsCard.addView(appList);
        populateApps();

        LinearLayout permissionCard = card(root);
        addActionRow(permissionCard, "Usage access",
                getString(R.string.dw_usage_permission), v -> openUsagePermission());
        addDivider(permissionCard);
        addActionRow(permissionCard, "Floating overlay",
                getString(R.string.dw_overlay_permission), v -> openOverlayPermission());

        addSectionLabel(root, "ABOUT", "04");
        LinearLayout aboutCard = card(root);
        addActionRow(aboutCard, getString(R.string.dw_about_license),
                "View third-party license information", v ->
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dw_about_license)
                        .setMessage(R.string.dw_license_notice)
                        .setPositiveButton(android.R.string.ok, null)
                        .show());

        TextView save = text(getString(R.string.dw_save), 15, Color.rgb(17, 38, 31), true);
        save.setGravity(Gravity.CENTER);
        save.setPadding(dp(18), dp(15), dp(18), dp(15));
        save.setBackground(gradient(new int[] {
                Color.rgb(160, 226, 190), Color.rgb(119, 199, 162)
        }, dp(18)));
        save.setElevation(dp(3));
        save.setOnClickListener(v -> saveAndClose());
        root.addView(save, margins(0, 22, 0, 0));

        setContentView(scroll);
    }

    private void openUsagePermission() {
        try {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,
                    Uri.parse("package:" + getPackageName())));
        } catch (RuntimeException e) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    private void openOverlayPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            try { startActivity(overlayIntent); }
            catch (RuntimeException e) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            }
        } else {
            Toast.makeText(this, R.string.dw_overlay_not_supported, Toast.LENGTH_SHORT).show();
        }
    }

    private void addSectionLabel(LinearLayout root, String label, String number) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView name = text(label, 11, ACCENT, true);
        name.setLetterSpacing(0.12f);
        row.addView(name);
        TextView index = text(number, 10, MUTED, true);
        index.setGravity(Gravity.END);
        row.addView(index, new LinearLayout.LayoutParams(0, -2, 1f));
        root.addView(row, margins(2, 0, 2, 10));
    }

    private LinearLayout card(LinearLayout parent) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(17), dp(14), dp(17), dp(15));
        panel.setBackground(rounded(SURFACE, dp(22)));
        parent.addView(panel, margins(0, 0, 0, 23));
        return panel;
    }

    private Switch addSwitchRow(LinearLayout parent, int titleRes, String subtitle,
            boolean checked, CompoundButton.OnCheckedChangeListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        TextView title = text(getString(titleRes), 14, TEXT, true);
        copy.addView(title);
        TextView detail = text(subtitle, 12, MUTED, false);
        detail.setLineSpacing(dp(2), 1f);
        copy.addView(detail, wrapMargins(0, 3, 5, 0));
        row.addView(copy, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Switch toggle = new Switch(this);
        toggle.setChecked(checked);
        toggle.setShowText(false);
        toggle.setThumbTintList(new ColorStateList(
                new int[][] { new int[] { android.R.attr.state_checked }, new int[] {} },
                new int[] { ACCENT, Color.rgb(190, 201, 201) }));
        toggle.setTrackTintList(new ColorStateList(
                new int[][] { new int[] { android.R.attr.state_checked }, new int[] {} },
                new int[] { ACCENT_DARK, Color.rgb(69, 81, 86) }));
        if (listener != null) toggle.setOnCheckedChangeListener(listener);
        row.addView(toggle, wrapMargins(10, 0, 0, 0));
        parent.addView(row, margins(0, 4, 0, 4));
        return toggle;
    }

    private void addActionRow(LinearLayout parent, String title, String subtitle,
            View.OnClickListener action) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        TextView copy = text(title, 14, TEXT, true);
        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.addView(copy);
        details.addView(text(subtitle, 12, MUTED, false), wrapMargins(0, 3, 0, 0));
        row.addView(details, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView arrow = text("›", 27, ACCENT, false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(28), -2));
        row.setOnClickListener(action);
        row.setBackground(selectableBackground());
        parent.addView(row);
    }

    private void addDivider(LinearLayout parent) {
        View line = new View(this);
        line.setBackgroundColor(Color.rgb(48, 62, 68));
        parent.addView(line, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
    }

    private void populateApps() {
        List<ApplicationInfo> apps = new ArrayList<>();
        PackageManager pm = getPackageManager();
        for (ApplicationInfo info : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (getPackageName().equals(info.packageName)) continue;
            if (pm.getLaunchIntentForPackage(info.packageName) == null) continue;
            apps.add(info);
        }
        Collections.sort(apps, new Comparator<ApplicationInfo>() {
            @Override public int compare(ApplicationInfo a, ApplicationInfo b) {
                return pmLabel(a).compareToIgnoreCase(pmLabel(b));
            }
            private String pmLabel(ApplicationInfo info) {
                return pm.getApplicationLabel(info).toString();
            }
        });

        for (ApplicationInfo info : apps) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(7), 0, dp(7));

            ImageView icon = new ImageView(this);
            try { icon.setImageDrawable(pm.getApplicationIcon(info)); }
            catch (PackageManager.NameNotFoundException ignored) {}
            row.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(36)));

            TextView label = text(pm.getApplicationLabel(info).toString(), 13, TEXT, false);
            label.setMaxLines(1);
            label.setEllipsize(android.text.TextUtils.TruncateAt.END);
            row.addView(label, new LinearLayout.LayoutParams(0, -2, 1f));
            CheckBoxCompat check = new CheckBoxCompat(this);
            check.setChecked(selectedApps.contains(info.packageName));
            check.setOnCheckedChangeListener((button, checked) -> {
                if (checked) selectedApps.add(info.packageName);
                else selectedApps.remove(info.packageName);
            });
            row.addView(check);
            row.setOnClickListener(v -> check.setChecked(!check.isChecked()));
            appList.addView(row);
        }
        if (apps.isEmpty()) {
            appList.addView(text("No launchable apps found.", 12, MUTED, false));
        }
    }

    private static class CheckBoxCompat extends android.widget.CheckBox {
        CheckBoxCompat(Activity activity) {
            super(activity);
            setButtonTintList(ColorStateList.valueOf(ACCENT));
            setPadding(0, 0, 0, 0);
        }
    }

    private void styleSeekBar(SeekBar bar) {
        bar.setProgressTintList(ColorStateList.valueOf(ACCENT));
        bar.setThumbTintList(ColorStateList.valueOf(ACCENT));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(66, 81, 84)));
    }

    private void updateDurationLabel(int seconds) {
        durationLabel.setText(getString(R.string.dw_pause_duration, seconds));
    }

    private void updateDailyLimitLabel(int minutes) {
        limitLabel.setText(minutes == 0 ? getString(R.string.dw_daily_limit_off)
                : getString(R.string.dw_daily_limit, minutes));
    }

    private void updateReminderLabel(int step) {
        int minutes = step * 15;
        reminderLabel.setText(minutes == 0 ? getString(R.string.dw_reminders_off)
                : getString(R.string.dw_reminder_interval, minutes));
    }

    private void saveAndClose() {
        if (enabled.isChecked() && selectedApps.isEmpty()) {
            Toast.makeText(this, R.string.dw_no_apps_selected, Toast.LENGTH_LONG).show();
            return;
        }
        prefs.edit()
                .putBoolean(DigitalWellbeing.KEY_ENABLED, enabled.isChecked())
                .putBoolean(DigitalWellbeing.KEY_GUILT_MODE, guiltMode.isChecked())
                .putInt(DigitalWellbeing.KEY_PAUSE_SECONDS, duration.getProgress() + 3)
                .putInt(DigitalWellbeing.KEY_DAILY_LIMIT_MINUTES, dailyLimit.getProgress())
                .putInt(DigitalWellbeing.KEY_REMINDER_INTERVAL_MINUTES,
                        reminderInterval.getProgress() * 15)
                .putBoolean(DigitalWellbeing.KEY_OVERLAY_REMINDERS, overlayReminders.isChecked())
                .putStringSet(DigitalWellbeing.KEY_PAUSED_APPS, new HashSet<>(selectedApps))
                .apply();
        DigitalWellbeingReminderReceiver.schedule(this, reminderInterval.getProgress() * 15);
        finish();
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private GradientDrawable gradient(int[] colors, int radius) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, colors);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private android.graphics.drawable.Drawable selectableBackground() {
        android.util.TypedValue value = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
        return getDrawable(value.resourceId);
    }

    private LinearLayout.LayoutParams margins(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);
        return params;
    }

    private LinearLayout.LayoutParams wrapMargins(int left, int top, int right, int bottom) {\n        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(\n                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);\n        params.setMargins(left, top, right, bottom);\n        return params;\n    }\n\n    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
