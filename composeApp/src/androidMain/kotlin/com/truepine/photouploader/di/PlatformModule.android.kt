package com.truepine.photouploader.di

import com.truepine.photouploader.AndroidAppInfo
import com.truepine.photouploader.AndroidPlatformInfo
import com.truepine.photouploader.AppInfo
import com.truepine.photouploader.PlatformInfo
import com.truepine.photouploader.auth.GoogleAuthService
import com.truepine.photouploader.auth.StubGoogleAuthService
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<GoogleAuthService> { StubGoogleAuthService() }
    single<AppInfo> { AndroidAppInfo }
    single<PlatformInfo> { AndroidPlatformInfo }
}

