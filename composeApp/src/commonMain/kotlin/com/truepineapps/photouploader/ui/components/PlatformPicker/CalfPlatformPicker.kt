package com.truepineapps.photouploader.ui.components.PlatformPicker

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerLauncher
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.truepineapps.photouploader.io.getAbsolutePath

open class CalfPlatformPicker : PlatformPicker {

    /*****
        To read files:
        ```
        var kmpFiles by remember {
            mutableStateOf<List<KmpFile>>(emptyList())
        }
        val scope = rememberCoroutineScope()
        val context = LocalPlatformContext.current
            onResult = { files ->
                kmpFiles = files
                scope.launch {
                    files.firstOrNull()?.readByteArray(context)
            }
        }
        ```
        Note: readByteArray reads the entire file into memory. For large files, it's recommended to use
        the platform-specific APIs to read the file.
    ******/


    @Composable
    override fun PlatformFilePicker(
        show: Boolean,
        fileExtensions: List<String>,
        onFileSelected: (String?) -> Unit,
    ) {
        val context = LocalPlatformContext.current
        val pickerLauncher = rememberFilePickerLauncher(
            type = FilePickerFileType.Extension(fileExtensions),
            selectionMode = FilePickerSelectionMode.Single,
            onResult = { files -> if (files.isNotEmpty()) onFileSelected(files[0].getPath(context)) }
        )
        launchFilePicker(show = show, pickerLauncher = pickerLauncher)
    }

    @Composable
    override fun PlatformMultipleFilePicker(
        show: Boolean,
        fileExtensions: List<String>,
        onFilesSelected: (List<String>?) -> Unit,
    ) {
        val context = LocalPlatformContext.current
        val pickerLauncher = rememberFilePickerLauncher(
            type = FilePickerFileType.Extension(fileExtensions),
            selectionMode = FilePickerSelectionMode.Multiple,
            onResult = { files ->
                onFilesSelected(files.mapNotNull { file -> file.getPath(context) }.toList())
            }
        )
        launchFilePicker(show = show, pickerLauncher = pickerLauncher)
    }

    @Composable
    override fun PlatformDirectoryPicker(
        show: Boolean,
        onDirectorySelected: (KmpFile?, String?) -> Unit,
    ) {
        val context = LocalPlatformContext.current
        val pickerLauncher = rememberFilePickerLauncher(
            type = FilePickerFileType.Folder,
            selectionMode = FilePickerSelectionMode.Single,
            onResult = { files ->
                val kmpFile = files.firstOrNull()
                onDirectorySelected(kmpFile, kmpFile?.getAbsolutePath(context))
            }
        )
        launchFilePicker(show = show, pickerLauncher = pickerLauncher)
    }

    @Composable
    fun PlatformFolderPicker(
        show: Boolean,
        onFolderSelected: (KmpFile) -> Unit,
    ) {
        val pickerLauncher = rememberFilePickerLauncher(
            type = FilePickerFileType.Folder,
            selectionMode = FilePickerSelectionMode.Single,
            onResult = { files ->
                if (files.isNotEmpty()) onFolderSelected(files[0])
            }
        )
        launchFilePicker(show = show, pickerLauncher = pickerLauncher)
    }

    protected fun launchFilePicker(show: Boolean, pickerLauncher: FilePickerLauncher) {
        if (show) {
            pickerLauncher.launch()
        }
    }

}