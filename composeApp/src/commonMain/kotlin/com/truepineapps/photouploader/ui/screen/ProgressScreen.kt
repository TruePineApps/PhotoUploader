package com.truepineapps.photouploader.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.ui.components.ProgressIndicator

@Composable
fun ProgressScreen(action: String, modifier: Modifier = Modifier) {
    ProgressIndicator(
        action = action, modifier = modifier.fillMaxSize().wrapContentSize(Alignment.Center)
    )
}