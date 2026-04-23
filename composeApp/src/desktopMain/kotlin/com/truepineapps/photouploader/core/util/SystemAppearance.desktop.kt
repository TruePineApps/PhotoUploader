package com.truepineapps.photouploader.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Toolkit

// Windows is the primary OS with a system-level High Contrast toggle accessible via Java AWT.
// macOS/Linux support via Java is limited, so they usually default to Standard.
@Composable
actual fun getSystemContrastLevel(): ContrastLevel {
    return remember {
        // Windows High Contrast check
        val highContrastOn = Toolkit.getDefaultToolkit()
            .getDesktopProperty("win.highContrast.on") as? Boolean

        if (highContrastOn == true) {
            ContrastLevel.High
        } else {
            ContrastLevel.Standard
        }
    }
}