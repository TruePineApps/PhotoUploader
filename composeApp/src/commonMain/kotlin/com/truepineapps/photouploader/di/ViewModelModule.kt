package com.truepineapps.photouploader.di

import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun viewModelModule(): Module = module {
    singleOf(::PhotoUploaderViewModel)
}
