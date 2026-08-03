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

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

private const val LANG_KEY = "AppleLanguages"

class IOSPlatformLocaleManager: PlatformLocaleManager {
    override fun setPlatformLocale(localeTag: String?) {
        try {
            if (localeTag == null) {
                NSUserDefaults.standardUserDefaults.removeObjectForKey(LANG_KEY)
                println("iOS: Locale reset to default")
            } else {
                NSUserDefaults.standardUserDefaults.setObject(arrayListOf(localeTag), LANG_KEY)
                println("iOS: Locale set to: $localeTag.")
            }
        } catch (e: Exception) {
            println("Error setting iOS default locale, not changing system locale. Error: ${e.message}")
        }
    }

    override fun getPlatformLocaleTag(): String? = NSLocale.preferredLanguages.firstOrNull() as? String?
}