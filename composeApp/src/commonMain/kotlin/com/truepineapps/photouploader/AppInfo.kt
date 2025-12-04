package com.truepineapps.photouploader

interface AppInfo {
    val appId: String
    val versionName: String
    val versionCode: String
}

class PlatformAppInfo(
    override val appId: String,
    override val versionName: String,
    override val versionCode: String
) : AppInfo