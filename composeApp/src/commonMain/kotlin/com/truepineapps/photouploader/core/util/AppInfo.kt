package com.truepineapps.photouploader.core.util

interface AppInfo {
    val appId: String
    val versionName: String
    val versionCode: String
    /**
     * Information about what the app was built against, e.g. "Target SDK 37" or "JVM 11".
     */
    val targetInfo: UiText
}

