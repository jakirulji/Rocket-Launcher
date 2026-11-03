package app.lawnchair.gestures.handlers

import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import app.lawnchair.ui.preferences.components.controls.ListPreferenceEntry
import com.android.launcher3.R

enum class SleepMode(
    @StringRes val labelResourceId: Int,
) {
    AUTO(
        labelResourceId = R.string.sleep_mode_auto,
    ),
    ROOT(
        labelResourceId = R.string.sleep_mode_root,
    ),
    // Retained for reading old saved values; no longer offered in the UI.
    ACCESSIBILITY(
        labelResourceId = R.string.sleep_mode_accessibility,
    ),
    DEVICE_ADMIN(
        labelResourceId = R.string.sleep_mode_device_admin,
    ),
    ;

    companion object {
        fun values() = enumValues<SleepMode>().toList()

        fun fromString(string: String) = values().firstOrNull { it.toString() == string }

        fun entries(): List<ListPreferenceEntry<SleepMode>> = values()
            .filterNot { it == ACCESSIBILITY }
            .map {
                ListPreferenceEntry(value = it) { stringResource(id = it.labelResourceId) }
            }
    }
}
