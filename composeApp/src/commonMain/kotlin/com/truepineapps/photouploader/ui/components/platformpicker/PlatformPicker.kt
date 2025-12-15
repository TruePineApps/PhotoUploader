package com.truepineapps.photouploader.ui.components.platformpicker

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.io.KmpFile

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
    fun PlatformDirectoryPicker(show: Boolean, onDirectorySelected: (KmpFile?) -> Unit)
}