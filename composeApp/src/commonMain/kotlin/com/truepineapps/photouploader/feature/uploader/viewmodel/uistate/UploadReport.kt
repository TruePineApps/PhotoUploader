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
