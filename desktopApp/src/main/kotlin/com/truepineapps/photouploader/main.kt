package com.truepineapps.photouploader

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.di.initKoin
import com.truepineapps.photouploader.ui.DesktopPlatformPicker
import com.truepineapps.photouploader.core.presentation.components.platformpicker.PlatformPicker
import io.ktor.client.HttpClient
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.system.exitProcess

fun main() = application {
    val koinApp = initKoin(isPickerDefined = true) {
        // Pass the desktop directory picker
        modules(module { single<PlatformPicker> { DesktopPlatformPicker() } })
    }
    val log = koinApp.koin.get<Logger>()
    Window(
        onCloseRequest = {
            log.d {"Window close requested. Shutting down resources..."}
            try {
                // Get the HttpClient from Koin and make sure it is closed.
                val httpClient = koinApp.koin.get<HttpClient>()
                httpClient.close()

                // Stop Koin to clean up all singletons
                stopKoin()
            } catch (e: Exception) {
                log.e { "Error during shutdown: ${e.message}" }
            } finally {
                log.d {"Stop application and process..."}

                // Explicitly exit the Compose application.
                exitApplication()

                // Force the process to end immediately.
                // This releases all file handles to the JRE/DLLs instantly.
                exitProcess(0)
            }
        },
        title = "PhotoUploader",
    ) {
        App()
    }
}
