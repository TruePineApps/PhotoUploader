package com.truepine.photouploader

import android.app.Application
import com.truepine.photouploader.di.initKoin

class PhotoUploaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
