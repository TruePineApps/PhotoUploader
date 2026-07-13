package com.truepineapps.photouploader.core.util

/**
 * JVM (Desktop) specific implementation of PlatformInfo.
 */
object JvmPlatformInfo : PlatformInfo {
    override val name: String =
        "${System.getProperty("os.name")}, Java ${System.getProperty("java.version")}"
    override val platformType: PlatformType = PlatformType.JVM

    /**
     * For JVM (Desktop):
     * - Checks the environment variable PHOTO_UPLOADER_DEBUG.
     * - Set this variable in the Environment variables field of the Android Studio Run Configuration:
     *   `PHOTO_UPLOADER_DEBUG=true`
     * - Defaults to false if the property is not set or not "true".
     */
    override val isDebugBuild: Boolean = System.getenv("PHOTO_UPLOADER_DEBUG") == "true"
}

