package com.truepineapps.photouploader.core.util

enum class PlatformType {
    JVM, NATIVE,
}

/**
 * Provides information about the current platform and build environment.
 */
interface PlatformInfo {
    /**
     * The name of the current platform:
     * - Android: "Android <version>"
     * - iOS: "iOS <version>"
     * - JVM: "JVM"
     *
     * @return The name of the current platform.
     */
    val name: String

    /**
     * The specific [PlatformType] the application is currently running on.
     * This provides a more formal classification than [name], allowing for
     * platform-specific logic.
     *
     * @return The current [PlatformType].
     */
    val platformType: PlatformType

    /**
     * Indicates if the current build is intended for debugging or development.
     * The exact definition of "debug" can vary by platform.
     * - Android: Typically corresponds to the 'debug' build type.
     * - iOS: Corresponds to binaries compiled with the debug configuration.
     * - JVM: Relies on environment variable.
     *
     * @return True if the current build is in debug mode, false otherwise.
     */
    val isDebugBuild: Boolean
}
