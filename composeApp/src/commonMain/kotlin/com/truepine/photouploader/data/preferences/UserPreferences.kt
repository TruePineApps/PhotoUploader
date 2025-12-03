/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepine.photouploader.data.preferences

/* Default: Get the locale from the platform */
const val DEFAULT_LOCALE_FROM_PLATFORM = "System"
// Default: If no locale is set and the platform doesn't provide one, use English
const val DEFAULT_LOCALE = "en"

data class UserPreferences(
    /** Locale for language, default to English */
    val localeTag: String = DEFAULT_LOCALE_FROM_PLATFORM,
) {
    companion object {
        val DEFAULTS = UserPreferences()
    }
}
