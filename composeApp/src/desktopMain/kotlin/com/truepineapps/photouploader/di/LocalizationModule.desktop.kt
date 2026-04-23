package com.truepineapps.photouploader.di

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