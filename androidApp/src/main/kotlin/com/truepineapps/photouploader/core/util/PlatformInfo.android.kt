package com.truepineapps.photouploader.core.util

import android.os.Build
import com.truepineapps.photouploader.BuildConfig
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.unknown


/**
 * Android-specific implementation of PlatformInfo.
 */
object AndroidPlatformInfo : PlatformInfo {
    private const val OS_VERSION_PROP = "os.version"
    private const val OS_ARCH_PROP = "os.arch"
    private const val JAVA_VM_VERSION_PROP = "java.vm.version"

    private const val BYTES_IN_MB = 1024.0 * 1024.0

    override val name: UiText =
        UiTextString("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    override val osName: String? = null // Redundant with name
    override val osVersion: String? = null // Redundant with name
    override val kernelVersion: String? = System.getProperty(OS_VERSION_PROP)

    override val cpuArch: UiText = (Build.SUPPORTED_ABIS?.firstOrNull() ?: System.getProperty(OS_ARCH_PROP))
        ?.let { UiTextString(it) }

    ?: UiTextResource(Res.string.unknown)


    override val availableProcessors: UiText =
        UiTextString(Runtime.getRuntime().availableProcessors().toString())

    override val maxMemory: UiText = run {
        val maxMemoryBytes = Runtime.getRuntime().maxMemory()
        if (maxMemoryBytes == Long.MAX_VALUE) UiTextResource(Res.string.unknown)
        else {
            val maxMemoryMb = maxMemoryBytes / BYTES_IN_MB
            UiTextString("${"%.0f".format(maxMemoryMb)} MB")
        }
    }

    override val runtimeName: String = "ART"
    override val runtimeVersion: UiText = System.getProperty(JAVA_VM_VERSION_PROP)?.let { UiTextString(it) }
        ?: UiTextResource(Res.string.unknown)
    override val platformType: PlatformType = PlatformType.NATIVE

    /* For Android, `isDebugBuild` is true if the app was built with the 'debug' build type. */
    override val isDebugBuild: Boolean = BuildConfig.DEBUG

}
