package com.truepineapps.photouploader.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class JvmPlatformInfoTest {

    private class MockJvmEnvironment(
        private val properties: Map<String, String> = emptyMap(),
        private val envVars: Map<String, String> = emptyMap(),
        private val osRelease: Map<String, String> = emptyMap(),
        private val processors: Int = 1,
        private val maxMemory: Long = Long.MAX_VALUE
    ) : JvmEnvironment {
        override fun getProperty(key: String): String? = properties[key]
        override fun getEnv(key: String): String? = envVars[key]
        override fun getAvailableProcessors(): Int = processors
        override fun getMaxMemory(): Long = maxMemory
        override fun readOsRelease(): Map<String, String> = osRelease
    }

    @Test
    fun `Ubuntu initialization resolves correctly`() {
        val sysOsName = "Linux"
        val kernelVersion = "7.0.0-28-generic"
        val arch = "amd64"
        val javaVer = "21.0.11"
        val distroName = "Ubuntu"
        val distroVer = "24.04"
        val prettyName = "$distroName $distroVer.4 LTS"
        val processors = 16
        val maxMemoryBytes = 4_000_000_000L // ~3.7 GB

        verifyPlatformInfo(
            mockEnv = MockJvmEnvironment(
                properties = mapOf(
                    "os.name" to sysOsName,
                    "os.version" to kernelVersion,
                    "os.arch" to arch,
                    "java.version" to javaVer
                ),
                envVars = mapOf("PHOTO_UPLOADER_DEBUG" to "true"),
                osRelease = mapOf(
                    "PRETTY_NAME" to prettyName,
                    "NAME" to distroName,
                    "VERSION_ID" to distroVer
                ),
                processors = processors,
                maxMemory = maxMemoryBytes
            ),
            expectedName = prettyName,
            expectedOsName = sysOsName,
            expectedOsVersion = null,
            expectedKernelVersion = kernelVersion,
            expectedCpuArch = arch,
            expectedProcessors = processors.toString(),
            expectedMaxMemory = "3.7 GB",
            expectedRuntimeVersion = javaVer,
            expectedIsDebug = true
        )
    }

    @Test
    fun `Generic Linux without os-release uses system properties`() {
        val sysOsName = "Linux"
        val sysOsVersion = "6.1.0"
        val arch = "x86_64"
        val javaVer = "17.0.2"
        val processors = 8
        val maxMemoryBytes = 8_000_000_000L // ~7.5 GB

        verifyPlatformInfo(
            mockEnv = MockJvmEnvironment(
                properties = mapOf(
                    "os.name" to sysOsName,
                    "os.version" to sysOsVersion,
                    "os.arch" to arch,
                    "java.version" to javaVer
                ),
                processors = processors,
                maxMemory = maxMemoryBytes
            ),
            expectedName = "$sysOsName $sysOsVersion",
            expectedOsName = sysOsName,
            expectedKernelVersion = sysOsVersion,
            expectedCpuArch = arch,
            expectedProcessors = processors.toString(),
            expectedMaxMemory = "7.5 GB",
            expectedRuntimeVersion = javaVer
        )
    }

    @Test
    fun `Windows initialization uses system properties`() {
        val sysOsName = "Windows 11"
        val sysOsVersion = "10.0"
        val arch = "amd64"
        val javaVer = "11.0.12"
        val processors = 4
        val maxMemoryBytes = 2_000_000_000L // ~1.9 GB

        verifyPlatformInfo(
            mockEnv = MockJvmEnvironment(
                properties = mapOf(
                    "os.name" to sysOsName,
                    "os.version" to sysOsVersion,
                    "os.arch" to arch,
                    "java.version" to javaVer
                ),
                processors = processors,
                maxMemory = maxMemoryBytes
            ),
            expectedName = "$sysOsName $sysOsVersion",
            expectedCpuArch = arch,
            expectedProcessors = processors.toString(),
            expectedMaxMemory = "1.9 GB",
            expectedRuntimeVersion = javaVer
        )
    }

    private fun verifyPlatformInfo(
        mockEnv: JvmEnvironment,
        expectedName: String,
        expectedOsName: String? = null,
        expectedOsVersion: String? = null,
        expectedKernelVersion: String? = null,
        expectedCpuArch: String = "ResourceText",
        expectedProcessors: String = "1",
        expectedMaxMemory: String = "ResourceText",
        expectedRuntimeName: String = "Java",
        expectedRuntimeVersion: String = "ResourceText",
        expectedPlatformType: PlatformType = PlatformType.JVM,
        expectedIsDebug: Boolean = false
    ) {
        val info = JvmPlatformInfo(mockEnv)

        assertEquals(expectedName, info.name.asStringForTest(), "name")
        assertEquals(expectedOsName, info.osName, "osName")
        assertEquals(expectedOsVersion, info.osVersion, "osVersion")
        assertEquals(expectedKernelVersion, info.kernelVersion, "kernelVersion")
        assertEquals(expectedCpuArch, info.cpuArch.asStringForTest(), "cpuArch")
        assertEquals(expectedProcessors, info.availableProcessors.asStringForTest(), "availableProcessors")
        assertEquals(expectedMaxMemory, info.maxMemory.asStringForTest(), "maxMemory")
        assertEquals(expectedRuntimeName, info.runtimeName, "runtimeName")
        assertEquals(expectedRuntimeVersion, info.runtimeVersion.asStringForTest(), "runtimeVersion")
        assertEquals(expectedPlatformType, info.platformType, "platformType")
        assertEquals(expectedIsDebug, info.isDebugBuild, "isDebugBuild")
    }

    /**
     * Helper to simplify UiText assertions in unit tests.
     * Note: This assumes UiTextString implementations for the properties being tested.
     */
    private fun UiText?.asStringForTest(): String? {
        if (this == null) return null
        return when (this) {
            is UiTextString -> toString()
            else -> "ResourceText"
        }
    }
}
