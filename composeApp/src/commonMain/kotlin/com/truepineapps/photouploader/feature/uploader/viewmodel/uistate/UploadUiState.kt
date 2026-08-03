/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader.feature.uploader.viewmodel.uistate

import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_one_or_more_photos_failed
import com.truepineapps.photouploader.core.util.UiTextResource


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
        var isAllDisabled = true
        var errorText: UiTextResource? = null
        uploadUiStates.forEach { uploadState ->
            if (uploadState.isEnabled) {
                isAllDisabled = false
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
        if (isAllSuccess && !isAllDisabled) return UploadStatus.Success

        return newStatus
    }

}