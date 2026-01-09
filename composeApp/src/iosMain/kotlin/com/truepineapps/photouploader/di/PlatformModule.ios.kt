package com.truepineapps.photouploader.di

import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.AppInfo
import com.truepineapps.photouploader.IosAppInfo
import com.truepineapps.photouploader.IosPlatformInfo
import com.truepineapps.photouploader.PlatformInfo
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.auth.StubGoogleAuthService
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { Logger.withTag("PhotoUploader") }
    single<GoogleAuthService> { StubGoogleAuthService() }
    single<AppInfo> { IosAppInfo }
    single<PlatformInfo> { IosPlatformInfo }
}
