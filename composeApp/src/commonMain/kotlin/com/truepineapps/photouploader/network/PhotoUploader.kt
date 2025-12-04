package com.truepineapps.photouploader.network

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PhotoUploader(val accessToken: String) : KoinComponent {
    private val client: HttpClient by inject()
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Creates an album in Google Photos
     * @return Album ID if successful, null otherwise
     */
    suspend fun createAlbum(albumTitle: String): String? {
        return try {
            val requestBody = CreateAlbumRequest(
                album = AlbumData(title = albumTitle)
            )

            val response: HttpResponse =
                    client.post("https://photoslibrary.googleapis.com/v1/albums") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(requestBody))
                    }

            if (response.status.isSuccess()) {
                val albumResponse =
                        json.decodeFromString<AlbumResponse>(response.bodyAsText())
                albumResponse.id
            } else {
                println("Failed to create album: ${response.status}")
                println("Response: ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            println("Exception creating album: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Uploads a photo file and returns the upload token
     * @return Upload token if successful, null otherwise
     */
    suspend fun uploadPhoto(photoPath: Path, fileSystem: FileSystem): String? {
        return try {
            // Read the file as bytes
            val photoBytes = fileSystem.read(photoPath) {
                readByteArray()
            }

            // Upload the bytes to Google Photos
            val response: HttpResponse = client.post("https://photoslibrary.googleapis.com/v1/uploads") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                    append("X-Goog-Upload-Content-Type", getMimeType(photoPath.name))
                    append("X-Goog-Upload-Protocol", "raw")
                    append("X-Goog-Upload-File-Name", photoPath.name)
                }
                contentType(ContentType.Application.OctetStream)
                setBody(photoBytes)
            }

            if (response.status.isSuccess()) {
                response.bodyAsText() // The upload token is returned as plain text
            } else {
                println("Failed to upload photo: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Exception uploading photo: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Adds uploaded photos to an album
     */
    suspend fun addPhotosToAlbum(albumId: String, photos: List<UploadedPhoto>) {
        // Google Photos API allows up to 50 items per batch
        val batchSize = 50

        photos.chunked(batchSize).forEachIndexed { batchIndex, batch ->
            try {
                val newMediaItems = batch.map { photo ->
                    NewMediaItem(
                        description = photo.fileName,
                        simpleMediaItem = SimpleMediaItem(
                            fileName = photo.fileName,
                            uploadToken = photo.uploadToken
                        )
                    )
                }

                val requestBody = BatchCreateMediaItemsRequest(
                    albumId = albumId,
                    newMediaItems = newMediaItems
                )

                val response: HttpResponse = client.post("https://photoslibrary.googleapis.com/v1/mediaItems:batchCreate") {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }

                if (response.status.isSuccess()) {
                    val result = json.decodeFromString<BatchCreateMediaItemsResponse>(response.bodyAsText())
                    val successCount = result.newMediaItemResults.count { it.status.code == 0 }
                    val failCount = result.newMediaItemResults.count { it.status.code != 0 }

                    println("      Batch ${batchIndex + 1}: $successCount succeeded, $failCount failed")

                    // Log any failures
                    result.newMediaItemResults.filter { it.status.code != 0 }.forEach {
                        println("      Failed item: ${it.status.message}")
                    }
                } else {
                    println("Failed to add photos to album: ${response.status}")
                    println("Response: ${response.bodyAsText()}")
                }
            } catch (e: Exception) {
                println("Exception adding photos to album (batch ${batchIndex + 1}): ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Checks if a file is a photo based on its extension
     */
    fun isPhotoFile(fileName: String): Boolean {
        // Accepted types: AVIF, BMP, GIF, HEIC, ICO, JPG, PNG, TIFF, WEBP, see https://developers.google.com/photos/library/guides/upload-media
        val photoExtensions = setOf("avif", "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "ico", "tif", "tiff")
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in photoExtensions
    }

    /**
     * Gets the MIME type based on file extension
     */
    private fun getMimeType(fileName: String): String {
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