package com.truepineapps.photouploader.network

import co.touchlab.kermit.Logger
import com.mohamedrejeb.calf.core.PlatformContext
import com.truepineapps.photouploader.io.PlatformFileSystem
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_add_media_items_failed_with_message
import com.truepineapps.photouploader.resources.error_album_creation_failed_with_message
import com.truepineapps.photouploader.resources.error_sign_in_failed
import com.truepineapps.photouploader.resources.error_upload_failed_with_message
import com.truepineapps.photouploader.util.FileUtils
import com.truepineapps.photouploader.util.UiTextResource
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
import io.ktor.http.HttpStatusCode
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
    private val log: Logger by inject()

    /**
     * Creates an album in Google Photos
     * @return Album ID if successful
     * @throws UploadException.GlobalException for auth errors
     * @throws UploadException.AlbumException for other API errors
     */
    suspend fun createAlbum(albumTitle: String): String {
        val response: HttpResponse = client.post("https://photoslibrary.googleapis.com/v1/albums") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(CreateAlbumRequest(album = AlbumData(title = albumTitle))))
        }

        if (response.status.isSuccess()) {
            val albumResponse = json.decodeFromString<AlbumResponse>(response.bodyAsText())
            return albumResponse.id
        } else {
            handleError(response, isAlbumCreation = true)
        }
    }

    /**
     * Uploads a photo file and returns the upload token
     * @return Upload token if successful
     * @throws UploadException.GlobalException for auth errors
     * @throws UploadException.PhotoException for other API errors
     */
    suspend fun uploadPhoto(photo: Photo): String {
        // Upload the bytes to Google Photos
        val response: HttpResponse =
                client.post("https://photoslibrary.googleapis.com/v1/uploads") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $accessToken")
                        append("X-Goog-Upload-Content-Type", FileUtils.getMimeType(photo.name))
                        append("X-Goog-Upload-Protocol", "raw")
                        append("X-Goog-Upload-File-Name", photo.name)
                    }
                    contentType(ContentType.Application.OctetStream)
                    setBody(photoChannelContent(photo))
                }

        if (response.status.isSuccess()) {
            return response.bodyAsText()
        } else {
            handleError(response)
        }
    }

    /**
     * Adds uploaded photos to an album in batches of 50 (API limit)
     */
    suspend fun addPhotosToAlbum(
        albumId: String,
        newMediaItems: List<NewMediaItem>,
    ): List<MediaItemResult> {
        val batchSize = GOOGLE_PHOTO_BATCH_SIZE
        val allResults = mutableListOf<MediaItemResult>()

        newMediaItems.chunked(batchSize).forEachIndexed { batchIndex, batch ->
            val requestBody = BatchCreateMediaItemsRequest(albumId = albumId, newMediaItems = batch)

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
                val successCount = result.newMediaItemResults.count { it.isSuccess() }
                val failCount = result.newMediaItemResults.count { !it.isSuccess() }

                log.d { "      Batch ${batchIndex + 1}: $successCount succeeded, $failCount failed" }
                result.newMediaItemResults.filter { !it.isSuccess() }.forEach {
                    log.e { "      Failed item: ${it.status}" }
                }
            } else {
                handleError(response, isBatchAdd = true)
            }
        }
        return allResults
    }

    suspend fun updateAlbumCover(albumId: String, coverMediaItemId: String) {
        val response: HttpResponse =
                client.patch("https://photoslibrary.googleapis.com/v1/albums/$albumId?updateMask=coverPhotoMediaItemId") {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(UpdateAlbumCoverRequest(coverMediaItemId)))
                }

        if (!response.status.isSuccess()) {
            handleError(response)
        }
    }

    private fun photoChannelContent(photo: Photo): OutgoingContent.WriteChannelContent =
            object : OutgoingContent.WriteChannelContent() {
                override val contentType = ContentType.Application.OctetStream
                override suspend fun writeTo(channel: ByteWriteChannel) {
                    platformFileSystem.source(photo.kmpFile, context).use { source ->
                        val buffer = Buffer()
                        while (true) {
                            val bytesRead = source.read(buffer, 8192)
                            if (bytesRead == -1L) break
                            channel.writeFully(buffer.readByteArray())
                        }
                    }
                }
            }

    private suspend fun handleError(
        response: HttpResponse,
        isAlbumCreation: Boolean = false,
        isBatchAdd: Boolean = false,
    ): Nothing {
        val errorBody = response.bodyAsText()
        log.e { "Request failed: ${response.status}" }
        log.e { "Response: $errorBody" }

        val message = parseErrorMessage(errorBody).let {
            if (it.isNullOrBlank()) "${response.status}" else "$it (${response.status})"
        }

        when {
            response.status == HttpStatusCode.Unauthorized -> throw UploadException.GlobalException(
                UiTextResource(
                    Res.string.error_sign_in_failed,
                    message
                ),
                response.status
            )
            isAlbumCreation -> throw UploadException.AlbumException(
                UiTextResource(
                    Res.string.error_album_creation_failed_with_message,
                    message
                ),
                response.status
            )

            isBatchAdd -> throw UploadException.PhotoException(
                UiTextResource(
                    Res.string.error_add_media_items_failed_with_message,
                    message
                ),
                response.status
            )

            else -> throw UploadException.PhotoException(
                UiTextResource(
                    Res.string.error_upload_failed_with_message,
                    message
                ),
                response.status
            )
        }
    }

    private fun parseErrorMessage(responseBody: String): String? {
        return try {
            val errorResponse = json.decodeFromString<GooglePhotosErrorResponse>(responseBody)
            errorResponse.error.message
        } catch (e: Exception) {
            log.e(e) { "Parsing error response failed" }
            null
        }
    }
}
