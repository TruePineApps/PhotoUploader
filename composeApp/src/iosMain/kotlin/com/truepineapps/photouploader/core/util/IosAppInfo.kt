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

import platform.Foundation.NSBundle

/**
 * Implementation of the shared AppInfo interface for iOS.
 *
 * This class retrieves application metadata directly from the `NSBundle.mainBundle.infoDictionary`.
 * These values are populated by the `iosApp/Configuration/Config.xcconfig` file, which is
 * automatically kept in sync with the project's Gradle configuration via the `syncIosVersion`
 * Gradle task.
 *
 * Xcode Build Setting Mapping:
 * When 'Generate Info.plist File' is enabled in Xcode, the following mappings apply:
 * - MARKETING_VERSION -> CFBundleShortVersionString (versionName)
 * - CURRENT_PROJECT_VERSION -> CFBundleVersion (buildNumber)
 * - PRODUCT_NAME -> CFBundleName (appLabel)
 * - CFG_APP_MAJOR -> AppMajor (appMajor)
 * - CFG_APP_STAGE -> AppStage (appStage)
 *
 * Relationship:
 * Gradle (`libs.versions.toml`) -> `syncIosVersion` task -> `Config.xcconfig` -> `Info.plist` -> `IosAppInfo`
 */
object IosAppInfo : AppInfo {
    private val infoDictionary = NSBundle.mainBundle.infoDictionary

    init {
        if (infoDictionary == null) {
            error("Info.plist not found or invalid.")
        }
    }

    override val appId: String =
        NSBundle.mainBundle.bundleIdentifier ?: error("Bundle identifier not found in Info.plist.")

    override val appName: String =
        (infoDictionary?.get(AppInfo.IOS_KEY_APP_NAME) as? String) ?: AppInfo.DEFAULT_APP_NAME

    override val appLabel: String =
        (infoDictionary?.get(AppInfo.IOS_KEY_DISPLAY_NAME) as? String)
            ?: (infoDictionary?.get(AppInfo.IOS_KEY_BUNDLE_NAME) as? String)
            ?: AppInfo.DEFAULT_APP_NAME

    override val appMajor: String =
        (infoDictionary?.get(AppInfo.IOS_KEY_APP_MAJOR) as? String) ?: ""

    override val appStage: String =
        (infoDictionary?.get(AppInfo.IOS_KEY_APP_STAGE) as? String) ?: ""

    override val versionName: String =
        (infoDictionary?.get(AppInfo.IOS_KEY_VERSION_NAME) as? String)
            ?: error("Version name (${AppInfo.IOS_KEY_VERSION_NAME}) not found or invalid in Info.plist.")

    override val targetInfo: UiText = UiTextString("iOS Native")
}
