package com.truepine.photouploader.ui
//
//import androidx.compose.runtime.Composable
//import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
//import com.darkrockstudios.libraries.mpfilepicker.FilePicker
//import com.darkrockstudios.libraries.mpfilepicker.MultipleFilePicker
//
//class MpPlatformPicker : PlatformPicker {
//    @Composable
//    override fun PlatformFilePicker(
//        show: Boolean,
//        fileExtensions: List<String>,
//        onFileSelected: (String?) -> Unit,
//    ) {
//        FilePicker(show, fileExtensions = fileExtensions) { platformFile ->
//            onFileSelected(platformFile?.path)
//        }
//    }
//
//    @Composable
//    override fun PlatformMultipleFilePicker(
//        show: Boolean,
//        fileExtensions: List<String>,
//        onFilesSelected: (List<String>?) -> Unit,
//    ) {
//        MultipleFilePicker(show, fileExtensions = fileExtensions) { platformFiles ->
//            onFilesSelected(platformFiles?.map { it.path })
//        }
//    }
//
//    @Composable
//    override fun PlatformDirectoryPicker(
//        show: Boolean,
//        onDirectorySelected: (String?) -> Unit,
//    ) {
//        DirectoryPicker(show) { path ->
//            onDirectorySelected(path)
//        }
//    }
//}
