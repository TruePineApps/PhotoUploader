package com.truepineapps.photouploader.ui.screen.uploader.uistate

import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.data.Album
import okio.Path


data class AlbumUiState(
    val id: String, // Unique identifier (e.g. path)
    val kmpFile: KmpFile,
    val path: Path,
    val name: String,
    val group: String, // For sticky headers (e.g. parent directory name)
    val photoUiStates: List<PhotoUiState>,
    val coverPhotoUiState: PhotoUiState,
    override val isEnabled: Boolean = true,
    /** Google Album ID after upload */
    val googleAlbumId: String? = null,
    override val uploadStatus: UploadStatus = UploadStatus.None,
) : UploadUiState(uploadStatus, isEnabled, true) {

    fun getDerivedUploadStatus(newStatus: UploadStatus): UploadStatus =
            super.getDerivedUploadStatus(photoUiStates, newStatus)
}

fun Album.toAlbumUiState(): AlbumUiState {
    var isFirst = true
    val photoUiStates = photos.map { photoData ->
        val state = photoData.toPhotoUiState(isFirst)
        isFirst = false
        state
    }
    return AlbumUiState(
        id = id,
        kmpFile = kmpFile,
        path = path,
        name = name,
        group = group,
        photoUiStates = photoUiStates,
        coverPhotoUiState = photoUiStates.first(),
    )
}
