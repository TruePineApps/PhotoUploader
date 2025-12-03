package com.truepine.photouploader.ui.components.PlatformPicker

import androidx.compose.runtime.Composable

interface PlatformPicker {
    @Composable
    fun PlatformFilePicker(
        show: Boolean,
        fileExtensions: List<String>,
        onFileSelected: (String?) -> Unit,
    )

    @Composable
    fun PlatformMultipleFilePicker(
        show: Boolean,
        fileExtensions: List<String>,
        onFilesSelected: (List<String>?) -> Unit,
    )

    @Composable
    fun PlatformDirectoryPicker(show: Boolean, onDirectorySelected: (String?) -> Unit)
}