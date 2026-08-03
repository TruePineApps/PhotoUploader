/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.app.App
import com.truepineapps.photouploader.app.di.exitKoin
import com.truepineapps.photouploader.app.di.initKoin
import com.truepineapps.photouploader.core.presentation.component.platformpicker.DesktopPlatformPicker
import com.truepineapps.photouploader.core.presentation.component.platformpicker.PlatformPicker
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
                // Clean up and stop Koin to remove all singletons
                exitKoin(koinApp.koin)
                log.d { "Koin and resources successfully shut down." }
            } catch (e: Exception) {
                log.e(e) { "Error during shutdown: ${e.message}" }
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
