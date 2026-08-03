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
import com.truepineapps.photouploader.feature.uploader.domain.model.Photo
import okio.Path

data class PhotoUiState(
    val kmpFile: KmpFile,
    val path: Path,
    /** File name */
    val name: String,
    override val isEnabled: Boolean = true,
    val isCoverPhoto: Boolean = false,
    /** Google Photo ID after upload */
    val mediaItemId: String? = null,
    override val uploadStatus: UploadStatus = UploadStatus.None,
) : UploadUiState(uploadStatus, isEnabled) {
    /** File name without extension */
    fun getDisplayName(): String {
        // Safe filename parsing
        val dotIndex = name.lastIndexOf('.')
        return if (dotIndex > 0) name.take(dotIndex) else name
    }
}

fun Photo.toPhotoUiState(isCoverPhoto: Boolean = false) =
    PhotoUiState(
        kmpFile = kmpFile,
        path = path,
        name = name,
        isCoverPhoto = isCoverPhoto,
    )