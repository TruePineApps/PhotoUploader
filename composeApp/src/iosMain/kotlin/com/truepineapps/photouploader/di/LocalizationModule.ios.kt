package com.truepineapps.photouploader.di

import com.truepineapps.photouploader.localization.DateTimeFormatter
import com.truepineapps.photouploader.localization.IOSDateTimeFormatter
import com.truepineapps.photouploader.localization.IOSNumberFormatter
import com.truepineapps.photouploader.localization.IOSPlatformLocaleManager
import com.truepineapps.photouploader.localization.NumberFormatter
import com.truepineapps.photouploader.localization.PlatformLocaleManager
import com.truepineapps.photouploader.localization.PlatformLocaleProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { IOSPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { IOSDateTimeFormatter() }
    single<NumberFormatter> { IOSNumberFormatter() }
}