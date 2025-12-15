package com.truepineapps.photouploader

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.truepineapps.photouploader.di.initKoin
import com.truepineapps.photouploader.ui.DesktopPlatformPicker
import com.truepineapps.photouploader.ui.components.platformpicker.PlatformPicker
import org.koin.dsl.module

fun main() = application {
    initKoin(isPickerDefined = true) {
        // Pass the desktop directory picker
        modules(module { single<PlatformPicker> { DesktopPlatformPicker() } })
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "PhotoUploader",
    ) {
        App()
    }
}