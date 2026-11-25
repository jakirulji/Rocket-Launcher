/*
 * Copyright 2026, Rocket Launcher contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair.ui.preferences.destinations

import android.content.Intent
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.lawnchair.preferences.PreferenceAdapterImpl
import app.lawnchair.ui.preferences.components.controls.ClickablePreference
import app.lawnchair.ui.preferences.components.controls.SliderPreference
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.layout.ExpandAndShrink
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import app.lawnchair.util.restartLauncher
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.R

@Composable
fun MiscPreferences(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isPhone = context.resources.configuration.smallestScreenWidthDp < 600
    val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        context.getSystemService(WindowManager::class.java)?.isCrossWindowBlurEnabled == true
    val hasSuggestionsSettings = remember(context) {
        val isDpsEnabled = runCatching {
            context.packageManager.getApplicationInfo("com.google.android.as", 0).enabled
        }.getOrDefault(false)
        isDpsEnabled && Intent("android.settings.ACTION_CONTENT_SUGGESTIONS_SETTINGS")
            .resolveActivity(context.packageManager) != null
    }
    val blurDepthAdapter = remember {
        PreferenceAdapterImpl(
            get = { LauncherPrefs.BLUR_DEPTH.get(context) },
            set = { LauncherPrefs.get(context).put(LauncherPrefs.BLUR_DEPTH.to(it)) },
        )
    }
    val fixedLandscapeAdapter = remember {
        PreferenceAdapterImpl(
            get = { LauncherPrefs.FIXED_LANDSCAPE_MODE.get(context) },
            set = { LauncherPrefs.get(context).put(LauncherPrefs.FIXED_LANDSCAPE_MODE.to(it)) },
        )
    }

    PreferenceLayout(
        label = stringResource(R.string.misc_label),
        modifier = modifier,
    ) {
        PreferenceGroup(heading = stringResource(R.string.misc_label)) {
            ExpandAndShrink(visible = blurSupported) {
                SliderPreference(
                    label = stringResource(R.string.background_blur_title),
                    adapter = blurDepthAdapter,
                    valueRange = 0..60,
                    step = 1,
                    showUnit = "dp",
                )
            }
            ExpandAndShrink(visible = isPhone) {
                SwitchPreference(
                    adapter = fixedLandscapeAdapter,
                    label = stringResource(R.string.fixed_landscape_mode_title),
                    description = stringResource(R.string.fixed_landscape_mode_summary),
                )
            }
            ExpandAndShrink(visible = hasSuggestionsSettings) {
                ClickablePreference(
                    label = stringResource(R.string.suggestion_pref_screen_title),
                    subtitle = stringResource(R.string.suggestion_pref_screen_desc),
                    onClick = {
                        val intent = Intent("android.settings.ACTION_CONTENT_SUGGESTIONS_SETTINGS")
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    },
                )
            }
            ClickablePreference(
                label = stringResource(R.string.restart_launcher_label),
                subtitle = stringResource(R.string.restart_launcher_summary),
                confirmationText = stringResource(R.string.restart_launcher_confirmation),
                onClick = { restartLauncher(context) },
            )
        }
    }
}
