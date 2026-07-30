package com.truepineapps.photouploader.core.util

enum class PlatformType {
    JVM, NATIVE,
}

/**
 * Provides information about the current platform and build environment.
 */
interface PlatformInfo {
    /**
     * A user-friendly name of the current platform, e.g. "Android 14 (API 34)" or "Ubuntu 24.04".
     * Set to resource id for Unknown if no name could be found in the system properties
     */
    val name: UiText

    /**
     * The name of the operating system, e.g. "Android", "Linux", "macOS", "Windows", "iOS".
     */
    val osName: String?

    /**
     * The version of the operating system, e.g. "14", "24.04", "17.0".
     */
    val osVersion: String?

    /**
     * The specific kernel or technical version of the system, e.g. "6.8.0-generic".
     */
    val kernelVersion: String?

    /**
     * The CPU architecture of the system, e.g. "x86_64", "aarch64".
     */
    val cpuArch: UiText

    /**
     * The number of available processors to the JVM.
     */
    val availableProcessors: UiText

    /**
     * The maximum amount of memory the JVM will attempt to use.
     */
    val maxMemory: UiText?

    /**
     * The name of the runtime environment, e.g. "ART", "Java", "Kotlin/Native".
     */
    val runtimeName: String

    /**
     * The technical version of the runtime, e.g. "11.0.2", "2.1.0".
     */
    val runtimeVersion: UiText

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
