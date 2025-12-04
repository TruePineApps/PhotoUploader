package com.truepineapps.photouploader.di

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.module.Module
import org.koin.dsl.module

actual val settingsModule: Module = module {
    single<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(get()) }
    single<Settings> { SharedPreferencesSettings(get()) }
}