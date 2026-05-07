package com.truepineapps.photouploader.app

import android.app.Application
import com.truepineapps.photouploader.app.di.initKoin
import com.truepineapps.photouploader.core.util.AndroidAppInfo
import com.truepineapps.photouploader.core.util.AndroidPlatformInfo
import com.truepineapps.photouploader.core.util.AppInfo
import com.truepineapps.photouploader.core.util.PlatformInfo
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// Call initKoin to define all singletons that can be injected.
// No need to call exitKoin when the app terminates, since Android OS claims the resources back when
// the process is killed.
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
