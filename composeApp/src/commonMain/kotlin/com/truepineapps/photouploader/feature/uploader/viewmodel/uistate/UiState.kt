package com.truepineapps.photouploader.feature.uploader.viewmodel.uistate

import com.truepineapps.photouploader.feature.auth.UserProfile
import com.truepineapps.photouploader.core.util.UiText

data class UiState(
    val viewState: ViewState = ViewState(),
    val albumUiStates: List<AlbumUiState> = emptyList(),
    val groupUiStates: List<GroupUiState> = emptyList(),
) {
    val userProfile: UserProfile? get() = viewState.userProfile
    val isAuthenticated: Boolean get() = viewState.isAuthenticated
    val isShowDirPicker: Boolean get() = viewState.status == AppStatus.CHOOSING_DIRECTORY
    val isSigningIn: Boolean get() = viewState.status == AppStatus.SIGNING_IN
    val isUploading: Boolean get() = viewState.status == AppStatus.UPLOADING
    val path: String get() = viewState.path
    val selectedAlbumId get() = viewState.selectedAlbumId
    val globalErrorMessage: UiText? get() = viewState.globalErrorMessage

    fun busy() = viewState.status != AppStatus.IDLE
    fun idle() = viewState.status == AppStatus.IDLE

    override fun toString(): String {
        return "UiState(userProfile=$userProfile, " +
                "isAuthenticated=$isAuthenticated, " +
                "isShowDirPicker=$isShowDirPicker, " +
                "isSigningIn=$isSigningIn, " +
                "isUploading=$isUploading, " +
                "path='$path', " +
                "globalErrorMessage=$globalErrorMessage, " +
                "album size=${albumUiStates.size})"
    }
}