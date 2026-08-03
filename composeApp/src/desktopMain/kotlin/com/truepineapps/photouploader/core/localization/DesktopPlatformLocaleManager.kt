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

import co.touchlab.kermit.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

// Remember the default Locale on app startup to revert to it if needed
@Suppress("ConstantLocale")
private val defaultLocale = Locale.getDefault()

class DesktopPlatformLocaleManager: PlatformLocaleManager, KoinComponent {
    private val log: Logger by inject()
    override fun setPlatformLocale(localeTag: String?) {
        try {
            val newLocale = if (localeTag == null) {
                log.d { "Desktop: Reverting to system locale." }
                defaultLocale
            } else {
                Locale.forLanguageTag(localeTag)
            }

            if (newLocale == null) {
                // If localeTag is invalid, just reflect the system's current value.
                log.d { "Desktop: Not changing system locale (newLocale is null)." }
            } else {
                Locale.setDefault(newLocale)
                log.d { "Desktop: JVM default locale successfully set to: ${newLocale.toLanguageTag()}" }
            }
        } catch (e: Exception) {
            log.e(e) { "Error setting JVM default locale to '$localeTag'" }
        }
    }

    override fun getPlatformLocaleTag(): String? = Locale.getDefault().toLanguageTag()
}