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

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import co.touchlab.kermit.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

@Suppress("ConstantLocale")
// Remember the default Locale on app startup to revert to it if needed
private val defaultLocale = Locale.getDefault()

class AndroidPlatformLocaleManager: PlatformLocaleManager, KoinComponent {
    private val log: Logger by inject()
    override fun setPlatformLocale(localeTag: String?) {
        try {
            if (localeTag == null) {
                log.d { "Android: Reverting to system locale." }
                Locale.setDefault(defaultLocale)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            } else {
                val localeList = LocaleListCompat.forLanguageTags(localeTag)
                if (localeList.isEmpty) {
                    // If localeTag is invalid, just to reflect the system's current value.
                    log.d { "Android: Not changing system locale." }
                } else {
                    val javaLocale = Locale.forLanguageTag(localeTag)
                    Locale.setDefault(javaLocale)
                    AppCompatDelegate.setApplicationLocales(localeList)
                    log.d { "Android: Successfully set $javaLocale" }
                }
            }
        } catch (e: Exception) {
            // Log error or handle invalid tag, do not change the current locale
            log.e(e) { "Invalid localeTag: $localeTag, not changing system locale." }
        }
    }

    override fun getPlatformLocaleTag(): String? = Locale.getDefault().toLanguageTag()
}