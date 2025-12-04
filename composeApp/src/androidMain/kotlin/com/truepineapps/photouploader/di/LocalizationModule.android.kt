package com.truepineapps.photouploader.di

import com.truepineapps.photouploader.localization.AndroidDateTimeFormatter
import com.truepineapps.photouploader.localization.AndroidNumberFormatter
import com.truepineapps.photouploader.localization.AndroidPlatformLocaleManager
import com.truepineapps.photouploader.localization.DateTimeFormatter
import com.truepineapps.photouploader.localization.NumberFormatter
import com.truepineapps.photouploader.localization.PlatformLocaleManager
import com.truepineapps.photouploader.localization.PlatformLocaleProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { AndroidPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { AndroidDateTimeFormatter() }
    single<NumberFormatter> { AndroidNumberFormatter() }
}