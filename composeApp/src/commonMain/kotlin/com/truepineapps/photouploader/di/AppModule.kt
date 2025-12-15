package com.truepineapps.photouploader.di

import com.truepineapps.photouploader.data.PhotoDirectoryRepository
import com.truepineapps.photouploader.io.KmpPlatformFileSystem
import com.truepineapps.photouploader.io.PlatformFileSystem
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule: Module = module {
    singleOf<PlatformFileSystem>(::KmpPlatformFileSystem)
    singleOf(::PhotoDirectoryRepository)
}
