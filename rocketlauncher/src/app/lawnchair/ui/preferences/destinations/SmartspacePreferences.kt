/*
 * Copyright 2022, Lawnchair
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

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.lawnchair.preferences.PreferenceAdapter
import app.lawnchair.preferences.getAdapter
import androidx.datastore.preferences.core.stringPreferencesKey
import org.json.JSONArray
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.smartspace.SmartspaceViewContainer
import app.lawnchair.smartspace.model.LawnchairSmartspace
import app.lawnchair.smartspace.model.SmartspaceCalendar
import app.lawnchair.smartspace.model.SmartspaceMode
import app.lawnchair.smartspace.model.SmartspaceTimeFormat
import app.lawnchair.smartspace.model.SoulSpace
import app.lawnchair.smartspace.provider.SmartspaceProvider
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.controls.ClickablePreference
import app.lawnchair.ui.preferences.components.controls.ListPreference
import app.lawnchair.ui.preferences.components.controls.ListPreferenceEntry
import app.lawnchair.ui.preferences.components.controls.MainSwitchPreference
import app.lawnchair.ui.preferences.components.controls.SliderPreference
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.layout.ExpandAndShrink
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import app.lawnchair.ui.theme.isSelectedThemeDark
import app.lawnchair.ui.theme.preferenceGroupColor
import com.android.launcher3.R
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants

@Composable
fun SmartspacePreferences(
    fromWidget: Boolean,
    modifier: Modifier = Modifier,
) {
    val preferenceManager2 = preferenceManager2()
    val smartspaceProvider = SmartspaceProvider.INSTANCE.get(LocalContext.current)
    val smartspaceAdapter = preferenceManager2.enableSmartspace.getAdapter()
    val smartspaceModeAdapter = preferenceManager2.smartspaceMode.getAdapter()
    val selectedMode = smartspaceModeAdapter.state.value
    val modeIsLawnchair = selectedMode == LawnchairSmartspace

    PreferenceLayout(
        label = stringResource(id = R.string.smartspace_widget),
        backArrowVisible = !LocalIsExpandedScreen.current && !fromWidget,
        modifier = modifier,
    ) {
        if (fromWidget) {
            SmartspacePreview()
            LawnchairSmartspaceSettings(smartspaceProvider)
        } else {
            MainSwitchPreference(
                adapter = smartspaceAdapter,
                label = stringResource(R.string.smartspace_widget_toggle_label),
                description = stringResource(id = R.string.smartspace_widget_toggle_description).takeIf { modeIsLawnchair },
            ) {
                if (modeIsLawnchair) {
                    SmartspacePreview()
                }
                PreferenceGroup {
                    SmartspaceProviderPreference(
                        adapter = smartspaceModeAdapter,
                    )
                }

                Crossfade(
                    targetState = selectedMode,
                    label = "Smartspace setting transition",
                ) { targetState ->
                    when (targetState) {
                        LawnchairSmartspace -> {
                            LawnchairSmartspaceSettings(smartspaceProvider)
                        }

                        SoulSpace -> {
                            SoulSpaceSettings()
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun LawnchairSmartspaceSettings(
    smartspaceProvider: SmartspaceProvider,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        PreferenceGroup(
            heading = stringResource(id = R.string.what_to_show),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            smartspaceProvider.dataSources
                .asSequence()
                .filter { it.isAvailable }
                .forEach {
                    key(it.providerName) {
                        SwitchPreference(
                            adapter = it.enabledPref.getAdapter(),
                            label = stringResource(id = it.providerName),
                        )
                    }
                }
        }
        SmartspaceDateAndTimePreferences()
    }
}

@Composable
fun SmartspaceProviderPreference(
    adapter: PreferenceAdapter<SmartspaceMode>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val entries = remember {
        SmartspaceMode.values().map { mode ->
            ListPreferenceEntry(
                value = mode,
                label = { stringResource(id = mode.nameResourceId) },
                enabled = mode.isAvailable(context = context),
            )
        }.toList()
    }

    ListPreference(
        adapter = adapter,
        entries = entries,
        label = stringResource(id = R.string.smartspace_mode_label),
        modifier = modifier,
    )
}

@Composable
fun SmartspacePreview(
    modifier: Modifier = Modifier,
) {
    val themeRes = if (isSelectedThemeDark) R.style.AppTheme_Dark else R.style.AppTheme_DarkText
    val context = LocalContext.current
    val themedContext = remember(themeRes) { ContextThemeWrapper(context, themeRes) }

    PreferenceGroup(
        heading = stringResource(id = R.string.preview_label),
        modifier = modifier,
    ) {
        Surface(
            color = preferenceGroupColor(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            CompositionLocalProvider(LocalContext provides themedContext) {
                AndroidView(
                    factory = {
                        val view = SmartspaceViewContainer(it, previewMode = true)
                        val height = it.resources
                            .getDimensionPixelSize(R.dimen.enhanced_smartspace_height)
                        view.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, height)
                        view
                    },
                    modifier = Modifier.padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                    ),
                )
            }
        }
        LaunchedEffect(key1 = null) {
            SmartspaceProvider.INSTANCE.get(context).startSetup(context as Activity)
        }
    }
}

@Composable
fun SmartspaceDateAndTimePreferences(
    modifier: Modifier = Modifier,
) {
    val preferenceManager2 = preferenceManager2()

    val calendarAdapter = preferenceManager2.smartspaceCalendar.getAdapter()
    val showDateAdapter = preferenceManager2.smartspaceShowDate.getAdapter()
    val showTimeAdapter = preferenceManager2.smartspaceShowTime.getAdapter()

    val calendarHasMinimumContent = !showDateAdapter.state.value || !showTimeAdapter.state.value
    val calendar = calendarAdapter.state.value

    PreferenceGroup(
        heading = stringResource(id = R.string.smartspace_date_and_time),
        modifier = modifier.padding(top = 8.dp),
    ) {
        val supportCustomizationFormat = calendar.formatCustomizationSupport
        ExpandAndShrink(visible = supportCustomizationFormat) {
            SwitchPreference(
                adapter = showDateAdapter,
                label = stringResource(id = R.string.smartspace_date),
                enabled = if (showDateAdapter.state.value) !calendarHasMinimumContent else true,
            )
        }
        ExpandAndShrink(visible = supportCustomizationFormat && showDateAdapter.state.value) {
            SmartspaceCalendarPreference()
        }
        ExpandAndShrink(visible = supportCustomizationFormat) {
            SwitchPreference(
                adapter = showTimeAdapter,
                label = stringResource(id = R.string.smartspace_time),
                enabled = if (showTimeAdapter.state.value) !calendarHasMinimumContent else true,
            )
        }
        ExpandAndShrink(visible = supportCustomizationFormat && showTimeAdapter.state.value) {
            SmartspaceTimeFormatPreference()
        }
    }
}

@Composable
fun SmartspaceTimeFormatPreference(
    modifier: Modifier = Modifier,
) {
    val entries = remember {
        SmartspaceTimeFormat.values().map { format ->
            ListPreferenceEntry(format) { stringResource(id = format.nameResourceId) }
        }
    }

    val adapter = preferenceManager2().smartspaceTimeFormat.getAdapter()

    ListPreference(
        adapter = adapter,
        entries = entries,
        label = stringResource(id = R.string.smartspace_time_format),
        modifier = modifier,
    )
}

@Composable
fun SmartspaceCalendarPreference(
    modifier: Modifier = Modifier,
) {
    val entries = remember {
        SmartspaceCalendar.values().map { calendar ->
            ListPreferenceEntry(calendar) { stringResource(id = calendar.nameResourceId) }
        }
    }

    val adapter = preferenceManager2().smartspaceCalendar.getAdapter()

    ListPreference(
        adapter = adapter,
        entries = entries,
        label = stringResource(id = R.string.smartspace_calendar),
        modifier = modifier,
    )
}

@Composable
fun SoulSpaceSettings(
    modifier: Modifier = Modifier,
) {
    val prefs = preferenceManager2()
    val cardBackgroundAdapter = prefs.soulSpaceCardBackground.getAdapter()
    val cardTextAdapter = prefs.soulSpaceCardTextColor.getAdapter()
    val cardReferenceAdapter = prefs.soulSpaceCardReferenceColor.getAdapter()
    val radiusAdapter = prefs.soulSpaceCardRadius.getAdapter()
    val fontSizeAdapter = prefs.soulSpaceCardFontSize.getAdapter()
    val fontStyleAdapter = prefs.soulSpaceCardFontStyle.getAdapter()
    var showColorPalette by remember { mutableStateOf(false) }
    var selectedSavedList by remember { mutableStateOf<String?>(null) }
    var paletteTarget by remember { mutableStateOf("background") }
    val palette = remember { listOf(0xFF172554.toInt(), 0xFF1E293B.toInt(), 0xFF064E3B.toInt(), 0xFF3F1D5B.toInt(), 0xFF7F1D1D.toInt(), 0xFFFFFFFF.toInt(), 0xFFF8FAFC.toInt(), 0xFFE2E8F0.toInt(), 0xFF000000.toInt(), 0xFF334155.toInt(), 0xFF0F766E.toInt(), 0xFF1D4ED8.toInt(), 0xFFFCD34D.toInt(), 0xFFBAE6FD.toInt(), 0xFFBBF7D0.toInt(), 0xFFFBCFE8.toInt(), 0xFF111827.toInt(), 0xFFDC2626.toInt(), 0xFF7C3AED.toInt(), 0xFFEA580C.toInt()) }
    val radiusEntries = remember { listOf(0, 6, 12, 18, 24, 32).map { value -> ListPreferenceEntry(value) { "$value dp" } } }
    val fontSizeEntries = remember { listOf(14, 16, 18, 20, 22, 24, 26, 28).map { value -> ListPreferenceEntry(value) { "$value sp" } } }
    val fontStyleEntries = remember { listOf(ListPreferenceEntry("normal") { "Normal" }, ListPreferenceEntry("medium") { "Medium / Strong" }, ListPreferenceEntry("bold") { "Bold" }, ListPreferenceEntry("italic") { "Italic" }, ListPreferenceEntry("serif") { "Serif" }) }
    val languageEntries = remember {
        listOf(
            ListPreferenceEntry("bengali_english") { stringResource(R.string.soul_space_language_both) },
            ListPreferenceEntry("bengali") { stringResource(R.string.soul_space_language_bengali) },
            ListPreferenceEntry("english") { stringResource(R.string.soul_space_language_english) },
            ListPreferenceEntry("original_bengali") { stringResource(R.string.soul_space_language_original_bengali) },
        )
    }
    fun savedItems(key: String): List<String> = runCatching {
        val raw = prefs.getCachedPreferences()[stringPreferencesKey(key)] ?: "[]"
        val array = JSONArray(raw)
        List(array.length()) { index -> array.getString(index) }
    }.getOrDefault(emptyList())

    val scheduleEntries = remember {
        listOf(
            ListPreferenceEntry("each_unlock") { stringResource(R.string.soul_space_schedule_unlock) },
            ListPreferenceEntry("daily") { "Once per day (first unlock)" },
            ListPreferenceEntry("manual") { stringResource(R.string.soul_space_schedule_manual) },
        )
    }

    val context = LocalContext.current
    var autoStartSoulSpaceAiDownload by remember { mutableStateOf(false) }
    LaunchedEffect(context) {
        val activity = context as? Activity
        if (activity?.intent?.getBooleanExtra("open_soul_space_ai", false) == true) {
            selectedSavedList = "soul_space_ai"
            autoStartSoulSpaceAiDownload = activity.intent.getBooleanExtra("auto_download_soul_space_ai", false)
            activity.intent.removeExtra("open_soul_space_ai")
            activity.intent.removeExtra("auto_download_soul_space_ai")
        }
    }
    Column(modifier.padding(top = 8.dp)) {
        if (selectedSavedList == "soul_space_ai") {
            SoulSpaceAiSettings(
                onBack = { selectedSavedList = null },
                autoStartDownload = autoStartSoulSpaceAiDownload,
            )
        } else if (selectedSavedList != null) {
            val key = selectedSavedList!!
            val title = if (key == "soul_space_favorites") "Favorites" else "History"
            val items = savedItems(key)
            PreferenceGroup(heading = title) {
                TextButton(onClick = { selectedSavedList = null }) {
                    Text("← Back to Soul Space settings")
                }
                if (items.isEmpty()) {
                    Text(
                        text = if (key == "soul_space_favorites") "No favorites saved yet." else "No wisdom history yet.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = entry,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                TextButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText(title, entry))
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Copy")
                                }
                            }
                            if (index < items.lastIndex) {
                                androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        } else {
        PreferenceGroup(heading = stringResource(id = R.string.soul_space_settings)) {
            androidx.compose.material3.Text(
                text = stringResource(id = R.string.soul_space_settings_intro),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            ListPreference(
                adapter = prefs.soulSpaceLanguage.getAdapter(),
                entries = languageEntries,
                label = stringResource(R.string.soul_space_language_label),
            )
            ClickablePreference(
                label = "AI Settings",
                subtitle = "On-device AI model and download manager",
                onClick = { selectedSavedList = "soul_space_ai" },
            )
            ClickablePreference(
                label = "Favorites",
                subtitle = "View your saved wisdom",
                onClick = { selectedSavedList = "soul_space_favorites" },
            )
            ClickablePreference(
                label = "History",
                subtitle = "View recently displayed wisdom",
                onClick = { selectedSavedList = "soul_space_history" },
            )
            ClickablePreference(
                label = "Card background color",
                subtitle = "Choose the filled rectangle color",
                onClick = { paletteTarget = "background"; showColorPalette = true },
            )
            ClickablePreference(
                label = "Quote text color",
                subtitle = "Default: white for clear contrast",
                onClick = { paletteTarget = "text"; showColorPalette = true },
            )
            ClickablePreference(
                label = "Source / reference color",
                subtitle = "Default: bright amber",
                onClick = { paletteTarget = "reference"; showColorPalette = true },
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf("Card" to cardBackgroundAdapter.state.value, "Text" to cardTextAdapter.state.value, "Reference" to cardReferenceAdapter.state.value).forEach { (label, colorValue) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(24.dp).background(Color(colorValue), CircleShape).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape))
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            if (showColorPalette) {
                AlertDialog(
                    onDismissRequest = { showColorPalette = false },
                    title = { Text("Choose color") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            palette.chunked(4).forEach { colorRow ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                    colorRow.forEach { colorValue ->
                                        Box(
                                            modifier = Modifier.size(38.dp).background(Color(colorValue), CircleShape).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape).clickable {
                                                when (paletteTarget) {
                                                    "background" -> cardBackgroundAdapter.onChange(colorValue)
                                                    "text" -> cardTextAdapter.onChange(colorValue)
                                                    else -> cardReferenceAdapter.onChange(colorValue)
                                                }
                                                showColorPalette = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = { showColorPalette = false }) { Text(stringResource(android.R.string.cancel)) } },
                )
            }
            ListPreference(adapter = radiusAdapter, entries = radiusEntries, label = "Corner radius")
            ListPreference(adapter = fontSizeAdapter, entries = fontSizeEntries, label = "Quote font size")
            ListPreference(adapter = fontStyleAdapter, entries = fontStyleEntries, label = "Quote font style")
            androidx.compose.material3.Text(
                text = stringResource(R.string.soul_space_source_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            SwitchPreference(
                adapter = prefs.soulSpaceSourceQuran.getAdapter(),
                label = stringResource(R.string.soul_space_source_quran),
            )
            SwitchPreference(
                adapter = prefs.soulSpaceSourceHindu.getAdapter(),
                label = stringResource(R.string.soul_space_source_hindu),
            )
            SwitchPreference(
                adapter = prefs.soulSpaceSourceBuddhist.getAdapter(),
                label = stringResource(R.string.soul_space_source_buddhist),
            )
            SwitchPreference(
                adapter = prefs.soulSpaceSourceSikh.getAdapter(),
                label = stringResource(R.string.soul_space_source_sikh),
            )
            SwitchPreference(
                adapter = prefs.soulSpaceSourceSufi.getAdapter(),
                label = stringResource(R.string.soul_space_source_sufi),
            )
            SwitchPreference(
                adapter = prefs.soulSpaceSourcePhilosophy.getAdapter(),
                label = stringResource(R.string.soul_space_source_philosophy),
            )
            SwitchPreference(
                adapter = prefs.soulSpaceSourceGeneral.getAdapter(),
                label = stringResource(R.string.soul_space_source_general),
            )
            ListPreference(
                adapter = prefs.soulSpaceSchedule.getAdapter(),
                entries = scheduleEntries,
                label = stringResource(R.string.soul_space_schedule_label),
            )
            androidx.compose.material3.Text(
                text = stringResource(id = R.string.soul_space_privacy_note),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        }
    }
}

@Composable
fun SmartspacerSettings(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val prefs2 = preferenceManager2()

    Column(modifier) {
        PreferenceGroup(
            heading = stringResource(id = R.string.smartspacer_settings),
        ) {
            SliderPreference(
                label = stringResource(R.string.maximum_number_of_targets),
                adapter = prefs2.smartspacerMaxCount.getAdapter(),
                valueRange = 5..15,
                step = 1,
            )
            ClickablePreference(label = stringResource(R.string.open_smartspacer_settings)) {
                val intent = context.packageManager.getLaunchIntentForPackage(
                    SmartspacerConstants.SMARTSPACER_PACKAGE_NAME,
                )
                context.startActivity(intent)
            }
        }
    }
}
