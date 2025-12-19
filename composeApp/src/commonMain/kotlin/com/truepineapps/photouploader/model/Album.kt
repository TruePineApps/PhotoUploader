package com.truepineapps.photouploader.model

import com.mohamedrejeb.calf.io.KmpFile
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
    var albumId: String? = null,
    var uploadStatus: UploadStatus = UploadStatus.None,
) {
    fun getDerivedUploadStatus(): UploadStatus {
        // If the album itself has a specific status (like creation failed), prioritize it.
        if (uploadStatus !is UploadStatus.None && uploadStatus !is UploadStatus.Success) {
            return uploadStatus
        }

        val hasError = photos.any { it.uploadStatus is UploadStatus.Error }
        val isUploading = photos.any { it.uploadStatus is UploadStatus.Uploading }

        // Handle error states first.
        if (hasError) {
            val firstError = photos.first { it.uploadStatus is UploadStatus.Error }.uploadStatus as UploadStatus.Error
            // Uploading is in progress, but an error has occurred.
            if (isUploading) return UploadStatus.UploadingError("One or more photos failed: ${firstError.message}")
            // An error occurred, and nothing is uploading anymore.
            return UploadStatus.Error("One or more photos failed: ${firstError.message}")
        }

        // Photos are still uploading (without any errors).
        if (isUploading) {
            return UploadStatus.Uploading
        }

        // All photos completed successfully.
        if (photos.all { it.uploadStatus is UploadStatus.Success }) {
            return UploadStatus.Success
        }

        // Photos are queued for upload.
        if (photos.any { it.uploadStatus is UploadStatus.Waiting }) {
            return UploadStatus.Waiting
        }

        // Fallback to whatever the album's status is (could be None or Success).
        return uploadStatus
    }
}
