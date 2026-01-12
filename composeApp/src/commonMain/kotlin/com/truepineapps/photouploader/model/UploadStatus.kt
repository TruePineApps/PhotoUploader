package com.truepineapps.photouploader.model

import com.truepineapps.photouploader.util.UiText

/**
 * Represents the various states of an upload operation for an Album or Photo.
 */
sealed class UploadStatus(val isFinal: Boolean = false) {
    /** Initial state, not yet processed. */
    data object None : UploadStatus()

    /** Queued for upload, but not yet started. */
    data object Waiting : UploadStatus()

    /** The upload is currently in progress. */
    data object Uploading : UploadStatus()

    /** The upload is currently in progress, but one of its parts failed. */
    data class UploadingError(val message: UiText) : UploadStatus()

    /** The upload completed successfully. */
    data object Success : UploadStatus(true)

    /** The upload was canceled by the user. */
    data object Cancelled : UploadStatus(true)

    /** The upload failed with an error. */
    data class Error(val message: UiText) : UploadStatus(true)
}
