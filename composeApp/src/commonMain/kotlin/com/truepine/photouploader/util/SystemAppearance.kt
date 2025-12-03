package com.truepine.photouploader.util

import androidx.compose.runtime.Composable

// composeApp/src/commonMain/kotlin/.../util/SystemAppearance.kt

enum class ContrastLevel {
    Standard,
    Medium,
    High}

@Composable
expect fun getSystemContrastLevel(): ContrastLevel
