package com.truepineapps.photouploader

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.di.initKoin
import com.truepineapps.photouploader.ui.DesktopPlatformPicker
import com.truepineapps.photouploader.ui.components.platformpicker.PlatformPicker
import io.ktor.client.HttpClient
import org.koin.core.context.stopKoin
import org.koin.dsl.module

fun main() = application {
    val koinApp = initKoin(isPickerDefined = true) {
        // Pass the desktop directory picker
        modules(module { single<PlatformPicker> { DesktopPlatformPicker() } })
    }
    val log = koinApp.koin.get<Logger>()
    Window(
        onCloseRequest = {
            log.d {"Window close requested. Shutting down resources..."}

            // Get the HttpClient from Koin and make sure it is closed.
            val httpClient = koinApp.koin.get<HttpClient>()
            httpClient.close()

            // Stop Koin to clean up all singletons
            stopKoin()

            // Explicitly exit the application.
            exitApplication()
        },
        title = "PhotoUploader",
    ) {
        App()
    }
}
