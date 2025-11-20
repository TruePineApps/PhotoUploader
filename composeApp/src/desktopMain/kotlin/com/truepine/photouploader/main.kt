package com.truepine.photouploader

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.truepine.photouploader.di.initKoin
import com.truepine.photouploader.ui.DesktopPlatformPicker
import com.truepine.photouploader.ui.PlatformPicker
import org.koin.dsl.module

fun main() = application {
    initKoin(
        // Pass the desktop directory picker
        pickerModule = module { single<PlatformPicker> { DesktopPlatformPicker() } }
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "PhotoUploader",
    ) {
        App()
    }
}