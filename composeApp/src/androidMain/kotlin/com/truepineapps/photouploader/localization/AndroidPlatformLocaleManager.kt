package com.truepineapps.photouploader.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

@Suppress("ConstantLocale")
// Remember the default Locale on app startup to revert to it if needed
private val defaultLocale = Locale.getDefault()

class AndroidPlatformLocaleManager: PlatformLocaleManager {
    override fun setPlatformLocale(localeTag: String?) {
        try {
            if (localeTag == null) {
                println("Android: Reverting to system locale.")
                Locale.setDefault(defaultLocale)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            } else {
                val localeList = LocaleListCompat.forLanguageTags(localeTag)
                if (localeList.isEmpty) {
                    // If localeTag is invalid, just to reflect the system's current value.
                    println("Android: Not changing system locale.")
                } else {
                    val javaLocale = Locale.forLanguageTag(localeTag)
                    Locale.setDefault(javaLocale)
                    AppCompatDelegate.setApplicationLocales(localeList)
                    println("Android: Successfully set $javaLocale")
                }
            }
        } catch (e: Exception) {
            // Log error or handle invalid tag, do not change the current locale
            println("Invalid localeTag: $localeTag, not changing system locale. Error: ${e.message}")
        }
    }

    override fun getPlatformLocaleTag(): String? = Locale.getDefault().toLanguageTag()
}