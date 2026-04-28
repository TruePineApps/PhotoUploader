package com.truepineapps.photouploader.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.truepineapps.photouploader.core.presentation.design.ExtendedColorScheme
import com.truepineapps.photouploader.core.presentation.design.LocalExtendedColors
import com.truepineapps.photouploader.core.util.ContrastLevel
import com.truepineapps.photouploader.core.util.getSystemContrastLevel

// app/theme/AppTheme.kt
val myExtendedColors = ExtendedColorScheme(
    selectedItemHighlight = LightPalette.Primary70,
    statusSuccess = StatusPalette.Success,
    statusWarning = StatusPalette.Warning,
    statusError = StatusPalette.Error,
    statusDisabled = StatusPalette.Disabled

)

// Dynamic color is only available on Android 12+, no multiplatform support
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    contrastLevel: ContrastLevel = getSystemContrastLevel(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Handle Dark Mode permutations
        darkTheme && contrastLevel == ContrastLevel.High -> darkHighContrastColorScheme
        darkTheme && contrastLevel == ContrastLevel.Medium -> darkMediumContrastColorScheme
        darkTheme -> darkLowContrastLevelColorScheme

        // Handle Light Mode permutations
        contrastLevel == ContrastLevel.High -> lightHighContrastColorScheme
        contrastLevel == ContrastLevel.Medium -> lightMediumContrastColorScheme
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
