/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader.core.util

import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.unknown

/**
 * JVM (Desktop) specific implementation of PlatformInfo.
 */
class JvmPlatformInfo(env: JvmEnvironment) : PlatformInfo {

    override val name: UiText
    override val osName: String?
    override val osVersion: String?
    override val kernelVersion: String?
    override val cpuArch: UiText
    override val availableProcessors: UiText
    override val maxMemory: UiText
    override val runtimeName: String = "Java"
    override val runtimeVersion: UiText
    override val platformType: PlatformType = PlatformType.JVM

    /**
     * For JVM (Desktop):
     * - Checks the environment variable PHOTO_UPLOADER_DEBUG.
     * - Set this variable in the Environment variables field of the Android Studio Run Configuration:
     *   `PHOTO_UPLOADER_DEBUG=true`
     * - Defaults to false if the property is not set or not "true".
     */
    override val isDebugBuild: Boolean = env.getEnv("PHOTO_UPLOADER_DEBUG") == "true"

    init {
        val sysOsName = env.getProperty(OS_NAME_PROP)
        val sysOsVersion = env.getProperty(OS_VERSION_PROP)

        val isLinux = sysOsName?.lowercase()?.contains("linux") == true
        val osReleaseData = if (isLinux) env.readOsRelease() else emptyMap()

        // 1. Resolve Summary Name (Pretty Name)
        val summaryNameStr = if (isLinux) {
            osReleaseData[PRETTY_NAME_KEY] ?: osReleaseData[NAME_KEY]?.let { distro ->
                val ver = osReleaseData[VERSION_KEY] ?: osReleaseData[VERSION_ID_KEY]
                    ?: osReleaseData[ID_KEY] ?: ""
                "$distro $ver"
            } ?: if (sysOsVersion != null) "$sysOsName $sysOsVersion" else sysOsName
        } else {
            if (sysOsName != null && sysOsVersion != null) "$sysOsName $sysOsVersion" else sysOsName
        }
        name = if (summaryNameStr == null) UiTextResource(Res.string.unknown) else UiTextString(
            summaryNameStr
        )

        // 2. Resolve Technical OS Details
        if (isLinux) {
            val distroName = osReleaseData[NAME_KEY] ?: osReleaseData[ID_KEY]
            // If distro is already in summary, return generic "Linux"
            osName = if (distroName != null
                && (summaryNameStr == null || !summaryNameStr.contains(distroName, ignoreCase = true))
            ) {
                distroName
            } else {
                "Linux"
            }

            // If no unique distro version, show technical kernel version
            val distroVersion = osReleaseData[VERSION_ID_KEY] ?: osReleaseData[VERSION_KEY]
            osVersion = if (distroVersion != null
                && (summaryNameStr == null || !summaryNameStr.contains(distroVersion))
            ) {
                distroVersion
            } else {
                null
            }
            kernelVersion = sysOsVersion
        } else {
            osName = null
            osVersion = null
            kernelVersion = null
        }

        // 3. System Metrics
        cpuArch = env.getProperty(OS_ARCH_PROP)?.let { UiTextString(it) }
            ?: UiTextResource(Res.string.unknown)

        availableProcessors = UiTextString(env.getAvailableProcessors().toString())

        val maxMemoryBytes = env.getMaxMemory()
        maxMemory = if (maxMemoryBytes == Long.MAX_VALUE) UiTextResource(Res.string.unknown)
        else {
            val maxMemoryGb = maxMemoryBytes / BYTES_IN_GB
            UiTextString("${"%.1f".format(maxMemoryGb)} GB")
        }

        runtimeVersion = env.getProperty(JAVA_VERSION_PROP)?.let { UiTextString(it) }
            ?: UiTextResource(Res.string.unknown)
    }

    companion object {
        private const val OS_NAME_PROP = "os.name"
        private const val OS_VERSION_PROP = "os.version"
        private const val JAVA_VERSION_PROP = "java.version"
        private const val OS_ARCH_PROP = "os.arch"
        private const val PRETTY_NAME_KEY = "PRETTY_NAME"
        private const val NAME_KEY = "NAME"
        private const val VERSION_KEY = "VERSION"
        private const val VERSION_ID_KEY = "VERSION_ID"
        private const val ID_KEY = "ID"

        private const val BYTES_IN_GB = 1024.0 * 1024.0 * 1024.0
    }
}
