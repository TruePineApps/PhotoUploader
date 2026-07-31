package com.truepineapps.photouploader.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.truepineapps.photouploader.core.presentation.design.ExtendedColorScheme
import com.truepineapps.photouploader.core.presentation.design.LocalExtendedColors
import com.truepineapps.photouploader.core.presentation.design.toContainer
import com.truepineapps.photouploader.core.util.ContrastLevel
import com.truepineapps.photouploader.core.util.getSystemContrastLevel
import com.truepineapps.photouploader.core.util.isHighContrastDark
import io.github.kdroidfilter.platformtools.darkmodedetector.isSystemInDarkMode

// app/theme/AppTheme.kt
val myExtendedColors = ExtendedColorScheme(
    selectedItemHighlight = LightPalette.Primary70,
    statusSuccess = StatusPalette.Success,
    statusWarning = StatusPalette.Warning,
    statusError = StatusPalette.Error,
    statusDisabled = StatusPalette.Disabled,
    successContainer = StatusPalette.Success.toContainer(),
    warningContainer = StatusPalette.Warning.toContainer(),
    errorContainer = StatusPalette.Error.toContainer(),
)

// Dynamic color is only available on Android 12+, no multiplatform support
@Composable
fun AppTheme(
    darkTheme: Boolean? = null,
    contrastLevel: ContrastLevel? = null,
    content: @Composable () -> Unit
) {
    // Use provided parameters or fall back to reactive system detectors
    val currentContrast = contrastLevel ?: getSystemContrastLevel()
    val standardDark = darkTheme ?: isSystemInDarkMode()

    // Windows 11 handles Contrast Themes separately from the standard Dark/Light mode.
    // When High Contrast is on, we use a specialized detector for theme "darkness".
    // If it returns null, we fall back to the standard dark mode preference.
    val isDark = isHighContrastDark() ?: standardDark

    val colorScheme = when {
        // Handle Dark Mode permutations
        isDark && currentContrast == ContrastLevel.High -> darkHighContrastColorScheme
        isDark && currentContrast == ContrastLevel.Medium -> darkMediumContrastColorScheme
        isDark -> darkLowContrastLevelColorScheme

        // Handle Light Mode permutations
        currentContrast == ContrastLevel.High -> lightHighContrastColorScheme
        currentContrast == ContrastLevel.Medium -> lightMediumContrastColorScheme
        else -> lightLowContrastColorScheme
    }
    CompositionLocalProvider(LocalExtendedColors provides myExtendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getAppTypography(),
            content = content
        )
    }
}
