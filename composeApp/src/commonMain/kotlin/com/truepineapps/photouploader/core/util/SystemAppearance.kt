/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
