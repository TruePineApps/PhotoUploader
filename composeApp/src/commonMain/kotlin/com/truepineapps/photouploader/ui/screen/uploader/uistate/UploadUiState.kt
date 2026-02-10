package com.truepineapps.photouploader.ui.screen.uploader.uistate

import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_one_or_more_photos_failed
import com.truepineapps.photouploader.util.UiTextResource


open class UploadUiState(
    open val uploadStatus: UploadStatus = UploadStatus.None,
    open val isEnabled: Boolean = true,
    // Groups and Albums have child states and can therefore be in status UploadingError
    val hasChildStates: Boolean = false,
) {
    fun getDerivedUploadStatus(uploadUiStates: List<UploadUiState>, newStatus: UploadStatus): UploadStatus {
        if (uploadStatus.isFinal) {
            if (newStatus.isFinal) throw IllegalStateException("DerivedUploadStatus: Cannot change final status")
            return uploadStatus
        }

        if (newStatus == UploadStatus.Cancelled) return UploadStatus.Cancelled

        var isStateWaiting = false
        var isStateUploading = false
        var isStateUploadingError = false
        var isStateCancelled = false
        var isAllSuccess = true
        var errorText: UiTextResource? = null
        uploadUiStates.forEach { uploadState ->
            if (uploadState.isEnabled) {
                when (uploadState.uploadStatus) {
                    UploadStatus.None -> isAllSuccess = false
                    UploadStatus.Waiting -> { isStateWaiting = true; isAllSuccess = false }
                    UploadStatus.Uploading -> { isStateUploading = true; isAllSuccess = false }
                    is UploadStatus.UploadingError -> {
                        if (!hasChildStates) {
                            throw IllegalStateException(
                                "${uploadState::class.simpleName} cannot have UploadStatus.UploadingError"
                            )
                        }
                        if (errorText == null) {
                            errorText =
                                    UiTextResource(
                                        Res.string.error_one_or_more_photos_failed,
                                        (uploadState.uploadStatus as UploadStatus.UploadingError).message
                                    )
                        }
                        isStateUploadingError = true
                        isAllSuccess = false
                    }
                    UploadStatus.Success -> { /* Nothing to do */ }
                    UploadStatus.Cancelled -> { isStateCancelled = true; isAllSuccess = false }
                    is UploadStatus.Error -> {
                        // Show only the message of the first upload in error status
                        if (errorText == null) {
                            errorText =
                                    UiTextResource(
                                        Res.string.error_one_or_more_photos_failed,
                                        (uploadState.uploadStatus as UploadStatus.Error).message
                                    )
                        }
                        isAllSuccess = false
                    }
                }
            }
        }

        if (errorText != null) {
            if (isStateWaiting || isStateUploading || isStateUploadingError) {
                // Uploading is in progress, but an error has occurred.
                return UploadStatus.UploadingError(errorText)
            }
            return UploadStatus.Error(errorText)
        }
        if (isStateUploading) return UploadStatus.Uploading
        if (isStateWaiting) return UploadStatus.Waiting
        if (isStateCancelled) return UploadStatus.Cancelled

        // All enabled uploads completed successfully.
        if (isAllSuccess) return UploadStatus.Success

        return newStatus
    }

}