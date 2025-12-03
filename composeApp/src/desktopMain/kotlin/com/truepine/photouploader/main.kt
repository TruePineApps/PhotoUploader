package com.truepine.photouploader

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.truepine.photouploader.di.initKoin
import com.truepine.photouploader.ui.DesktopPlatformPicker
import com.truepine.photouploader.ui.components.PlatformPicker.PlatformPicker
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