package com.truepineapps.photouploader

// Implementation of the shared AppInfo interface for iOS
// Note: com.truepineapps.photouploader.BuildConfig is a generated class
object AndroidAppInfo : AppInfo {
    override val appId: String = BuildConfig.APPLICATION_ID
    override val versionName: String = BuildConfig.VERSION_NAME
    override val versionCode: String = BuildConfig.VERSION_CODE.toString()
}