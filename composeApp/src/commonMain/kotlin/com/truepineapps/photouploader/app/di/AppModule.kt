package com.truepineapps.photouploader.app.di

import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalConfig
import com.truepineapps.photouploader.core.io.KmpPlatformFileSystem
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import com.truepineapps.photouploader.feature.uploader.data.repository.PhotoDirectoryRepositoryImpl
import com.truepineapps.photouploader.feature.uploader.data.repository.PhotoUploaderImpl
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoDirectoryRepository
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoUploader
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule: Module = module {
    singleOf(::PhotoDirectoryRepositoryImpl) { bind<PhotoDirectoryRepository>() }
    singleOf(::PhotoUploaderImpl) { bind<PhotoUploader>() }

    // core/io
    singleOf(::KmpPlatformFileSystem) { bind<PlatformFileSystem>() }

    // core/feature/legal
    single {
        LegalConfig(
            versionUrl = "https://truepineapps.com/photouploader/legal_version.txt",
            termsUrl = "https://truepineapps.com/photouploader/terms_of_service.txt",
            privacyPolicyUrl = "https://truepineapps.com/photouploader/privacy_policy.txt",
        )
    }

}
