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

package com.truepineapps.photouploader.feature.uploader.domain.repository

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.core.util.ServiceUtil
import com.truepineapps.photouploader.feature.uploader.data.dto.MediaItemResult
import com.truepineapps.photouploader.feature.uploader.data.dto.NewMediaItem

interface PhotoUploader {
    /**
     * Verifies if an album with the given ID exists on Google Photos.
     * @return `true` if the album exists, `false` if it was not found (404).
     * @throws [UploadException.GlobalException] for other HTTP errors.
     */
    suspend fun verifyAlbumExists(
        albumId: String,
        accessToken: String,
        serviceUtil: ServiceUtil = ServiceUtil("verifyAlbumExists")
    ): Boolean

    /**
     * Creates an album in Google Photos
     * @return Album ID if successful
     * @throws [UploadException.GlobalException] for auth errors
     * @throws [UploadException.AlbumException] for other API errors
     */
    suspend fun createAlbum(
        albumTitle: String,
        accessToken: String,
        serviceUtil: ServiceUtil = ServiceUtil("createAlbum")
    ): String

    /**
     * Uploads a photo file and returns the upload token
     * @return Upload token if successful
     * @throws [UploadException.GlobalException] for auth errors
     * @throws [UploadException.PhotoException] for other API errors
     */
    suspend fun uploadPhoto(
        photoName: String,
        kmpFile: KmpFile,
        accessToken: String,
        platformContext: PlatformContext,
        serviceUtil: ServiceUtil = ServiceUtil("uploadPhoto")
    ): String

    /**
     * Adds uploaded photos to an album in batches of 50 (API limit)
     */
    suspend fun addPhotosToAlbum(
        albumId: String,
        newMediaItems: List<NewMediaItem>,
        accessToken: String,
    ): List<MediaItemResult>

    suspend fun updateAlbumCover(
        albumId: String, coverMediaItemId: String,
        accessToken: String,
        serviceUtil: ServiceUtil = ServiceUtil("updateAlbumCover")
    )
}