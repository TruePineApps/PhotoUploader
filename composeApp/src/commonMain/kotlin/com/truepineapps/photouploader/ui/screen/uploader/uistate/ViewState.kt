package com.truepineapps.photouploader.ui.screen.uploader.uistate

import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.auth.UserProfile
import com.truepineapps.photouploader.util.UiText

data class ViewState(
    val userProfile: UserProfile? = null,
    val status: AppStatus = AppStatus.IDLE,
    private val kmpFile: KmpFile? = null,
    val path: String = "",
    val selectedAlbumId: String? = null,
    val globalErrorMessage: UiText? = null,
) {
    val isAuthenticated = userProfile != null
}