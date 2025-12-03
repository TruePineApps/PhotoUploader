package com.truepine.photouploader.di

import com.truepine.photouploader.AppInfo
import com.truepine.photouploader.IosAppInfo
import com.truepine.photouploader.IosPlatformInfo
import com.truepine.photouploader.PlatformInfo
import com.truepine.photouploader.auth.GoogleAuthService
import com.truepine.photouploader.auth.StubGoogleAuthService
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<GoogleAuthService> { StubGoogleAuthService() }
    single<AppInfo> { IosAppInfo }
    single<PlatformInfo> { IosPlatformInfo }
}
