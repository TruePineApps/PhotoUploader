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

package com.truepineapps.photouploader.core.presentation.design

import androidx.compose.ui.graphics.Color

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

    // To convert a primary color to a container color using Color.copy(alpha = CONTAINER)
    CONTAINER(0.1f),
    DISABLED(0.38f), // See also IconButtonTokens.DisabledIconOpacity
    FULL(1f)
}

fun Color.toEnabled(isEnabled: Boolean) = if (isEnabled) this else this.copy(alpha = Opacity.DISABLED.value)
fun Color.toContainer() = this.copy(alpha = Opacity.CONTAINER.value)