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

import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.feature.uploader.domain.model.Album
import okio.Path


data class AlbumUiState(
    val id: String, // Unique identifier (e.g. path)
    val kmpFile: KmpFile,
    val path: Path,
    val name: String,
    val group: String, // For sticky headers (e.g. parent directory name)
    val photoUiStates: List<PhotoUiState>,
    val coverPhotoUiState: PhotoUiState,
    override val isEnabled: Boolean = true,
    /** Google Album ID after upload */
    val googleAlbumId: String? = null,
    override val uploadStatus: UploadStatus = UploadStatus.None,
) : UploadUiState(uploadStatus, isEnabled, true) {

    fun getDerivedUploadStatus(newStatus: UploadStatus): UploadStatus =
            super.getDerivedUploadStatus(photoUiStates, newStatus)
}

fun Album.toAlbumUiState(): AlbumUiState {
    var isFirst = true
    val photoUiStates = photos.map { photoData ->
        val state = photoData.toPhotoUiState(isFirst)
        isFirst = false
        state
    }
    return AlbumUiState(
        id = id,
        kmpFile = kmpFile,
        path = path,
        name = name,
        group = group,
        photoUiStates = photoUiStates,
        coverPhotoUiState = photoUiStates.first(),
    )
}
