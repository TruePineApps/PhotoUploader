package com.truepineapps.photouploader.di

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.loggerConfigInit
import com.truepineapps.photouploader.AppInfo
import com.truepineapps.photouploader.JvmAppInfo
import com.truepineapps.photouploader.JvmPlatformInfo
import com.truepineapps.photouploader.PlatformInfo
import com.truepineapps.photouploader.auth.DesktopGoogleAuthService
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.log.TimestampMessageFormatter
import org.koin.core.module.Module
import org.koin.dsl.module


actual fun platformModule(): Module = module {
    single {
        Logger(
            config = loggerConfigInit(CommonWriter(TimestampMessageFormatter)),
            tag = "PhotoUploader"
        )
    }
    single<GoogleAuthService> { DesktopGoogleAuthService(get()) }
    single<AppInfo> { JvmAppInfo }
    single<PlatformInfo> { JvmPlatformInfo }
}
