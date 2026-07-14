package com.truepineapps.photouploader.core.localization

/**
 * Manages the application-wide preferred locale.
 */
interface PlatformLocaleManager :
    PlatformLocaleProvider { // Extends LocaleProvider for convenience
    /**
     * Platform-specific function to set the chosen locale tag to the application's environment.
     * Passing null will result in using the system's default locale.
     * Passing an invalid locale tag will not change the current locale of the system.
     * On Android, this would call AppCompatDelegate.setApplicationLocales().
     * On Web, this might involve a page reload or other framework-specific actions.
     * On other platforms, it might set the default locale or do nothing if not applicable.
     * The actual implementation is injected in Koin
     *
     * @param localeTag The BCP 47 language tag  (e.g., "en-US", "fr-FR") to apply. If null, it
     *                  implies that the application should revert to using the system's default
     *                  locale. An invalid tag will be ignored.
     */
    fun setPlatformLocale(localeTag: String?)
}