package com.truepineapps.photouploader.localization

/**
 * Provides the currently active locale tag for the application.
 * Other services can depend on this to get the locale for their operations.
 */
interface PlatformLocaleProvider {
    /**
     * Platform-specific function to get the current system's locale tag (e.g., "en-US", "fr-CA").
     * This function will be implemented by each platform (actual implementation injected in Koin).
     *
     * @return The IETF BCP 47 language tag (e.g., "en-US") of the currently active locale.
     */
    fun getPlatformLocaleTag(): String?
}