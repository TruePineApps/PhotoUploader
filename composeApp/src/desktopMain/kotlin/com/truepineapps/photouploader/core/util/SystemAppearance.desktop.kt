package com.truepineapps.photouploader.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import java.awt.Toolkit

// Windows is the primary OS with a system-level High Contrast toggle accessible via Java AWT.
// macOS/Linux support via Java is limited, so they usually default to Standard.
@Composable
actual fun getSystemContrastLevel(): ContrastLevel {
    val isLinux = remember { LinuxHighContrastDetector.isLinux() }
    
    if (isLinux) {
        // On Linux, start monitoring high contrast for reactive updates
        val highContrastOn by LinuxHighContrastDetector.monitorHighContrast().collectAsState(initial = false)
        return if (highContrastOn) ContrastLevel.High else ContrastLevel.Standard
    }

    return remember {
        // Windows High Contrast check
        val highContrastOn = isWindowsHighContrast() ?: false

        if (highContrastOn) {
            ContrastLevel.High
        } else {
            ContrastLevel.Standard
        }
    }
}

fun isWindowsHighContrast(): Boolean? = Toolkit.getDefaultToolkit()
    .getDesktopProperty("win.highContrast.on") as? Boolean
