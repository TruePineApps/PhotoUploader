package com.truepineapps.photouploader.core.util

import platform.Foundation.NSBundle

// Implementation of the shared AppInfo interface for iOS
object IosAppInfo : AppInfo {
    private val infoDictionary = NSBundle.mainBundle.infoDictionary

    init {
        if (infoDictionary == null) {
            error("Info.plist not found or invalid.")
        }
    }

    override val appId: String =
            NSBundle.mainBundle.bundleIdentifier ?: error("Bundle identifier not found in Info.plist.")

    override val versionName: String =
            (infoDictionary!!["CFBundleShortVersionString"] as? String)
                ?: error("Version name (CFBundleShortVersionString) not found or invalid in Info.plist.")

    override val versionCode: String =
            (infoDictionary!!["CFBundleVersion"] as? String)
                ?: error("Version code (CFBundleVersion) not found or invalid in Info.plist.")
}