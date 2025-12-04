package com.truepineapps.photouploader

import android.app.Application
import com.truepineapps.photouploader.di.initKoin
import org.koin.android.ext.koin.androidContext

class PhotoUploaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(isPickerDefined = false) {
            androidContext(this@PhotoUploaderApp)
        }
    }
}
