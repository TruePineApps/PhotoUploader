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

import com.truepineapps.photouploader.core.util.UiText
import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile

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
    val uploadReport: UploadReport? get() = viewState.uploadReport

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