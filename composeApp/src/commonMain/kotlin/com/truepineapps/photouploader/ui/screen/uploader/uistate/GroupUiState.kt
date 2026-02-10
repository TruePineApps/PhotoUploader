package com.truepineapps.photouploader.ui.screen.uploader.uistate

data class GroupUiState(
    // Name for sticky headers
    val group: String,
    val albumsInGroup: List<AlbumUiState> = emptyList(),
    override val isEnabled: Boolean = true,
    val isExpanded: Boolean = true,
    override val uploadStatus: UploadStatus = UploadStatus.None,
) : UploadUiState(uploadStatus, isEnabled, true) {

    fun copyWithDerivedStatus(
        group: String = this.group,
        albumsInGroup: List<AlbumUiState> = this.albumsInGroup,
        isExpanded: Boolean = this.isExpanded,
        uploadStatus: UploadStatus = this.uploadStatus,
    ): GroupUiState {
        val newStatus = super.getDerivedUploadStatus(
            uploadUiStates = albumsInGroup,
            newStatus = uploadStatus
        )
        val newIsEnabled = albumsInGroup.any { it.isEnabled }
        return copy(
            group = group,
            albumsInGroup = albumsInGroup,
            isEnabled = newIsEnabled,
            isExpanded = isExpanded,
            uploadStatus = newStatus
        )
    }

    fun getDerivedUploadStatus(newStatus: UploadStatus): UploadStatus =
            super.getDerivedUploadStatus(albumsInGroup, newStatus)

}
