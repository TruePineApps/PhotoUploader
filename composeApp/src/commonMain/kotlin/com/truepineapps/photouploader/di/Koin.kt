package com.truepineapps.photouploader.di

import com.russhwolf.settings.Settings
import com.truepineapps.photouploader.data.preferences.UserPreferencesRepository
import com.truepineapps.photouploader.data.preferences.UserPreferencesSettingsRepository
import com.truepineapps.photouploader.localization.DateTimeFormatter
import com.truepineapps.photouploader.localization.NumberFormatter
import com.truepineapps.photouploader.localization.PlatformLocaleManager
import com.truepineapps.photouploader.localization.PlatformLocaleProvider
import com.truepineapps.photouploader.ui.components.PlatformPicker.CalfPlatformPicker
import com.truepineapps.photouploader.ui.components.PlatformPicker.PlatformPicker
import com.truepineapps.photouploader.ui.localization.LocaleViewModel
import com.truepineapps.photouploader.ui.screen.settings.SettingsViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.ksp.generated.module
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
        contextDeclaration()

        // Load the common AppModule, the platform-specific modules and the core module
        modules(
            AppModule().module,
            viewModelModule(),
            platformModule(),
            // Localization info: locale handling; date, time and number formatting
            // Platform dependent, partly in Kotlin
            platformLocalizationModule,
            // Settings storage
            // Platform dependent, in Kotlin
            settingsModule,
            // Common core modules
            // Platform independent, in Kotlin
            coreModule(isPickerDefined)
        )
    }
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
    single { LocaleViewModel(userPreferencesRepository = get()) }
    single { SettingsViewModel(userPreferencesRepository = get()) }

    single<Clock> { Clock.System }
}
