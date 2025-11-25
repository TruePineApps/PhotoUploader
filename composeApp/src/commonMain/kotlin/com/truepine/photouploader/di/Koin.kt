package com.truepine.photouploader.di

import com.truepine.photouploader.ui.CalfPlatformPicker
import com.truepine.photouploader.ui.PlatformPicker
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ksp.generated.*

/**
 * Initializes the Koin dependency injection framework for the application.
 *
 * This function sets up the Koin context, loading the necessary modules including
 * the platform-specific picker implementation, the common application module,
 * and other platform-specific dependencies.
 *
 * pickerModule is set by desktop; it overrides the default DirectoryPicker which does not work.
 *
 * @param pickerModule The Koin [Module] that provides the [PlatformPicker] implementation.
 *                     Defaults to a module providing [CalfPlatformPicker] as a singleton.
 *                     This can be overridden for testing or specific platform needs.
 */
fun initKoin(
    pickerModule: Module = module { single<PlatformPicker> { CalfPlatformPicker() } },
) {
    startKoin {
        // Load the common AppModule and the platform-specific module
        modules(
            pickerModule,
            AppModule().module,
            viewModelModule(),
            platformModule()
        )
    }
}
