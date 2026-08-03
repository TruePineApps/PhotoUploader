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

// composeApp/src/iosMain/kotlin/.../util/SystemAppearance.ios.kt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIAccessibilityContrastHigh
import platform.UIKit.UIAccessibilityDarkerSystemColorsEnabled

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun getSystemContrastLevel(): ContrastLevel {
    // We access the current view controller to get the current TraitCollection
    val viewController = LocalUIViewController.current

    return remember(viewController) {
        // 1. Try the modern iOS 13+ API first
        // operatingSystemVersion is a C-Struct, so we must use .useContents to read 'majorVersion'
        val majorVersion = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
            majorVersion
        }

        // 2. Try the modern iOS 13+ API first
        if (majorVersion >= 13) {
            val traits = viewController.traitCollection
            if (traits.accessibilityContrast == UIAccessibilityContrastHigh) {
                return@remember ContrastLevel.High
            }
        } else {
            // 3. Fallback for iOS 12 and below
            if (UIAccessibilityDarkerSystemColorsEnabled()) {
                return@remember ContrastLevel.High
            }
        }

        // Default to Standard
        ContrastLevel.Standard
    }
}

@Composable
actual fun isHighContrastDark(): Boolean? {
    // On iOS, the standard isSystemInDarkMode() already respects the system theme
    // correctly even when high contrast is active.
    return null
}
