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
        return GroupUiState(
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
