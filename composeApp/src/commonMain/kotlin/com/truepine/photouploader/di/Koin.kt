package com.truepine.photouploader.di

import com.truepine.photouploader.ui.CalfPlatformPicker
import com.truepine.photouploader.ui.PlatformPicker
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ksp.generated.*

fun initKoin(
    pickerModule: Module = module { single<PlatformPicker> { CalfPlatformPicker() } },
) {
    startKoin {
        // Load the modules generated for the AppModule
        modules(pickerModule, AppModule().module)
    }
}
