package com.truepineapps.photouploader.di

import com.truepineapps.photouploader.core.localization.AndroidDateTimeFormatter
import com.truepineapps.photouploader.core.localization.AndroidNumberFormatter
import com.truepineapps.photouploader.core.localization.AndroidPlatformLocaleManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<com.truepineapps.photouploader.core.localization.PlatformLocaleManager> { AndroidPlatformLocaleManager() }
    single<com.truepineapps.photouploader.core.localization.PlatformLocaleProvider> { get<com.truepineapps.photouploader.core.localization.PlatformLocaleManager>() } // Use the same instance

    single<com.truepineapps.photouploader.core.localization.DateTimeFormatter> { AndroidDateTimeFormatter() }
    single<com.truepineapps.photouploader.core.localization.NumberFormatter> { AndroidNumberFormatter() }
}