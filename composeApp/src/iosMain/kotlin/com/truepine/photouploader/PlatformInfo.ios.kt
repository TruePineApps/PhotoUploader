package com.truepine.photouploader

import platform.UIKit.UIDevice
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * iOS-specific implementation of PlatformInfo.
 */
object IosPlatformInfo: PlatformInfo {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val platformType: PlatformType = PlatformType.NATIVE

    /* For iOS, `isDebugBuild` checks if the binary was compiled in debug mode. */
    @OptIn(ExperimentalNativeApi::class)
    override val isDebugBuild: Boolean = Platform.isDebugBinary
}

