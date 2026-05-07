package com.truepineapps.photouploader.core.util

import android.os.Build
import com.truepineapps.photouploader.BuildConfig

/**
 * Android-specific implementation of PlatformInfo.
 */
object AndroidPlatformInfo : PlatformInfo {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val platformType: PlatformType = PlatformType.NATIVE

    /* For Android, `isDebugBuild` is true if the app was built with the 'debug' build type. */
    override val isDebugBuild: Boolean = BuildConfig.DEBUG
}
