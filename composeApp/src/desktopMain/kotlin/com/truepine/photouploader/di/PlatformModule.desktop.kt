package com.truepine.photouploader.di

import com.truepine.photouploader.AppInfo
import com.truepine.photouploader.JvmAppInfo
import com.truepine.photouploader.JvmPlatformInfo
import com.truepine.photouploader.PlatformInfo
import com.truepine.photouploader.auth.DesktopGoogleAuthService
import com.truepine.photouploader.auth.GoogleAuthService
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<GoogleAuthService> { DesktopGoogleAuthService() }
    single<AppInfo> { JvmAppInfo }
    single<PlatformInfo> { JvmPlatformInfo }
}
