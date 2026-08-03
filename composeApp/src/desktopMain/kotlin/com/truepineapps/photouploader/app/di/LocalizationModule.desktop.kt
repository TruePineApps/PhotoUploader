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

package com.truepineapps.photouploader.app.di

import com.truepineapps.photouploader.core.localization.DateTimeFormatter
import com.truepineapps.photouploader.core.localization.DesktopDateTimeFormatter
import com.truepineapps.photouploader.core.localization.DesktopNumberFormatter
import com.truepineapps.photouploader.core.localization.DesktopPlatformLocaleManager
import com.truepineapps.photouploader.core.localization.NumberFormatter
import com.truepineapps.photouploader.core.localization.PlatformLocaleManager
import com.truepineapps.photouploader.core.localization.PlatformLocaleProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { DesktopPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { DesktopDateTimeFormatter() }
    single<NumberFormatter> { DesktopNumberFormatter() }
}