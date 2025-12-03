package com.truepine.photouploader.di

import com.truepine.photouploader.ui.screen.uploader.PhotoUploaderViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun viewModelModule(): Module = module {
    single<PhotoUploaderViewModel> { PhotoUploaderViewModel(authService=get()) }
}