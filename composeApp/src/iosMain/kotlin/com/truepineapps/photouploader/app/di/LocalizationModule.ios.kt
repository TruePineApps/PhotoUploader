package com.truepineapps.photouploader.app.di

import com.truepineapps.photouploader.core.localization.DateTimeFormatter
import com.truepineapps.photouploader.core.localization.IOSDateTimeFormatter
import com.truepineapps.photouploader.core.localization.IOSNumberFormatter
import com.truepineapps.photouploader.core.localization.IOSPlatformLocaleManager
import com.truepineapps.photouploader.core.localization.NumberFormatter
import com.truepineapps.photouploader.core.localization.PlatformLocaleManager
import com.truepineapps.photouploader.core.localization.PlatformLocaleProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { IOSPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { IOSDateTimeFormatter() }
    single<NumberFormatter> { IOSNumberFormatter() }
}