/*
 * Copyright 2021, Lawnchair
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
 * Rocket Launcher modification: adds the Digital Wellbeing entry to Home Settings.
 */

package app.lawnchair.ui.preferences.destinations

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.provider.Settings
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import app.lawnchair.LawnchairApp
import app.lawnchair.LawnchairLauncher
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences.observeAsState
import app.lawnchair.preferences.preferenceManager
import app.lawnchair.preferences2.firstCached
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.ui.OverflowMenuGrouped
import app.lawnchair.ui.preferences.components.AnnouncementPreference
import app.lawnchair.ui.preferences.components.controls.WarningPreference
import app.lawnchair.ui.preferences.components.layout.ClickableIcon
import app.lawnchair.ui.preferences.components.layout.ExpandAndShrink
import app.lawnchair.ui.preferences.components.layout.MistExpressiveGlyph
import app.lawnchair.ui.preferences.components.layout.MistExpressiveShape
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import app.lawnchair.ui.preferences.components.layout.PreferenceTemplate
import app.lawnchair.ui.preferences.components.layout.ProvideDescriptionTextStyle
import app.lawnchair.ui.preferences.navigation.About
import app.lawnchair.ui.preferences.navigation.AppDrawer
import app.lawnchair.ui.preferences.navigation.BackupAndRestore
import app.lawnchair.ui.preferences.navigation.DebugMenu
import app.lawnchair.ui.preferences.navigation.Dock
import app.lawnchair.ui.preferences.navigation.ExperimentalFeatures
import app.lawnchair.ui.preferences.navigation.Folders
import app.lawnchair.ui.preferences.navigation.General
import app.lawnchair.ui.preferences.navigation.Gestures
import app.lawnchair.ui.preferences.navigation.HomeScreen
import app.lawnchair.ui.preferences.navigation.Misc
import app.lawnchair.ui.preferences.navigation.PreferenceRootRoute
import app.lawnchair.ui.preferences.navigation.Quickstep
import app.lawnchair.ui.preferences.navigation.Recent
import app.lawnchair.ui.preferences.navigation.Search
import app.lawnchair.ui.preferences.navigation.Smartspace
import app.lawnchair.ui.util.addIf
import app.lawnchair.util.isDefaultLauncher
import app.lawnchair.util.restartLauncher
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.util.MSDLPlayerWrapper
import com.google.android.msdl.data.model.MSDLToken

@Composable
fun PreferencesDashboard(
    currentRoute: PreferenceRootRoute,
    onNavigate: (PreferenceRootRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val prefs = preferenceManager()
    val prefs2 = preferenceManager2()
    val scrollState = rememberScrollState()
    var showSettingsSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchInteractionSource = remember { MutableInteractionSource() }
    val searchPressed by searchInteractionSource.collectIsPressedAsState()
    val searchScale by animateFloatAsState(
        targetValue = if (searchPressed) 0.97f else 1f,
        animationSpec = if (searchPressed) {
            spring(dampingRatio = 0.9f, stiffness = 1400f)
        } else {
            spring(dampingRatio = 0.62f, stiffness = 380f)
        },
        label = "Mist search press scale",
    )
    val showCompactTitle = with(LocalDensity.current) { scrollState.value > 64.dp.roundToPx() }

    val aboutDescrption = if (prefs.hideVersionInfo.get()) {
        prefs.pseudonymVersion.get()
    } else {
        "${context.getString(R.string.derived_app_name)} ${BuildConfig.MAJOR_VERSION}"
    }

    PreferenceLayout(
        label = if (showCompactTitle) stringResource(id = R.string.settings) else "",
        modifier = modifier,
        isExpandedScreen = true,
        scrollState = scrollState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        backArrowVisible = false,
        actions = { PreferencesOverflowMenu(currentRoute = currentRoute, onNavigate = onNavigate) },
    ) {
        AnnouncementPreference()

        if (BuildConfig.APPLICATION_ID.contains("nightly") || BuildConfig.DEBUG) {
            PreferencesDebugWarning()
        }

        if (!context.isDefaultLauncher()) {
            PreferencesSetDefaultLauncherWarning()
        }

        // MistOS expressive dashboard: large title, asymmetric first row, then paired tiles.
        Text(
            text = stringResource(id = R.string.settings),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 4.dp),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 40.sp,
                lineHeight = 38.sp,
                letterSpacing = (-0.4).sp,
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer(scaleX = searchScale, scaleY = searchScale)
                .clickable(
                    interactionSource = searchInteractionSource,
                    indication = null,
                    onClick = { showSettingsSearch = true },
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Row(
                modifier = Modifier.height(56.dp).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.settings_search_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        val searchTargets = listOf(
            DashboardSearchTarget(stringResource(R.string.icons_and_general_label), "Icon packs, shapes, themes and general options", General),
            DashboardSearchTarget(stringResource(R.string.home_screen_label), "Wallpaper parallax and zoom, grid, widgets, labels and home layout", HomeScreen),
            DashboardSearchTarget(stringResource(R.string.app_drawer_label), "Drawer layout, hidden apps, grid, labels and Focus Mode", AppDrawer),
            DashboardSearchTarget(stringResource(R.string.recent_settings_label), "Recent apps, screenshot, share and clear all", Recent),
            DashboardSearchTarget(stringResource(R.string.dock_label), "Dock layout, icons and background", Dock),
            DashboardSearchTarget(stringResource(R.string.misc_label), stringResource(R.string.misc_description), Misc),
            DashboardSearchTarget(stringResource(R.string.smartspace_widget), "At a glance and Smartspace widget", Smartspace),
            DashboardSearchTarget(stringResource(R.string.search_bar_label), "Search providers and search bar settings", Search()),
            DashboardSearchTarget(stringResource(R.string.folders_label), "Folder appearance and behavior", Folders),
            DashboardSearchTarget(stringResource(R.string.gestures_label), "Launcher gesture actions", Gestures),
            DashboardSearchTarget(stringResource(R.string.quickstep_label), "Taskbar and Quickstep options", Quickstep),
            DashboardSearchTarget(stringResource(R.string.backup_and_restore_label), "Backup and restore launcher settings", BackupAndRestore),
            DashboardSearchTarget(stringResource(R.string.about_label), aboutDescrption, About),
        )
        if (showSettingsSearch) {
            val results = searchTargets.filter {
                "${it.title} ${it.description}".contains(searchQuery.trim(), ignoreCase = true)
            }
            AlertDialog(
                onDismissRequest = { showSettingsSearch = false },
                title = { Text(stringResource(R.string.settings)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.settings_search_hint)) },
                        )
                        Spacer(Modifier.height(8.dp))
                        if (results.isEmpty()) {
                            Text("No matching settings categories")
                        } else {
                            results.take(8).forEach { target ->
                                TextButton(
                                    onClick = {
                                        onNavigate(target.route)
                                        showSettingsSearch = false
                                        searchQuery = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(Modifier.fillMaxWidth()) {
                                        Text(target.title, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            target.description,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsSearch = false }) {
                        Text("Close")
                    }
                },
            )
        }

        val deckLayout = prefs2.deckLayout.getAdapter()
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MistSettingsTile(
                    label = stringResource(R.string.icons_and_general_label),
                    description = stringResource(R.string.icons_and_general_description),
                    iconResource = R.drawable.ic_settings_icons_mist,
                    containerColor = Color(0xFFDCE8FF),
                    contentColor = Color(0xFF183B70),
                    onClick = { onNavigate(General) },
                    modifier = Modifier.weight(1f).height(272.dp),
                    shape = MistExpressiveShape.COOKIE,
                    large = true,
                    selected = currentRoute is General,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MistSettingsTile(
                        label = stringResource(R.string.home_screen_label),
                        description = stringResource(R.string.home_screen_description),
                        iconResource = R.drawable.ic_settings_homescreen_mist,
                        containerColor = Color(0xFFFFE1CC),
                        contentColor = Color(0xFF713A18),
                        onClick = { onNavigate(HomeScreen) },
                        modifier = Modifier.fillMaxWidth().height(132.dp),
                        shape = MistExpressiveShape.CLOVER,
                        selected = currentRoute is HomeScreen,
                    )
                    ExpandAndShrink(visible = !deckLayout.state.value) {
                        MistSettingsTile(
                            label = stringResource(R.string.app_drawer_label),
                            description = stringResource(R.string.app_drawer_description),
                            iconResource = R.drawable.ic_settings_appdrawer_mist,
                            containerColor = Color(0xFFFFEFBF),
                            contentColor = Color(0xFF634B00),
                            onClick = { onNavigate(AppDrawer) },
                            modifier = Modifier.fillMaxWidth().height(132.dp),
                            shape = MistExpressiveShape.BURST,
                            selected = currentRoute is AppDrawer,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MistSettingsTile(
                    label = stringResource(R.string.recent_settings_label),
                    description = stringResource(R.string.recent_settings_description),
                    iconResource = R.drawable.ic_settings_recents_mist,
                    containerColor = Color(0xFFFFDADD),
                    contentColor = Color(0xFF762B35),
                    onClick = { onNavigate(Recent) },
                    modifier = Modifier.weight(1f).height(132.dp),
                    shape = MistExpressiveShape.PENTAGON,
                    selected = currentRoute is Recent,
                )
                MistSettingsTile(
                    label = stringResource(R.string.misc_label),
                    description = stringResource(R.string.misc_description),
                    iconResource = R.drawable.ic_settings_misc_mist,
                    containerColor = Color(0xFFD5F1F0),
                    contentColor = Color(0xFF174D4B),
                    onClick = { onNavigate(Misc) },
                    modifier = Modifier.weight(1f).height(132.dp),
                    shape = MistExpressiveShape.FLOWER,
                    selected = currentRoute is Misc,
                )
            }
        }

        Text(
            text = stringResource(R.string.additional_features_label),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 2.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MistSettingsTile(
                    label = stringResource(R.string.dock_label),
                    description = stringResource(R.string.dock_description),
                    iconResource = R.drawable.ic_dock,
                    containerColor = Color(0xFFD5F1F0),
                    contentColor = Color(0xFF174D4B),
                    onClick = { onNavigate(Dock) },
                    modifier = Modifier.weight(1f).height(136.dp),
                    selected = currentRoute is Dock,
                )
                MistSettingsTile(
                    label = stringResource(R.string.smartspace_widget),
                    description = stringResource(R.string.smartspace_widget_description),
                    iconResource = if (prefs2.enableSmartspace.firstCached()) R.drawable.ic_smartspace else R.drawable.ic_smartspace_off,
                    containerColor = Color(0xFFE9DFFF),
                    contentColor = Color(0xFF49316D),
                    onClick = { onNavigate(Smartspace) },
                    modifier = Modifier.weight(1f).height(136.dp),
                    selected = currentRoute is Smartspace,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MistSettingsTile(
                    label = stringResource(R.string.search_bar_label),
                    description = stringResource(R.string.drawer_search_description),
                    iconResource = R.drawable.ic_search,
                    containerColor = Color(0xFFDCEBFA),
                    contentColor = Color(0xFF234768),
                    onClick = { onNavigate(Search()) },
                    modifier = Modifier.weight(1f).height(136.dp),
                    selected = currentRoute is Search,
                )
                MistSettingsTile(
                    label = stringResource(R.string.folders_label),
                    description = stringResource(R.string.folders_description),
                    iconResource = R.drawable.ic_folder,
                    containerColor = Color(0xFFE7E7F8),
                    contentColor = Color(0xFF38385E),
                    onClick = { onNavigate(Folders) },
                    modifier = Modifier.weight(1f).height(136.dp),
                    selected = currentRoute is Folders,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MistSettingsTile(
                    label = stringResource(R.string.gestures_label),
                    description = stringResource(R.string.gestures_description),
                    iconResource = R.drawable.ic_gestures,
                    containerColor = Color(0xFFFFE4EC),
                    contentColor = Color(0xFF6E3046),
                    onClick = { onNavigate(Gestures) },
                    modifier = Modifier.weight(1f).height(136.dp),
                    selected = currentRoute is Gestures,
                )
                ExpandAndShrink(visible = LawnchairApp.isRecentsEnabled || BuildConfig.DEBUG) {
                    MistSettingsTile(
                        label = stringResource(R.string.quickstep_label),
                        description = stringResource(R.string.quickstep_description),
                        iconResource = R.drawable.ic_quickstep,
                        containerColor = Color(0xFFE0E8F8),
                        contentColor = Color(0xFF334A70),
                        onClick = { onNavigate(Quickstep) },
                        modifier = Modifier.weight(1f).height(136.dp),
                        selected = currentRoute is Quickstep,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MistSettingsTile(
                    label = stringResource(R.string.digital_wellbeing_title),
                    description = stringResource(R.string.digital_wellbeing_summary),
                    iconResource = R.drawable.ic_general,
                    containerColor = Color(0xFFE2F0D9),
                    contentColor = Color(0xFF34502B),
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                com.android.launcher3.wellbeing.DigitalWellbeingSettingsActivity::class.java,
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f).height(136.dp),
                )
                MistSettingsTile(
                    label = stringResource(R.string.backup_and_restore_label),
                    description = stringResource(R.string.backup_and_restore_description),
                    iconResource = R.drawable.backup_restore,
                    containerColor = Color(0xFFFFE8C9),
                    contentColor = Color(0xFF68451D),
                    onClick = { onNavigate(BackupAndRestore) },
                    modifier = Modifier.weight(1f).height(136.dp),
                    selected = currentRoute is BackupAndRestore,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MistSettingsTile(
                    label = stringResource(R.string.about_label),
                    description = aboutDescrption,
                    iconResource = R.drawable.ic_about,
                    containerColor = Color(0xFFE5E5E5),
                    contentColor = Color(0xFF414141),
                    onClick = { onNavigate(About) },
                    modifier = Modifier.weight(1f).height(136.dp),
                    selected = currentRoute is About,
                )
            }
        }
    }
}

private data class DashboardSearchTarget(
    val title: String,
    val description: String,
    val route: PreferenceRootRoute,
)

@Composable
private fun MistSettingsTile(
    label: String,
    description: String,
    @androidx.annotation.DrawableRes iconResource: Int,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: MistExpressiveShape = MistExpressiveShape.CIRCLE,
    large: Boolean = false,
    selected: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = if (pressed) {
            spring(dampingRatio = 0.9f, stiffness = 1400f)
        } else {
            spring(dampingRatio = 0.62f, stiffness = 380f)
        },
        label = "Mist tile press scale",
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (pressed) 44.dp else 28.dp,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 380f),
        label = "Mist tile corner radius",
    )
    val glyphRotation by animateFloatAsState(
        targetValue = if (pressed) 40f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 380f),
        label = "Mist tile glyph rotation",
    )

    Surface(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = if (selected) containerColor.copy(alpha = 0.78f) else containerColor,
        tonalElevation = if (selected) 4.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Box(
                modifier = Modifier.size(if (large) 76.dp else 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                MistExpressiveGlyph(
                    shape = shape,
                    color = contentColor,
                    size = if (large) 76.dp else 48.dp,
                )
                Icon(
                    painter = painterResource(iconResource),
                    contentDescription = null,
                    tint = containerColor,
                    modifier = Modifier
                        .size(if (large) 36.dp else 24.dp)
                        .graphicsLayer(rotationZ = glyphRotation),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 2,
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.82f),
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
fun RowScope.PreferencesOverflowMenu(
    currentRoute: PreferenceRootRoute,
    onNavigate: (PreferenceRootRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val enableDebug by preferenceManager().enableDebugMenu.observeAsState()
    val highlightColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    val highlightShape = MaterialTheme.shapes.large

    if (enableDebug) {
        ClickableIcon(
            imageVector = Icons.Rounded.Build,
            onClick = { onNavigate(DebugMenu) },
            modifier = Modifier.addIf(currentRoute == DebugMenu) {
                Modifier
                    .clip(highlightShape)
                    .background(highlightColor)
            },
        )
    }
    val context = LocalContext.current

    OverflowMenuGrouped(
        modifier = modifier.addIf(
            listOf(ExperimentalFeatures).any {
                currentRoute == it
            },
        ) {
            Modifier
                .clip(highlightShape)
                .background(highlightColor)
        },
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(0, 1),
        ) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_about),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {
                    openAppInfo(context)
                    hideMenu()
                },
                text = {
                    Text(text = stringResource(id = R.string.app_info_drop_target_label))
                },
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {
                    restartLauncher(context)
                    hideMenu()
                },
                text = {
                    Text(text = stringResource(id = R.string.debug_restart_launcher))
                },
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {
                    onNavigate(ExperimentalFeatures)
                    hideMenu()
                },
                text = {
                    Text(text = stringResource(id = R.string.experimental_features_label))
                },
            )
        }

        Spacer(Modifier.height(MenuDefaults.GroupSpacing))
    }
}

@Composable
fun PreferencesDebugWarning(
    modifier: Modifier = Modifier,
) {
    WarningPreference(
        // Don't move to strings.xml, no need to translate this warning
        text = "You are using a development build, which may contain bugs and broken features. Use at your own risk!",
        modifier = modifier.padding(horizontal = 16.dp),
        standalone = true,
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    )
}

@Composable
fun PreferencesSetDefaultLauncherWarning(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mMSDLPlayerWrapper = MSDLPlayerWrapper.INSTANCE.get(context)
    Surface(
        modifier = modifier.padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        PreferenceTemplate(
            modifier = Modifier,
            onClick = {
                mMSDLPlayerWrapper.playToken(MSDLToken.TAP_MEDIUM_EMPHASIS)
                Intent(Settings.ACTION_HOME_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .let { context.startActivity(it) }
                (context as? Activity)?.finish()
            },
            title = {
                ProvideDescriptionTextStyle {
                    Text(
                        text = stringResource(id = R.string.set_default_launcher_tip),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            startWidget = {
                Icon(
                    imageVector = Icons.Rounded.TipsAndUpdates,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null,
                )
            },
        )
    }
}

fun openAppInfo(context: Context) {
    val launcherApps = context.getSystemService<LauncherApps>()
    val componentName = ComponentName(context, LawnchairLauncher::class.java)
    launcherApps?.startAppDetailsActivity(componentName, Process.myUserHandle(), null, null)
}
