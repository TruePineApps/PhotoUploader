package com.truepineapps.photouploader.feature.uploader.data.repository

import co.touchlab.kermit.Logger
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import com.truepineapps.photouploader.core.util.FileUtils
import com.truepineapps.photouploader.core.util.ServiceUtil
import com.truepineapps.photouploader.core.util.UiTextResource
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_add_media_items_failed_with_message
import com.truepineapps.photouploader.resources.error_album_creation_failed_with_message
import com.truepineapps.photouploader.resources.error_network_auth_required
import com.truepineapps.photouploader.resources.error_sign_in_failed
import com.truepineapps.photouploader.resources.error_upload_failed_with_message
import com.truepineapps.photouploader.feature.uploader.data.dto.AlbumData
import com.truepineapps.photouploader.feature.uploader.data.dto.AlbumResponse
import com.truepineapps.photouploader.feature.uploader.data.dto.BatchCreateMediaItemsRequest
import com.truepineapps.photouploader.feature.uploader.data.dto.BatchCreateMediaItemsResponse
import com.truepineapps.photouploader.feature.uploader.data.dto.CreateAlbumRequest
import com.truepineapps.photouploader.feature.uploader.data.dto.GooglePhotosErrorResponse
import com.truepineapps.photouploader.feature.uploader.data.dto.MediaItemResult
import com.truepineapps.photouploader.feature.uploader.data.dto.NewMediaItem
import com.truepineapps.photouploader.feature.uploader.data.dto.UpdateAlbumCoverRequest
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoUploader
import com.truepineapps.photouploader.feature.uploader.domain.repository.UploadException
import io.ktor.client.HttpClient
import io.ktor.client.request.get
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

private const val BUFFER_SIZE = 8192L

class PhotoUploaderImpl(
    val accessToken: String,
    val context: PlatformContext,
) : KoinComponent, PhotoUploader {
    private val platformFileSystem: PlatformFileSystem by inject()
    private val client: HttpClient by inject()
    private val json: Json by inject()
    private val log: Logger by inject()

    /**
     * Verifies if an album with the given ID exists on Google Photos.
     * @return `true` if the album exists, `false` if it was not found (404).
     * @throws com.truepineapps.photouploader.feature.uploader.domain.repository.UploadException.GlobalException for other HTTP errors.
     */
    override suspend fun verifyAlbumExists(
        albumId: String,
        serviceUtil: ServiceUtil
    ): Boolean {
        val response: HttpResponse =
            client.get("https://photoslibrary.googleapis.com/v1/albums/$albumId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }

        return when {
            response.status.isSuccess() -> true
            response.status == HttpStatusCode.NotFound -> false
            else -> {
                var result = false
                handleError(
                    response = response,
                    serviceUtil = serviceUtil,
                    isAlbumCreation = true
                ) {
                    result = verifyAlbumExists(albumId, serviceUtil)
                }
                result
            }
        }
    }

    /**
     * Creates an album in Google Photos
     * @return Album ID if successful
     * @throws com.truepineapps.photouploader.feature.uploader.domain.repository.UploadException.GlobalException for auth errors
     * @throws com.truepineapps.photouploader.feature.uploader.domain.repository.UploadException.AlbumException for other API errors
     */
    override suspend fun createAlbum(
        albumTitle: String,
        serviceUtil: ServiceUtil
    ): String {
        val response: HttpResponse = client.post("https://photoslibrary.googleapis.com/v1/albums") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(
                CreateAlbumRequest(
                    album = AlbumData(
                        title = albumTitle
                    )
                )
            ))
        }

        return if (response.status.isSuccess()) {
            val albumResponse = json.decodeFromString<AlbumResponse>(response.bodyAsText())
            albumResponse.id
        } else {
            var result = ""
            handleError(response = response, serviceUtil = serviceUtil, isAlbumCreation = true) {
                result = createAlbum(albumTitle, serviceUtil)
            }
            result
        }
    }

    /**
     * Uploads a photo file and returns the upload token
     * @return Upload token if successful
     * @throws com.truepineapps.photouploader.feature.uploader.domain.repository.UploadException.GlobalException for auth errors
     * @throws com.truepineapps.photouploader.feature.uploader.domain.repository.UploadException.PhotoException for other API errors
     */
    override suspend fun uploadPhoto(
        photoName: String,
        kmpFile: KmpFile,
        serviceUtil: ServiceUtil
    ): String {
        // Upload the bytes to Google Photos
        val response: HttpResponse =
            client.post("https://photoslibrary.googleapis.com/v1/uploads") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                    append("X-Goog-Upload-Content-Type", FileUtils.getMimeType(photoName))
                    append("X-Goog-Upload-Protocol", "raw")
                    append("X-Goog-Upload-File-Name", photoName)
                }
                contentType(ContentType.Application.OctetStream)
                setBody(photoChannelContent(kmpFile))
            }

        return if (response.status.isSuccess()) {
            response.bodyAsText()
        } else {
            var result = ""
            handleError(response, serviceUtil) {
                result = uploadPhoto(photoName, kmpFile, serviceUtil)
            }
            result
        }
    }

    /**
     * Adds uploaded photos to an album in batches of 50 (API limit)
     */
    override suspend fun addPhotosToAlbum(
        albumId: String,
        newMediaItems: List<NewMediaItem>,
    ): List<MediaItemResult> {
        val batchSize =
            GOOGLE_PHOTO_BATCH_SIZE
        val allResults = mutableListOf<MediaItemResult>()

        newMediaItems.chunked(batchSize).forEachIndexed { batchIndex, batch ->
            addOneBatchOfPhotosToAlbum(albumId, batchIndex, batch, allResults)
        }
        return allResults
    }

    private suspend fun addOneBatchOfPhotosToAlbum(
        albumId: String,
        batchIndex: Int,
        batch: List<NewMediaItem>,
        allResults: MutableList<MediaItemResult>,
        serviceUtil: ServiceUtil = ServiceUtil(
            "addOneBatchOfPhotosToAlbum (batch=$batchIndex)"
        )
    ) {
        val requestBody =
            BatchCreateMediaItemsRequest(
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
            val successCount = result.newMediaItemResults.count { it.isSuccess() }
            val failCount = result.newMediaItemResults.count { !it.isSuccess() }

            log.d { "addPhotosToAlbum:       Batch ${batchIndex + 1}: $successCount succeeded, $failCount failed" }
            result.newMediaItemResults.filter { !it.isSuccess() }.forEach {
                log.e { "addPhotosToAlbum:       Failed item: ${it.status}" }
            }
        } else {
            handleError(response = response, serviceUtil = serviceUtil, isBatchAdd = true) {
                addOneBatchOfPhotosToAlbum(albumId, batchIndex, batch, allResults, serviceUtil)
            }
        }
    }

    override suspend fun updateAlbumCover(
        albumId: String, coverMediaItemId: String,
        serviceUtil: ServiceUtil
    ) {
        val response: HttpResponse =
            client.patch("https://photoslibrary.googleapis.com/v1/albums/$albumId?updateMask=coverPhotoMediaItemId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(
                    UpdateAlbumCoverRequest(
                        coverMediaItemId
                    )
                ))
            }

        if (!response.status.isSuccess()) {
            handleError(response = response, serviceUtil = serviceUtil) {
                updateAlbumCover(albumId, coverMediaItemId, serviceUtil)
            }
        }
    }

    private fun photoChannelContent(kmpFile: KmpFile): OutgoingContent.WriteChannelContent =
        object : OutgoingContent.WriteChannelContent() {
            override val contentType = ContentType.Application.OctetStream
            override suspend fun writeTo(channel: ByteWriteChannel) {
                platformFileSystem.source(kmpFile, context).use { source ->
                    val buffer = Buffer()
                    while (true) {
                        val bytesRead = source.read(buffer,
                            BUFFER_SIZE
                        )
                        if (bytesRead == -1L) break
                        channel.writeFully(buffer.readByteArray())
                    }
                }
            }
        }

    /**
     * Handles error responses from the Google Photos API by logging the error details. In case the
     * request should be retried, an exponential backoff is applied before calling the [retryAction].
     *
     * @param response The [HttpResponse] representing the error response.
     * @param serviceUtil An instance of [ServiceUtil] for handling exponential backoff.
     * @param isAlbumCreation A flag indicating whether the error is related to album creation.
     * @param isBatchAdd A flag indicating whether the error is related to batch adding photos.
     * @param retryAction An optional lambda to be executed if the request should be retried.
     */
    private suspend fun handleError(
        response: HttpResponse,
        serviceUtil: ServiceUtil,
        isAlbumCreation: Boolean = false,
        isBatchAdd: Boolean = false,
        retryAction: suspend () -> Unit = {}
    ) {
        val errorBody = response.bodyAsText()
        log.e { "handleError: Request failed: ${response.status}" }
        log.e { "handleError: Response: $errorBody" }

        /* First test for status codes that allow a retry.
           Status codes marked for exponential backoff in Google Photos API docs https://docs.cloud.google.com/storage/docs/json_api/v1/status-codes:
           408 (Timeout), 429 (Rate Limit)  HttpStatusCode.RequestTimeout and HttpStatusCode.TooManyRequests,
           and in the 5xx range HttpStatusCode.InternalServerError, HttpStatusCode.BadGateway,
           HttpStatusCode.ServiceUnavailable and HttpStatusCode.GatewayTimeout.
           However, Google occasionally uses non-standard 5xx codes for internal load balancing.
           Therefore, test on 408 (Timeout), 429 (Rate Limit), and all 5xx (except 511, see below).
        */
        if (response.status == HttpStatusCode.RequestTimeout
            || response.status == HttpStatusCode.TooManyRequests
            || (response.status.value in 500..599 && response.status.value != 511)
        ) {
            // Retry the request with exponential backoff
            if (serviceUtil.exponentialBackoffDelay()) {
                retryAction()
                return
            }
            // Max attempts were reached. Fall through to throw exception.
        }

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

            // HTTP 511 "Network Auth Required" is a special case: it's a network issue, not a Google issue.
            // Retrying won't help until the user acts.
            response.status.value == 511 -> {
                throw UploadException.PhotoException(
                    UiTextResource(Res.string.error_network_auth_required),
                    response.status
                )
            }

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
            log.e(e) { "parseErrorMessage: Parsing error response failed" }
            null
        }
    }

}
