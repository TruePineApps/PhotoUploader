package com.truepineapps.photouploader.app.di

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.loggerConfigInit
import com.truepineapps.photouploader.core.log.TimestampMessageFormatter
import com.truepineapps.photouploader.core.util.AppInfo
import com.truepineapps.photouploader.core.util.JvmAppInfo
import com.truepineapps.photouploader.core.util.JvmPlatformInfo
import com.truepineapps.photouploader.core.util.PlatformInfo
import com.truepineapps.photouploader.foundation.auth.data.repository.DesktopGoogleAuthService
import com.truepineapps.photouploader.foundation.auth.domain.repository.GoogleAuthService
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose


actual fun platformModule(): Module = module {
    single {
        Logger(
            config = loggerConfigInit(CommonWriter(TimestampMessageFormatter)),
            tag = "PhotoUploader"
        )
    }
    single<GoogleAuthService> { DesktopGoogleAuthService(get()) } onClose { it?.shutdown() }
    single<AppInfo> { JvmAppInfo }
    single<PlatformInfo> { JvmPlatformInfo }
}
