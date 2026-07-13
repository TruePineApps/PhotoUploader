package com.truepineapps.photouploader.core.util

import androidx.compose.runtime.Composable

enum class ContrastLevel {
    Standard,
    Medium,
    High
}

@Composable
expect fun getSystemContrastLevel(): ContrastLevel
