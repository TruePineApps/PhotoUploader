package com.truepineapps.photouploader.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.status_cancelled
import com.truepineapps.photouploader.resources.status_error
import com.truepineapps.photouploader.resources.status_none
import com.truepineapps.photouploader.resources.status_success
import com.truepineapps.photouploader.resources.status_uploading
import com.truepineapps.photouploader.resources.status_uploading_error
import com.truepineapps.photouploader.resources.status_waiting
import com.truepineapps.photouploader.ui.theme.StatusPalette
import com.truepineapps.photouploader.util.UiText
import org.jetbrains.compose.resources.stringResource


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

    @Composable
    fun getDescription(): String {
        val descriptionRes = when (this) {
            Cancelled -> Res.string.status_cancelled
            is Error -> Res.string.status_error
            None -> Res.string.status_none
            Success -> Res.string.status_success
            Uploading -> Res.string.status_uploading
            is UploadingError -> Res.string.status_uploading_error
            Waiting -> Res.string.status_waiting
        }
        return stringResource(descriptionRes)
    }

    @Composable
    fun getColor(): Color =
            when (this) {
                is Error -> StatusPalette.Error
                is UploadingError -> StatusPalette.Warning
                is Cancelled -> StatusPalette.Warning
                is Success -> StatusPalette.Success
                else -> MaterialTheme.colorScheme.primary
            }

}
