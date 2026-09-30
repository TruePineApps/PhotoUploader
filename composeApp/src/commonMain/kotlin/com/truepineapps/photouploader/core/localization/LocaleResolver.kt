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

package com.truepineapps.photouploader.core.localization

import com.truepineapps.photouploader.core.feature.settings.domain.model.DEFAULT_LOCALE_FROM_PLATFORM
import com.truepineapps.photouploader.core.feature.settings.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Resolves the active locale and constructs localized file names or URLs.
 */
class LocaleResolver(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val localeProvider: PlatformLocaleProvider,
) {
    /**
     * Resolves the current locale tag based on user preferences and platform settings.
     * @return The language code (e.g., "nl") or null if not resolvable.
     */
    suspend fun current(): String? {
        val preferences = userPreferencesRepository.preferences.first()
        val tag = if (preferences.localeTag == DEFAULT_LOCALE_FROM_PLATFORM) {
            localeProvider.getPlatformLocaleTag()
        } else {
            preferences.localeTag
        }
        return tag?.substringBefore("-")?.lowercase()
    }

    /**
     * Returns the localized URL by replacing the '.md' extension with '.<locale>.md' if applicable.
     */
    suspend fun getLocalizedUrl(baseUrl: String): String {
        val locale = current()
        return if (locale != null && locale != "en") {
            baseUrl.replace(Regex("\\.md$", RegexOption.IGNORE_CASE), ".$locale.md")
        } else {
            baseUrl
        }
    }

    /**
     * Returns the localized file name by appending '.<locale>' before the extension if applicable.
     */
    suspend fun getLocalizedName(name: String, extension: String): String {
        val locale = current()
        return if (locale == null || locale == "en") {
            "$name$extension"
        } else {
            "$name.$locale$extension"
        }
    }
}
