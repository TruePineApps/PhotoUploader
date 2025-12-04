package com.truepineapps.photouploader.di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.prefs.Preferences

actual val settingsModule: Module = module {
    single<Preferences> { Preferences.userRoot() }
    single<Settings> { PreferencesSettings(get()) }
}