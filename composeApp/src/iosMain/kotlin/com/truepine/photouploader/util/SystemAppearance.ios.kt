package com.truepine.photouploader.util

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