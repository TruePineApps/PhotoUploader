package com.truepineapps.photouploader.app.di

import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.util.AppInfo
import com.truepineapps.photouploader.core.util.IosAppInfo
import com.truepineapps.photouploader.core.util.IosPlatformInfo
import com.truepineapps.photouploader.core.util.PlatformInfo
import com.truepineapps.photouploader.feature.auth.GoogleAuthService
import com.truepineapps.photouploader.feature.auth.StubGoogleAuthService
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { Logger.withTag("PhotoUploader") }
    single<GoogleAuthService> { StubGoogleAuthService() }
    single<AppInfo> { IosAppInfo }
    single<PlatformInfo> { IosPlatformInfo }
}
