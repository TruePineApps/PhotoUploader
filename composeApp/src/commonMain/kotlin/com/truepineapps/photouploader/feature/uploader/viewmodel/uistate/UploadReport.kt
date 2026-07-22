package com.truepineapps.photouploader.feature.uploader.viewmodel.uistate

data class UploadReport(
    val albumsCreated: Int = 0,
    val albumsSkipped: Int = 0,
    val albumsFailed: Int = 0,
    val photosUploaded: Int = 0,
    val photosSkipped: Int = 0,
    val photosFailed: Int = 0,
    val errors: List<UploadError> = emptyList(),
    val status: UploadCompletionStatus = UploadCompletionStatus.SUCCESS
)

data class UploadError(
    val name: String,
    val reason: String
)

enum class UploadCompletionStatus {
    SUCCESS, CANCELLED, ERRORS
}
