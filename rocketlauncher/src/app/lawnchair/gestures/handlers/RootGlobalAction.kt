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
 * Executes a small, explicit set of system actions through Android's input
 * command when root is available. This lets rooted users keep global gesture
 * actions without enabling Rocket Launcher's Accessibility Service.
 *
 * This is not an accessibility-service replacement on unrooted devices.
 */
internal object RootGlobalAction {

    fun perform(action: Int): Boolean {
        val keyCode = when (action) {
            AccessibilityService.GLOBAL_ACTION_RECENTS -> KeyEvent.KEYCODE_APP_SWITCH
            AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS -> KeyEvent.KEYCODE_NOTIFICATION
            AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS -> KeyEvent.KEYCODE_QUICK_SETTINGS
            else -> return false
        }

        return runCatching {
            if (!Shell.getShell().isRoot) {
                return false
            }
            Shell.cmd("input keyevent $keyCode").exec().isSuccess
        }.getOrDefault(false)
    }

}
