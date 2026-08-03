/*
 * Copyright (c) 2026 True Pine Apps
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

package com.truepineapps.photouploader.core.util

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext


@Composable
actual fun getSystemContrastLevel(): ContrastLevel {
    val context = LocalContext.current

    return remember(context) {
        // API 34+ (Android 14) supports 3 explicit levels
        if (Build.VERSION.SDK_INT >= 34) {
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            // contrast is a float: 0.0 (standard) -> 1.0 (max)
            // 0.33 is Medium, 0.66 is High.
            return@remember when {
                uiModeManager.contrast >= 0.66f -> ContrastLevel.High
                uiModeManager.contrast >= 0.33f -> ContrastLevel.Medium
                else -> ContrastLevel.Standard
            }
        }

        // Fallback for API < 34
        // Checks the "High Contrast Text" toggle in Accessibility Settings
        // This is a binary on/off, so we map "On" to "High".
        val isHighTextContrastEnabled = try {
            Settings.Secure.getInt(
                context.contentResolver,
                "high_text_contrast_enabled",
                0,
            ) == 1
        } catch (_: Exception) {
            false
        }

        if (isHighTextContrastEnabled) ContrastLevel.High else ContrastLevel.Standard
    }
}

@Composable
actual fun isHighContrastDark(): Boolean? {
    // On Android, the standard isSystemInDarkMode() already respects the system theme
    // correctly even when high contrast is active.
    return null
}

