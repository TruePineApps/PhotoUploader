package com.truepineapps.photouploader.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

// Windows is the primary OS with a system-level High Contrast toggle accessible via Java AWT.
// macOS/Linux support via Java is limited, so they usually default to Standard.
@Composable
actual fun getSystemContrastLevel(): ContrastLevel = 
    DesktopThemeMonitor.themeState.collectAsState().value.contrastLevel

@Composable
actual fun isHighContrastDark(): Boolean? =
    DesktopThemeMonitor.themeState.collectAsState().value.isHighContrastDark
