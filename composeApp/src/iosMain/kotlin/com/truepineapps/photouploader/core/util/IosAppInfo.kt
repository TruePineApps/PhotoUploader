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

    override val targetInfo: UiText = UiTextString("iOS Native")
}
