package com.truepineapps.photouploader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.truepineapps.photouploader.app.App

fun MainViewController() = ComposeUIViewController {
    App(modifier = Modifier.fillMaxSize())
}