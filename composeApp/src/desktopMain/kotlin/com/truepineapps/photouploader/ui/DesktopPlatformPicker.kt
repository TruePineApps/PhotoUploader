package com.truepineapps.photouploader.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerLauncher
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.truepineapps.photouploader.DesktopType
import com.truepineapps.photouploader.DesktopTypeUtil
import com.truepineapps.photouploader.ui.components.PlatformPicker.CalfPlatformPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Directory picking on Linux doesn't work. Copy Windows fallback implementation using the Swing
 * for this case, see https://github.com/MohamedRejeb/Calf/blob/main/calf-file-picker/src/desktopMain/kotlin/com/mohamedrejeb/calf/picker/platform/windows/api/JnaFileChooser.kt
 */
class DesktopPlatformPicker : CalfPlatformPicker() {
    @Composable
    override fun PlatformDirectoryPicker(show: Boolean, onDirectorySelected: (String?) -> Unit) {
        when (DesktopTypeUtil.current) {
            DesktopType.Windows ->
                super.PlatformDirectoryPicker(show, onDirectorySelected)

            DesktopType.MacOS ->
                super.PlatformDirectoryPicker(show, onDirectorySelected)

            DesktopType.Linux -> {
                val context = LocalPlatformContext.current
                val pickerLauncher = rememberDirectoryPickerLauncher(
                    onResult = { files ->
                        if (files.isNotEmpty()) onDirectorySelected(
                            files[0].getPath(context)
                        )
                    }
                )
                launchFilePicker(show = show, pickerLauncher = pickerLauncher)
            }
        }
    }

    @Composable
    private fun rememberDirectoryPickerLauncher(onResult: (List<KmpFile>) -> Unit): FilePickerLauncher {
        val scope = rememberCoroutineScope()

        return remember {
            FilePickerLauncher(
                type = FilePickerFileType.Folder,
                selectionMode = FilePickerSelectionMode.Single,
                onLaunch = {
                    scope.launch {
                        launchDirectoryPicker(
                            onResult = { file ->
                                onResult(
                                    if (file == null)
                                        emptyList()
                                    else
                                        listOf(KmpFile(file))
                                )
                            }
                        )
                    }
                },
            )
        }
    }

    suspend fun launchDirectoryPicker(onResult: (File?) -> Unit) =
        withContext(Dispatchers.Default) {
            val fileChooser = SwingDirectoryChooser()

            // Show file chooser
            fileChooser.showOpenDialog()

            // Return selected directory
            onResult(fileChooser.selectedFile)
        }

}