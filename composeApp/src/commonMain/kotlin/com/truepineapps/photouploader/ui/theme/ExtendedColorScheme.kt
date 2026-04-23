package com.truepineapps.photouploader.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// core/presentation/design/CustomColorScheme.kt
data class ExtendedColorScheme(
    val selectedItemHighlight: Color,
    val statusSuccess: Color,
    val statusWarning: Color,
    val statusError: Color,
    val statusDisabled: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColorScheme(
        selectedItemHighlight = Color.Unspecified,
        statusSuccess = Color.Unspecified,
        statusWarning = Color.Unspecified,
        statusError = Color.Unspecified,
        statusDisabled = Color.Unspecified,
    )
}
