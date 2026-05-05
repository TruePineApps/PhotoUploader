package com.truepineapps.photouploader.app.di

import com.truepineapps.photouploader.feature.uploader.viewmodel.PhotoUploaderViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun viewModelModule(): Module = module {
    viewModelOf(::PhotoUploaderViewModel)
}
