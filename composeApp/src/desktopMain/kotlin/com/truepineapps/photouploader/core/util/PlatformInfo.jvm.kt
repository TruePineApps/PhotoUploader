package com.truepineapps.photouploader.core.util

/**
 * JVM (Desktop) specific implementation of PlatformInfo.
 */
object JvmPlatformInfo : PlatformInfo {
    override val name: String = "${System.getProperty("os.name")}, Java ${System.getProperty("java.version")}"
    override val platformType: PlatformType = PlatformType.JVM

    /**
     * For JVM (Desktop):
     * - Checks a system property e.g., "debug=true".
     * - Set this property when running your desktop application:
     *   `java -debug=true -jar photo_uploader_app.jar`
     * - Defaults to false if the property is not set or not "true".
     */
    override val isDebugBuild: Boolean = System.getProperty("debug", "false").toBoolean()
}

