/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.core.presentation.design

/**
 * Standard opacity states.
 * Predefined float values for common states like transparent, disabled, and fully opaque.
 * Example: modifier = Modifier.alpha(if (isEnabled) Opacity.FULL.value else Opacity.DISABLED.value)
 *
 * @property value The float representation of the opacity, ranging from 0.0f (completely transparent)
 * to 1.0f (completely opaque).
 */
enum class Opacity(val value: Float) {
    TRANSPARENT(0f),
    DISABLED(0.38f), // See also IconButtonTokens.DisabledIconOpacity
    FULL(1f)
}
