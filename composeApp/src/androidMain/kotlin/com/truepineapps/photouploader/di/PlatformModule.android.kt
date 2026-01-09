package com.truepineapps.photouploader.di

import com.truepineapps.photouploader.AndroidAppInfo
import com.truepineapps.photouploader.AndroidPlatformInfo
import com.truepineapps.photouploader.AppInfo
import com.truepineapps.photouploader.PlatformInfo
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.auth.StubGoogleAuthService
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<GoogleAuthService> { StubGoogleAuthService() }
    single<AppInfo> { AndroidAppInfo }
    single<PlatformInfo> { AndroidPlatformInfo }
}
