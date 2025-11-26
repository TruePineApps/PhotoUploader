package com.truepine.photouploader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


@Composable
fun PlatformFilePickerScreen(filePicker: PlatformPicker, modifier : Modifier = Modifier, viewModel: PhotoUploadViewModel) {
    val fileType = listOf("jpg", "png")

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        var showSingleFilePicker by remember { mutableStateOf(false) }
        var pathSingleChosen by remember { mutableStateOf("") }

        Button(onClick = {
            showSingleFilePicker = true
        }) {
            Text("Choose File")
        }
        Text("File Chosen: $pathSingleChosen")

        filePicker.PlatformFilePicker(showSingleFilePicker, fileExtensions = fileType) { platformFileName ->
            pathSingleChosen = platformFileName ?: "none selected"
            showSingleFilePicker = false
        }

        /////////////////////////////////////////////////////////////////

        var showMultipleFilePicker by remember { mutableStateOf(false) }
        var pathMultipleChosen by remember { mutableStateOf(listOf("")) }

        Button(onClick = {
            showMultipleFilePicker = true
        }) {
            Text("Multiple Choose File")
        }
        Text("Multiple File Chosen: $pathMultipleChosen")

        filePicker.PlatformMultipleFilePicker(showMultipleFilePicker, fileExtensions = fileType) { platformFilesNames ->
            if (platformFilesNames != null) {
                pathMultipleChosen = platformFilesNames.map { it + "\n" }
            }
            showMultipleFilePicker = false
        }

        /////////////////////////////////////////////////////////////////

        var showDirPicker by remember { mutableStateOf(false) }
        var dirChosen by remember { mutableStateOf("") }
        var validDir by remember { mutableStateOf(false) }

        Button(onClick = {
            showDirPicker = true
        }) {
            Text("Choose Directory")
        }
        Text("Directory Chosen: $dirChosen")

        filePicker.PlatformDirectoryPicker(showDirPicker) { path ->
            validDir = path != null
            dirChosen = path ?: "none selected"
            showDirPicker = false
        }

        if (validDir) {
            viewModel.viewModelScope.launch {
                viewModel.uploadPhotos(dirChosen)
            }
        }
    }
}