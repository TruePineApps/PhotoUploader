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

package com.truepineapps.photouploader.app

import android.app.Application
import com.truepineapps.photouploader.app.di.initKoin
import com.truepineapps.photouploader.core.util.AndroidAppInfo
import com.truepineapps.photouploader.core.util.AndroidPlatformInfo
import com.truepineapps.photouploader.core.util.AppInfo
import com.truepineapps.photouploader.core.util.PlatformInfo
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// Call initKoin to define all singletons that can be injected.
// No need to call exitKoin when the app terminates, since Android OS claims the resources back when
// the process is killed.
class PhotoUploaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(isPickerDefined = false) {
            androidContext(this@PhotoUploaderApplication)

            // Pass Platform module data that depends on BuildConfig
            modules(
                module {
                    single<AppInfo> { AndroidAppInfo }
                    single<PlatformInfo> { AndroidPlatformInfo }
                }
            )
        }
    }
}
