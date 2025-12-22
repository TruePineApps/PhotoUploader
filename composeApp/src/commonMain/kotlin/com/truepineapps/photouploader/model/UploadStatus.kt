package com.truepineapps.photouploader.model

import com.truepineapps.photouploader.util.UiText

/**
 * Represents the various states of an upload operation for an Album or Photo.
 */
sealed class UploadStatus {
    /** Initial state, not yet processed. */
    object None : UploadStatus()

    /** Queued for upload, but not yet started. */
    object Waiting : UploadStatus()

    /** The upload is currently in progress. */
    object Uploading : UploadStatus()

    /** The upload is currently in progress, but one of its parts failed. */
    data class UploadingError(val message: UiText) : UploadStatus()

    /** The upload completed successfully. */
    object Success : UploadStatus()

    /** The upload failed with an error. */
    data class Error(val message: UiText) : UploadStatus()
}
