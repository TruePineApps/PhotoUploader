package com.truepineapps.photouploader.core.util

import androidx.compose.runtime.Composable

enum class ContrastLevel {
    Standard,
    Medium,
    High
}

/**
 * Returns the current system-wide contrast level.
 * This is a reactive composable that updates when system settings change.
 */
@Composable
expect fun getSystemContrastLevel(): ContrastLevel

/**
 * Returns whether the active High Contrast theme should be treated as "Dark".
 *
 * @return true/false to force a specific mode, or null to defer to standard
 * dark mode detection (used when settings are independent or HC is off).
 */
@Composable
expect fun isHighContrastDark(): Boolean?
