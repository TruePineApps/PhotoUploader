package com.truepine.photouploader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.truepine.photouploader.ui.GoogleSignInButton
import com.truepine.photouploader.ui.PhotoUploadViewModel
import com.truepine.photouploader.ui.PlatformFilePickerScreen
import com.truepine.photouploader.ui.PlatformPicker
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel: PhotoUploadViewModel = koinInject()
        AppContent(viewModel)
    }
}

@Composable
fun AppContent(viewModel: PhotoUploadViewModel) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
//        if (isAuthenticated) {
            val picker: PlatformPicker = koinInject()
            PlatformFilePickerScreen(filePicker = picker, viewModel = koinInject())
//        } else {
//            GoogleSignInButton(onClick = viewModel::signIn)
//        }
    }
}
