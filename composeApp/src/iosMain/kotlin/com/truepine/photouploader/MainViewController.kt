package com.truepine.photouploader

import androidx.compose.ui.window.ComposeUIViewController
import com.truepine.photouploader.di.initKoin

fun MainViewController() = ComposeUIViewController { 
    initKoin()
    App() 
}