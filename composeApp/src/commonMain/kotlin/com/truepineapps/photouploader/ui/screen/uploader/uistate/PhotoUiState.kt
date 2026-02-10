package com.truepineapps.photouploader.ui.screen.uploader.uistate

import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.data.Photo
import okio.Path

data class PhotoUiState(
    val kmpFile: KmpFile,
    val path: Path,
    /** File name */
    val name: String,
    override val isEnabled: Boolean = true,
    val isCoverPhoto: Boolean = false,
    /** Google Photo ID after upload */
    val mediaItemId: String? = null,
    override val uploadStatus: UploadStatus = UploadStatus.None,
) : UploadUiState (uploadStatus, isEnabled) {
    /** File name without extension */
    fun getDisplayName(): String {
        // Safe filename parsing
        val dotIndex = name.lastIndexOf('.')
        return if (dotIndex > 0) name.take(dotIndex) else name
    }
}

fun Photo.toPhotoUiState(isCoverPhoto: Boolean = false) = PhotoUiState(
    kmpFile = kmpFile,
    path = path,
    name = name,
    isCoverPhoto = isCoverPhoto,
)