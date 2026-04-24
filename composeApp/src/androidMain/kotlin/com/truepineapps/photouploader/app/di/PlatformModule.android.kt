package com.truepineapps.photouploader.app.di

import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.feature.auth.GoogleAuthService
import com.truepineapps.photouploader.feature.auth.StubGoogleAuthService
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { Logger.withTag("PhotoUploader") }
    single<GoogleAuthService> { StubGoogleAuthService() }
    // Platform module data that depends on BuildConfig is loaded in the initKoin call in
    // androidApp/PhotoUploaderApp
}
