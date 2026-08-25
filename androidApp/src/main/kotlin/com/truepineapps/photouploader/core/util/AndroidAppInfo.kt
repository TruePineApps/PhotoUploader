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

import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.BuildConfig
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.unknown
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Properties

// Implementation of the shared AppInfo interface for Android
// Note: [BuildConfig] is a generated class
object AndroidAppInfo : AppInfo, KoinComponent {
    private val log: Logger by inject()
    private val properties = Properties()

    init {
        try {
            val stream = this::class.java.getResourceAsStream("/build-info.properties")
            if (stream != null) {
                properties.load(stream)
            } else {
                log.e { "AndroidAppInfo: build-info.properties not found in resources" }
            }
        } catch (e: Exception) {
            log.e(e) { "AndroidAppInfo: Error loading build-info.properties" }
        }
    }

    override val appId: String = BuildConfig.APPLICATION_ID
    override val appName: String = properties.getProperty(AppInfo.KEY_APP_NAME) ?: AppInfo.DEFAULT_APP_NAME
    override val appLabel: String = properties.getProperty(AppInfo.KEY_APP_LABEL) ?: AppInfo.DEFAULT_APP_NAME
    override val appMajor: String = properties.getProperty(AppInfo.KEY_APP_MAJOR) ?: ""
    override val appStage: String = properties.getProperty(AppInfo.KEY_APP_STAGE) ?: ""
    override val versionName: String = BuildConfig.VERSION_NAME
    override val targetInfo: UiText = run {
        val target = properties.getProperty(AppInfo.KEY_TARGET_SDK)
        if (target != null) UiTextString("Target SDK $target")
        else UiTextResource(Res.string.unknown)
    }
}
