package com.truepineapps.photouploader.app.di

import com.truepineapps.photouploader.feature.uploader.data.repository.PhotoDirectoryRepository
import com.truepineapps.photouploader.core.io.KmpPlatformFileSystem
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule: Module = module {
    singleOf<PlatformFileSystem>(::KmpPlatformFileSystem)
    singleOf(::PhotoDirectoryRepository)
}
