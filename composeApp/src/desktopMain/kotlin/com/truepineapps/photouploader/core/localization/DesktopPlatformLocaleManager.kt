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