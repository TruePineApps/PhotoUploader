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

package com.truepineapps.photouploader.core.util

object FileUtils {
    /**
     * Checks if a file is a photo based on its extension
     */
    fun isPhotoFile(fileName: String?): Boolean {
        // Accepted types: AVIF, BMP, GIF, HEIC, ICO, JPG, PNG, TIFF, WEBP, see https://developers.google.com/photos/library/guides/upload-media
        val photoExtensions = setOf("avif", "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "ico", "tif", "tiff")
        val extension = fileName?.substringAfterLast('.', "")?.lowercase()
        return extension in photoExtensions
    }

    /**
     * Gets the MIME type based on file extension
     */
    fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "avif" -> "image/avif"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "heic" -> "image/heic" // the most common file extension for HEIF images
            "heif" -> "image/heif"
            "ico" -> "image/x-icon"
            "tif", "tiff" -> "image/tiff"
            else -> "application/octet-stream"
        }
    }
}
