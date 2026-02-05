package com.truepineapps.photouploader.ui.screen.uploader.uistate

data class GroupUiState(
    // Name for sticky headers
    val group: String,
    val albumsInGroup: List<AlbumUiState> = emptyList(),
    val isEnabled: Boolean = true,
    val isExpanded: Boolean = true
)
