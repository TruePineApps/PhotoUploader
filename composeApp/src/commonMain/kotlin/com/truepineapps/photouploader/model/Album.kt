package com.truepineapps.photouploader.model

import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_one_or_more_photos_failed
import com.truepineapps.photouploader.util.UiTextResource
import okio.Path


data class Album(
    val id: String, // Unique identifier (e.g., path)
    val kmpFile: KmpFile,
    val path: Path,
    val name: String,
    val group: String, // For sticky headers (e.g., parent directory name)
    val photos: List<Photo>,
    val coverPhoto: Photo,
    val isEnabled: Boolean = true,
    /** Google Album ID after upload */
    val albumId: String? = null,
    val uploadStatus: UploadStatus = UploadStatus.None,
) {
    fun getDerivedUploadStatus(newStatus: UploadStatus): UploadStatus {
        // If the upload was cancelled, that is the primary status.
        // If the new status is an album specific status (like creation failed), prioritize it.
        if ((newStatus !is UploadStatus.None && newStatus !is UploadStatus.Success)
            || newStatus == UploadStatus.Cancelled) {
            return uploadStatus
        }

        val hasErrorPhoto = photos.any { it.uploadStatus is UploadStatus.Error }
        val isPhotoUploading = photos.any { it.uploadStatus is UploadStatus.Uploading }

        // Handle error states first.
        if (hasErrorPhoto) {
            val firstError =
                    photos.first { it.uploadStatus is UploadStatus.Error }.uploadStatus as UploadStatus.Error
            // Uploading is in progress, but an error has occurred.
            val errorText =
                    UiTextResource(Res.string.error_one_or_more_photos_failed, firstError.message)
            return if (isPhotoUploading) {
                UploadStatus.UploadingError(errorText)
            } else {
                // An error occurred, and nothing is uploading anymore.
                UploadStatus.Error(errorText)
            }
        }

        // Photos are uploading (without any errors).
        if (isPhotoUploading) {
            return UploadStatus.Uploading
        }

        val enabledPhotos = photos.filter { it.isEnabled }
        // All enabled photos completed successfully.
        if (enabledPhotos.all { it.uploadStatus is UploadStatus.Success }) {
            return UploadStatus.Success
        }

        // Photos are queued for upload.
        if (enabledPhotos.any { it.uploadStatus is UploadStatus.Waiting }) {
            return UploadStatus.Waiting
        }

        // Then the new status is valid (could be None or Success).
        return newStatus
    }
}
