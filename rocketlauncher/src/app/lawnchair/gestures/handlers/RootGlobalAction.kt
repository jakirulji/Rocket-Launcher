/*
 * Copyright 2026, Rocket Launcher contributors.
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

package app.lawnchair.gestures.handlers

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import com.topjohnwu.superuser.Shell

/**
 * Executes a small, explicit set of system actions through root when available.
 * This lets rooted users keep global gesture actions without enabling Rocket
 * Launcher's Accessibility Service.
 *
 * This is not an accessibility-service replacement on unrooted devices.
 */
internal object RootGlobalAction {

    fun perform(action: Int): Boolean {
        val command = when (action) {
            AccessibilityService.GLOBAL_ACTION_RECENTS ->
                "input keyevent ${KeyEvent.KEYCODE_APP_SWITCH}"

            AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS ->
                "input keyevent ${KeyEvent.KEYCODE_NOTIFICATION}"

            AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS ->
                "cmd statusbar expand-settings"

            else -> return false
        }

        return runCatching {
            val shell = Shell.getShell()
            shell.isRoot && Shell.cmd(command).exec().isSuccess
        }.getOrDefault(false)
    }
}
