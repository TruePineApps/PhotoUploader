package com.truepine.photouploader.di

import com.truepine.photouploader.localization.DateTimeFormatter
import com.truepine.photouploader.localization.DesktopDateTimeFormatter
import com.truepine.photouploader.localization.DesktopNumberFormatter
import com.truepine.photouploader.localization.DesktopPlatformLocaleManager
import com.truepine.photouploader.localization.NumberFormatter
import com.truepine.photouploader.localization.PlatformLocaleManager
import com.truepine.photouploader.localization.PlatformLocaleProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { DesktopPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { DesktopDateTimeFormatter() }
    single<NumberFormatter> { DesktopNumberFormatter() }
}