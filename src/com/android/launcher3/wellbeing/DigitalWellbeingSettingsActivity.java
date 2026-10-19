/*
 * Digital Wellbeing settings adaptation for Rocket Launcher.
 * Based on Dragon Launcher Digital Wellbeing; modifications are noted here.
 * Licensed under GPLv3 as part of this derivative feature; see THIRD_PARTY_LICENSES/DRAGON_LAUNCHER.md.
 */
package com.android.launcher3.wellbeing;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DigitalWellbeingSettingsActivity extends Activity {
    private android.content.SharedPreferences prefs;
    private CheckBox enabled;
    private CheckBox guiltMode;
    private TextView durationLabel;
    private SeekBar duration;
    private LinearLayout appList;
    private final Set<String> selectedApps = new HashSet<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = DigitalWellbeing.preferences(this);
        Set<String> stored = prefs.getStringSet(DigitalWellbeing.KEY_PAUSED_APPS, Collections.emptySet());
        if (stored != null) selectedApps.addAll(stored);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(20));
        root.setBackgroundColor(Color.rgb(18, 24, 35));
        scroll.addView(root);

        TextView heading = text(getString(R.string.dw_settings_title), 26, Color.WHITE, true);
        root.addView(heading);
        TextView intro = text(getString(R.string.digital_wellbeing_summary), 14, Color.LTGRAY, false);
        intro.setPadding(0, dp(8), 0, dp(16));
        root.addView(intro);

        enabled = new CheckBox(this);
        enabled.setText(R.string.dw_enable_pause);
        enabled.setTextColor(Color.WHITE);
        enabled.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0, 206, 201)));
        enabled.setChecked(prefs.getBoolean(DigitalWellbeing.KEY_ENABLED, false));
        root.addView(enabled);

        guiltMode = new CheckBox(this);
        guiltMode.setText(R.string.dw_guilt_mode);
        guiltMode.setTextColor(Color.WHITE);
        guiltMode.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0, 206, 201)));
        guiltMode.setChecked(prefs.getBoolean(DigitalWellbeing.KEY_GUILT_MODE, false));
        root.addView(guiltMode);

        durationLabel = text("", 15, Color.WHITE, true);
        durationLabel.setPadding(0, dp(18), 0, dp(4));
        root.addView(durationLabel);
        duration = new SeekBar(this);
        duration.setMax(57);
        duration.setProgress(Math.max(3, Math.min(60,
                prefs.getInt(DigitalWellbeing.KEY_PAUSE_SECONDS, 10))) - 3);
        duration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int value, boolean fromUser) { updateDurationLabel(value + 3); }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });
        root.addView(duration);
        updateDurationLabel(duration.getProgress() + 3);

        TextView appTitle = text(getString(R.string.dw_select_apps), 18, Color.WHITE, true);
        appTitle.setPadding(0, dp(22), 0, dp(8));
        root.addView(appTitle);
        appList = new LinearLayout(this);
        appList.setOrientation(LinearLayout.VERTICAL);
        root.addView(appList);
        populateApps();

        Button save = new Button(this);
        save.setText(R.string.dw_save);
        save.setOnClickListener(v -> saveAndClose());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = dp(18);
        root.addView(save, buttonParams);
        setContentView(scroll);
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
            CheckBox box = new CheckBox(this);
            box.setText(pm.getApplicationLabel(info));
            box.setTextColor(Color.WHITE);
            box.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0, 206, 201)));
            box.setChecked(selectedApps.contains(info.packageName));
            box.setOnCheckedChangeListener((button, checked) -> {
                if (checked) selectedApps.add(info.packageName);
                else selectedApps.remove(info.packageName);
            });
            appList.addView(box);
        }
    }

    private void updateDurationLabel(int seconds) {
        durationLabel.setText(getString(R.string.dw_pause_duration, seconds));
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
                .putStringSet(DigitalWellbeing.KEY_PAUSED_APPS, new HashSet<>(selectedApps))
                .apply();
        finish();
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.START);
        if (bold) view.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
