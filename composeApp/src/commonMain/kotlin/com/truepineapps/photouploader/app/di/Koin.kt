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

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.russhwolf.settings.Settings
import com.truepineapps.photouploader.core.feature.legal.di.legalModule
import com.truepineapps.photouploader.core.feature.legal.viewmodel.LicenseViewModel
import com.truepineapps.photouploader.core.feature.moremenu.di.moreMenuModule
import com.truepineapps.photouploader.core.feature.settings.data.repository.UserPreferencesSettingsRepository
import com.truepineapps.photouploader.core.feature.settings.domain.repository.UserPreferencesRepository
import com.truepineapps.photouploader.core.feature.settings.viewmodel.LocaleViewModel
import com.truepineapps.photouploader.core.feature.settings.viewmodel.SettingsViewModel
import com.truepineapps.photouploader.core.localization.DateTimeFormatter
import com.truepineapps.photouploader.core.localization.NumberFormatter
import com.truepineapps.photouploader.core.localization.PlatformLocaleManager
import com.truepineapps.photouploader.core.localization.PlatformLocaleProvider
import com.truepineapps.photouploader.core.presentation.component.platformpicker.CalfPlatformPicker
import com.truepineapps.photouploader.core.presentation.component.platformpicker.PlatformPicker
import com.truepineapps.photouploader.feature.uploader.viewmodel.PhotoUploaderViewModel
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Initializes the Koin dependency injection framework for the application.
 *
 * This function sets up the Koin context, loading the necessary modules including
 * the platform-specific picker implementation, the common application module,
 * and other platform-specific dependencies.
 *
 * pickerModule is set by desktop; it overrides the default DirectoryPicker which does not work.
 *
 * @param isPickerDefined Whether the platform-specific picker implementation is defined in the
 *                      appDeclaration. If false, the core module provides the default
 *                      [CalfPlatformPicker] as a singleton. This can be overridden for testing or
 *                      specific platform needs.
 * @param contextDeclaration The platform context specific Koin initialization that provides
 *                      instances that are specific for a certain platform like the [PlatformPicker]
 *                      implementation or context dependent instances, like the Android Context.
 * @return The Koin application instance.
 */
fun initKoin(
    isPickerDefined: Boolean = false,
    contextDeclaration: KoinAppDeclaration = {},
): KoinApplication {
    return startKoin {
        logger(KermitKoinLogger(Logger.withTag("koin")))
        contextDeclaration()

        // Load the common AppModule, the platform-specific modules and the core module
        modules(
            appModule,
            viewModelModule(),
            platformModule(),
            networkModule,
            // Localization info: locale handling; date, time and number formatting
            // Platform dependent, partly in Kotlin
            platformLocalizationModule,
            // Settings storage
            // Platform dependent, in Kotlin
            settingsModule,
            // Legal consent objects
            legalModule,
            // Debug Actions
            moreMenuModule,
            // Common core modules
            // Platform independent, in Kotlin
            coreModule(isPickerDefined)
        )
    }
}

/**
 * Call stopKoin after cleaning up the viewModel
 */
fun exitKoin(koin: Koin) {
    // ViewModels are complex, because they are also owned by the ViewModelStore. At the close of
    // the app, the onClose might not be called, therefore do it explicitly here.
    val photoUploaderViewModel = koin.getOrNull<PhotoUploaderViewModel>()
    photoUploaderViewModel?.shutdown()

    stopKoin()
}

/**
 * Expected Koin module that provides platform-specific implementations for
 * [PlatformLocaleManager], [DateTimeFormatter], and [NumberFormatter].
 * The [PlatformLocaleProvider] will typically be the same instance as [PlatformLocaleManager].
 */
expect val platformLocalizationModule: Module

/**
 * Expected Koin module that provides platform-specific implementation for the [Settings] storage.
 */
expect val settingsModule: Module

@OptIn(ExperimentalTime::class)
private fun coreModule(isPickerDefined: Boolean) = module {
    if (!isPickerDefined) {
        single<PlatformPicker> { CalfPlatformPicker() }
    }
    single<UserPreferencesRepository> { UserPreferencesSettingsRepository(get()) }
    single { LocaleViewModel(userPreferencesRepository = get(), platformLocaleManager = get(), log = get()) }
    single { SettingsViewModel(userPreferencesRepository = get()) }
    single { LicenseViewModel() }
    single<Clock> { Clock.System }
}
