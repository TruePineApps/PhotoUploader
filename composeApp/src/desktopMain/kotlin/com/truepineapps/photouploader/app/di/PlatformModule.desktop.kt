package com.truepineapps.photouploader.app.di

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.loggerConfigInit
import com.truepineapps.photouploader.core.util.AppInfo
import com.truepineapps.photouploader.core.util.JvmAppInfo
import com.truepineapps.photouploader.core.util.JvmPlatformInfo
import com.truepineapps.photouploader.core.util.PlatformInfo
import com.truepineapps.photouploader.feature.auth.DesktopGoogleAuthService
import com.truepineapps.photouploader.feature.auth.GoogleAuthService
import com.truepineapps.photouploader.core.log.TimestampMessageFormatter
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
