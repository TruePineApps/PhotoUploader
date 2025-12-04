package com.truepineapps.photouploader.localization

import java.util.Locale

// Remember the default Locale on app startup to revert to it if needed
@Suppress("ConstantLocale")
private val defaultLocale = Locale.getDefault()

class DesktopPlatformLocaleManager: PlatformLocaleManager {
    override fun setPlatformLocale(localeTag: String?) {
        try {
            val newLocale = if (localeTag == null) {
                println("Desktop: Reverting to system locale.")
                defaultLocale
            } else {
                Locale.forLanguageTag(localeTag)
            }

            if (newLocale == null) {
                // If localeTag is invalid, just to reflect the system's current value.
                println("Desktop: Not changing system locale.")
            } else {
                Locale.setDefault(newLocale)
                println("Desktop: JVM default locale set to: ${newLocale.toLanguageTag()}")
            }
        } catch (e: Exception) {
            println("Error setting JVM default locale, not changing system locale. Error: ${e.message}")
        }
    }

    override fun getPlatformLocaleTag(): String? = Locale.getDefault().toLanguageTag()
}