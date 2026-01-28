package com.truepineapps.photouploader.ui.screen.uploader.uistate

import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.data.Album
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_one_or_more_photos_failed
import com.truepineapps.photouploader.util.UiTextResource
import okio.Path


data class AlbumUiState(
    val id: String, // Unique identifier (e.g., path)
    val kmpFile: KmpFile,
    val path: Path,
    val name: String,
    val group: String, // For sticky headers (e.g., parent directory name)
    val photoUiStates: List<PhotoUiState>,
    val coverPhotoUiState: PhotoUiState,
    val isEnabled: Boolean = true,
    /** Google Album ID after upload */
    val googleAlbumId: String? = null,
    val uploadStatus: UploadStatus = UploadStatus.None,
) {
    fun getDerivedUploadStatus(newStatus: UploadStatus): UploadStatus {
        if (uploadStatus.isFinal) {
            if (newStatus.isFinal) throw IllegalStateException("DerivedUploadStatus: Cannot change final status")
            return uploadStatus
        }

        if (newStatus == UploadStatus.Cancelled) return UploadStatus.Cancelled

        var isPhotoWaiting = false
        var isPhotoUploading = false
        var isAllSuccess = true
        var errorText: UiTextResource? = null
        photoUiStates.forEach { photo ->
            if (photo.isEnabled) {
                when (photo.uploadStatus) {
                    UploadStatus.None -> isAllSuccess = false
                    UploadStatus.Waiting -> isPhotoWaiting = true
                    UploadStatus.Uploading -> isPhotoUploading = true
                    is UploadStatus.UploadingError -> throw IllegalStateException(
                        "Photo cannot have UploadStatus.UploadingError"
                    )
                    UploadStatus.Success -> { /* Nothing to do */ }
                    UploadStatus.Cancelled -> isAllSuccess = false
                    is UploadStatus.Error -> {
                        // Show only the message of the first photo in error status
                        if (errorText == null) {
                            errorText =
                                    UiTextResource(
                                        Res.string.error_one_or_more_photos_failed,
                                        photo.uploadStatus.message
                                    )
                        }
                    }
                }
            }
        }

        if (errorText != null) {
            if (isPhotoWaiting || isPhotoUploading) {
                // Uploading is in progress, but an error has occurred.
                return UploadStatus.UploadingError(errorText)
            }
            return UploadStatus.Error(errorText)
        }
        if (isPhotoUploading) return UploadStatus.Uploading
        if (isPhotoWaiting) return UploadStatus.Waiting

        // All enabled photos completed successfully.
        if (isAllSuccess) return UploadStatus.Success

        return newStatus
    }
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
