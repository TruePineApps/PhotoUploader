package com.truepineapps.photouploader.localization

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