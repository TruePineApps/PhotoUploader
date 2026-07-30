package com.truepineapps.photouploader.core.util

import platform.UIKit.UIDevice
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * iOS-specific implementation of PlatformInfo.
 */
object IosPlatformInfo : PlatformInfo {
    override val name: UiText =
        UiTextString(UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion)
    override val osName: String? = null // Redundant with name
    override val osVersion: String? = null // Redundant with name
    override val kernelVersion: String? = null

    @OptIn(ExperimentalNativeApi::class)
    override val cpuArch: UiText = UiTextString(Platform.cpuArchitecture.name)

    @OptIn(ExperimentalNativeApi::class)
    override val availableProcessors: UiText =
        UiTextString(Platform.getAvailableProcessors().toString())
    override val maxMemory: UiText? = null

    override val runtimeName: String = "Kotlin/Native"
    override val runtimeVersion: UiText = UiTextString(KotlinVersion.CURRENT.toString())
    override val platformType: PlatformType = PlatformType.NATIVE

    /* For iOS, `isDebugBuild` checks if the binary was compiled in debug mode. */
    @OptIn(ExperimentalNativeApi::class)
    override val isDebugBuild: Boolean = Platform.isDebugBinary

}
