package com.truepineapps.photouploader.app.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val settingsModule: Module = module {
    single<NSUserDefaults> { NSUserDefaults.standardUserDefaults }
    single<Settings> { NSUserDefaultsSettings(get()) }
}
