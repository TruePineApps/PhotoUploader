package com.truepineapps.photouploader.network

import com.mohamedrejeb.calf.core.PlatformContext
import com.truepineapps.photouploader.io.PlatformFileSystem
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.util.FileUtils
import com.truepineapps.photouploader.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.serialization.json.Json
import okio.Buffer
import okio.use
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// Google Photos API allows up to 50 items per batch
private const val GOOGLE_PHOTO_BATCH_SIZE = 50

class PhotoUploader(
    val accessToken: String,
    val context: PlatformContext,
) : KoinComponent {
    private val platformFileSystem: PlatformFileSystem by inject()
    private val client: HttpClient by inject()
    private val json: Json by inject()

    /**
     * Creates an album in Google Photos
     * @return Result with Album ID if successful, Error message otherwise
     */
    suspend fun createAlbum(albumTitle: String): Result<String, String> {
        return try {
            val requestBody = CreateAlbumRequest(
                album = AlbumData(title = albumTitle)
            )

            val response: HttpResponse = client.post("https://photoslibrary.googleapis.com/v1/albums") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(requestBody))
            }

            if (response.status.isSuccess()) {
                val albumResponse = json.decodeFromString<AlbumResponse>(response.bodyAsText())
                Result.Success(albumResponse.id)
            } else {
                val errorBody = response.bodyAsText()
                println("Failed to create album: ${response.status}")
                println("Response: $errorBody")
                val message = parseErrorMessage(errorBody).let {
                    if (it.isNullOrBlank()) "" else "$it "
                }
                Result.Error("$message(${response.status})")
            }
        } catch (e: Exception) {
            println("Exception creating album: ${e.message}")
            Result.Exception(e)
        }
    }

    /**
     * Uploads a photo file and returns the upload token
     * @return Result with Upload token if successful, Error message otherwise
     */
    suspend fun uploadPhoto(photo: Photo): Result<String, String> {
        return try {
            // Upload the bytes to Google Photos
            val response: HttpResponse =
                    client.post("https://photoslibrary.googleapis.com/v1/uploads") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $accessToken")
                            append(
                                "X-Goog-Upload-Content-Type", FileUtils.getMimeType(photo.name)
                            )
                            append("X-Goog-Upload-Protocol", "raw")
                            append("X-Goog-Upload-File-Name", photo.name)
                        }
                        contentType(ContentType.Application.OctetStream)

                        setBody(photoChannelContent(photo))
                    }

            if (response.status.isSuccess()) {
                Result.Success(response.bodyAsText()) // The upload token is returned as plain text
            } else {
                val errorBody = response.bodyAsText()
                println("Failed to upload photo: ${response.status}")
                println("Response: $errorBody")
                val message = parseErrorMessage(errorBody).let {
                    if (it.isNullOrBlank()) "" else "$it "
                }
                Result.Error("$message(${response.status})")
            }
        } catch (e: Exception) {
            println("Exception uploading photo: ${e.message}")
            Result.Exception(e)
        }
    }

    /**
     *  BRIDGE: Open the source and stream it to Ktor's ByteReadChannel
     *  @return OutgoingContent.WriteChannelContent with the photo's bytes
     */
    private fun photoChannelContent(photo: Photo): OutgoingContent.WriteChannelContent =
            object : OutgoingContent.WriteChannelContent() {
                override val contentType = ContentType.Application.OctetStream

                override suspend fun writeTo(channel: ByteWriteChannel) {
                    // 1. Open the source safely
                    platformFileSystem.source(photo.kmpFile, context).use { source ->
                        val buffer = Buffer()
                        // 2. Stream data in chunks
                        while (true) {
                            val bytesRead = source.read(buffer, 8192) // Read 8KB
                            if (bytesRead == -1L) break

                            // 3. Write to Ktor's output channel
                            // buffer.readByteArray() extracts the bytes we just read
                            channel.writeFully(buffer.readByteArray())
                        }
                    }
                }
            }

    /**
     * Adds uploaded photos to an album in batches of 50 (API limit)
     */
    suspend fun addPhotosToAlbum(
        albumId: String,
        newMediaItems: List<NewMediaItem>,
    ): Result<List<MediaItemResult>, String> {
        val batchSize = GOOGLE_PHOTO_BATCH_SIZE
        val allResults = mutableListOf<MediaItemResult>()

        try {
            newMediaItems.chunked(batchSize).forEachIndexed { batchIndex, batch ->
                val requestBody = BatchCreateMediaItemsRequest(
                    albumId = albumId, newMediaItems = batch
                )

                val response: HttpResponse =
                        client.post("https://photoslibrary.googleapis.com/v1/mediaItems:batchCreate") {
                            header(HttpHeaders.Authorization, "Bearer $accessToken")
                            contentType(ContentType.Application.Json)
                            setBody(json.encodeToString(requestBody))
                        }

                if (response.status.isSuccess()) {
                    val result =
                            json.decodeFromString<BatchCreateMediaItemsResponse>(response.bodyAsText())
                    allResults.addAll(result.newMediaItemResults)
                    val successCount = result.newMediaItemResults.count { it.status.code == 0 }
                    val failCount = result.newMediaItemResults.count { it.status.code != 0 }

                    println("      Batch ${batchIndex + 1}: $successCount succeeded, $failCount failed")

                    // Log any failures
                    result.newMediaItemResults.filter { it.status.code != 0 }.forEach {
                        println("      Failed item: ${it.status.message}")
                    }
                } else {
                    val errorBody = response.bodyAsText()
                    println("Failed to add photos to album: ${response.status}")
                    println("Response: $errorBody")
                    val message = parseErrorMessage(errorBody).let {
                        if (it.isNullOrBlank()) "" else "$it "
                    }
                    return Result.Error("$message(${response.status})")
                }
            }
        } catch (e: Exception) {
            println("Exception adding photos to album: ${e.message}")
            return Result.Exception(e)
        }
        return Result.Success(allResults)
    }

    suspend fun updateAlbumCover(albumId: String, coverMediaItemId: String) {
        try {
            val response: HttpResponse =
                    // Gemini suggested "https://photoslibrary.googleapis.com/v1/albums/$albumId?updateMask=cover_photo_media_item_id"
                    client.patch("https://photoslibrary.googleapis.com/v1/albums/$albumId?updateMask=coverPhotoMediaItemId") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(UpdateAlbumCoverRequest(coverMediaItemId)))
                    }

            if (!response.status.isSuccess()) {
                println("Failed to update cover photo: ${response.status}")
                println("Response: ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            println("Exception updating cover photo: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun parseErrorMessage(responseBody: String): String? {
        return try {
            val errorResponse = json.decodeFromString<GooglePhotosErrorResponse>(responseBody)
            errorResponse.error.message
        } catch (e: Exception) {
            println("Parsing error response failed: ${e.message}")
            null
        }
    }
}
