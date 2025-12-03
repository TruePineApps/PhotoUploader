package com.truepine.photouploader

import android.os.Build

/**
 * Android-specific implementation of PlatformInfo.
 */
object AndroidPlatformInfo : PlatformInfo {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val platformType: PlatformType = PlatformType.NATIVE

    /* For Android, `isDebugBuild` is true if the app was built with the 'debug' build type. */
    override val isDebugBuild: Boolean = BuildConfig.DEBUG
}
