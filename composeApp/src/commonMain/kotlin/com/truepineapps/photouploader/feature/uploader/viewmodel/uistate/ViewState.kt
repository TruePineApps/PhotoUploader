package com.truepineapps.photouploader.feature.uploader.viewmodel.uistate

import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.core.util.UiText
import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile

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