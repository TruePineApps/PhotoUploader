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
                "high_text_contrast_enabled", 0
            ) == 1
        } catch (e: Exception) {
            false
        }

        if (isHighTextContrastEnabled) ContrastLevel.High else ContrastLevel.Standard
    }
}

