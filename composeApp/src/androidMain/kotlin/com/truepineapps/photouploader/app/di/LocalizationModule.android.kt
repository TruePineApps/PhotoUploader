package com.truepineapps.photouploader.app.di

import com.truepineapps.photouploader.core.localization.AndroidDateTimeFormatter
import com.truepineapps.photouploader.core.localization.AndroidNumberFormatter
import com.truepineapps.photouploader.core.localization.AndroidPlatformLocaleManager
import com.truepineapps.photouploader.core.localization.DateTimeFormatter
import com.truepineapps.photouploader.core.localization.NumberFormatter
import com.truepineapps.photouploader.core.localization.PlatformLocaleManager
import com.truepineapps.photouploader.core.localization.PlatformLocaleProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { AndroidPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { AndroidDateTimeFormatter() }
    single<NumberFormatter> { AndroidNumberFormatter() }
}