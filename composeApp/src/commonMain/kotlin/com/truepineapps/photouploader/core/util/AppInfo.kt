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

/**
 * Provides metadata about the application, including versioning and labeling information
 * that may vary across platforms or release stages.
 */
interface AppInfo {
    /**
     * The unique application identifier.
     * For non-production stages, this may include a suffix (e.g., `.beta`) to support
     * side-by-side installation.
     */
    val appId: String

    /**
     * The stable application name (e.g., "PhotoUploader").
     */
    val appName: String

    /**
     * The user-facing application label.
     * Includes the release stage if applicable (e.g., "PhotoUploader Beta").
     */
    val appLabel: String

    /**
     * The human-maintained major version number.
     */
    val appMajor: String

    /**
     * The human-maintained release stage (e.g., "Beta 1").
     */
    val appStage: String

    /**
     * The technical version name, typically in the format `major.minor.build`.
     * This numeric format is used for platform-level version tracking and compatibility,
     * and provides full traceability back to the automated build.
     */
    val versionName: String

    /**
     * Information about what the app was built against, e.g. "Target SDK 37" or "JVM 11".
     */
    val targetInfo: UiText

    companion object {
        // Property Keys (matching BuildConstants in buildSrc)
        const val KEY_APP_ID = "app_id"
        const val KEY_APP_NAME = "app_name"
        const val KEY_APP_LABEL = "app_label"
        const val KEY_APP_MAJOR = "app_major"
        const val KEY_APP_STAGE = "app_stage"
        const val KEY_VERSION_NAME = "version_name"
        const val KEY_TARGET_SDK = "target_sdk"
        const val KEY_JVM_TARGET = "jvm_target"

        // iOS Bundle Keys (mapped from BuildConstants via Info.plist)
        const val IOS_KEY_APP_NAME = "AppName"
        const val IOS_KEY_APP_MAJOR = "AppMajor"
        const val IOS_KEY_APP_STAGE = "AppStage"
        const val IOS_KEY_DISPLAY_NAME = "CFBundleDisplayName"
        const val IOS_KEY_BUNDLE_NAME = "CFBundleName"
        const val IOS_KEY_VERSION_NAME = "CFBundleShortVersionString"

        // Default / Fallback Values
        const val DEFAULT_APP_NAME = "PhotoUploader"
        const val DEFAULT_VERSION_NAME = "0.0.0"
        const val DEFAULT_APP_ID = "com.truepineapps.photouploader"
    }
}
