package com.truepine.photouploader.di

import com.truepine.photouploader.ui.PhotoUploadViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun viewModelModule(): Module = module {
    single<PhotoUploadViewModel> { PhotoUploadViewModel(authService=get()) }
}