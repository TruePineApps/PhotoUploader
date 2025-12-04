package com.truepineapps.photouploader.di

import com.truepineapps.photouploader.AppInfo
import com.truepineapps.photouploader.JvmAppInfo
import com.truepineapps.photouploader.JvmPlatformInfo
import com.truepineapps.photouploader.PlatformInfo
import com.truepineapps.photouploader.auth.DesktopGoogleAuthService
import com.truepineapps.photouploader.auth.GoogleAuthService
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<GoogleAuthService> { DesktopGoogleAuthService() }
    single<AppInfo> { JvmAppInfo }
    single<PlatformInfo> { JvmPlatformInfo }
}
