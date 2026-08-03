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

package com.truepineapps.photouploader.core.feature.settings.domain.model

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
