package com.truepineapps.photouploader

import android.app.Application
import com.truepineapps.photouploader.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class PhotoUploaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(isPickerDefined = false) {
            androidContext(this@PhotoUploaderApplication)

            // Pass Platform module data that depends on BuildConfig
            modules(
                module {
                    single<AppInfo> { AndroidAppInfo }
                    single<PlatformInfo> { AndroidPlatformInfo }
                }
            )
        }
    }
}
