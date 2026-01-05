package com.truepineapps.photouploader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.truepineapps.photouploader.util.ContrastLevel
import com.truepineapps.photouploader.util.getSystemContrastLevel

// Dynamic color is only available on Android 12+, no multiplatform support
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    contrastLevel:ContrastLevel = getSystemContrastLevel(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getAppTypography(),
        content = content
    )
}
