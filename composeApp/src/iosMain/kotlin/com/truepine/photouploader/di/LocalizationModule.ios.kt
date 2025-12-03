package com.truepine.photouploader.di

import com.truepine.photouploader.localization.DateTimeFormatter
import com.truepine.photouploader.localization.IOSDateTimeFormatter
import com.truepine.photouploader.localization.IOSNumberFormatter
import com.truepine.photouploader.localization.IOSPlatformLocaleManager
import com.truepine.photouploader.localization.NumberFormatter
import com.truepine.photouploader.localization.PlatformLocaleManager
import com.truepine.photouploader.localization.PlatformLocaleProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { IOSPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { IOSDateTimeFormatter() }
    single<NumberFormatter> { IOSNumberFormatter() }
}