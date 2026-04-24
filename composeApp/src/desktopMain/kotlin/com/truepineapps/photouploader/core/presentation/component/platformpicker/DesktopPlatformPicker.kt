package com.truepineapps.photouploader.core.presentation.component.platformpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerLauncher
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.truepineapps.photouploader.core.util.DesktopType
import com.truepineapps.photouploader.core.util.DesktopTypeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import java.io.File
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.select_photo_folder

/** Directory picking on Linux doesn't work. Copy Windows fallback implementation using the Swing
 * for this case, see https://github.com/MohamedRejeb/Calf/blob/main/calf-file-picker/src/desktopMain/kotlin/com/mohamedrejeb/calf/picker/platform/windows/api/JnaFileChooser.kt
 */
class DesktopPlatformPicker : CalfPlatformPicker() {
    @Composable
    override fun PlatformDirectoryPicker(show: Boolean, onDirectorySelected: (KmpFile?) -> Unit) {
        when (DesktopTypeUtil.current) {
            DesktopType.Windows ->
                super.PlatformDirectoryPicker(show, onDirectorySelected)

            DesktopType.MacOS ->
                super.PlatformDirectoryPicker(show, onDirectorySelected)

            DesktopType.Linux -> {
                val pickerLauncher = rememberDirectoryPickerLauncher(
                    onResult = { files ->
                        val file = files.firstOrNull()
                        onDirectorySelected(file)
                    }
                )
                launchFilePicker(show = show, pickerLauncher = pickerLauncher)
            }
        }
    }

    @Composable
    private fun rememberDirectoryPickerLauncher(onResult: (List<KmpFile>) -> Unit): FilePickerLauncher {
        val scope = rememberCoroutineScope()
        val dialogTitle = stringResource(Res.string.select_photo_folder)
        return remember {
            FilePickerLauncher(
                type = FilePickerFileType.Folder,
                selectionMode = FilePickerSelectionMode.Single,
                onLaunch = {
                    scope.launch {
                        launchDirectoryPicker(dialogTitle = dialogTitle) { file ->
                            onResult(
                                if (file == null)
                                    emptyList()
                                else
                                    listOf(KmpFile(file))
                            )
                        }
                    }
                },
            )
        }
    }

    suspend fun launchDirectoryPicker(dialogTitle: String, onResult: (File?) -> Unit) =
        withContext(Dispatchers.Default) {
            val fileChooser = SwingDirectoryChooser(dialogTitle)

            // Show file chooser
            fileChooser.showOpenDialog()

            // Return selected directory
            onResult(fileChooser.selectedFile)
        }

}