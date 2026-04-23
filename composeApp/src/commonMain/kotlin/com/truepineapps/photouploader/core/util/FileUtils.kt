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
