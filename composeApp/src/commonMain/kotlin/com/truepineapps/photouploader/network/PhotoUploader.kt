package com.truepineapps.photouploader.network

import com.mohamedrejeb.calf.core.PlatformContext
import com.truepineapps.photouploader.io.PlatformFileSystem
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.util.FileUtils
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
    suspend fun uploadPhoto(photo: Photo): String? {
        return try {
            // Upload the bytes to Google Photos
            val response: HttpResponse =
                    client.post("https://photoslibrary.googleapis.com/v1/uploads") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $accessToken")
                            append(
                                "X-Goog-Upload-Content-Type",
                                FileUtils.getMimeType(photo.name)
                            )
                            append("X-Goog-Upload-Protocol", "raw")
                            append("X-Goog-Upload-File-Name", photo.name)
                        }
                        contentType(ContentType.Application.OctetStream)

                        setBody(photoChannelContent(photo))
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
    ): List<MediaItemResult>? {
        val batchSize = GOOGLE_PHOTO_BATCH_SIZE
        val allResults = mutableListOf<MediaItemResult>()

        newMediaItems.chunked(batchSize).forEachIndexed { batchIndex, batch ->
            try {
                val requestBody = BatchCreateMediaItemsRequest(
                    albumId = albumId,
                    newMediaItems = batch
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
                    println("Failed to add photos to album: ${response.status}")
                    println("Response: ${response.bodyAsText()}")
                    return null
                }
            } catch (e: Exception) {
                println("Exception adding photos to album (batch ${batchIndex + 1}): ${e.message}")
                e.printStackTrace()
                return null
            }
        }
        return allResults
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
}

