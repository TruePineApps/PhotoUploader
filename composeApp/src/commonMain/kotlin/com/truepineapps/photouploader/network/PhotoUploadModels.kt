package com.truepineapps.photouploader.network

import kotlinx.serialization.Serializable

// Data classes for API requests and responses

@Serializable
data class CreateAlbumRequest(
    val album: AlbumData,
)

@Serializable
data class AlbumData(
    val title: String,
)

@Serializable
data class AlbumResponse(
    val id: String,
    val title: String,
    val productUrl: String? = null,
    val isWriteable: Boolean? = null,
)

@Serializable
data class BatchCreateMediaItemsRequest(
    val albumId: String,
    val newMediaItems: List<NewMediaItem>,
)

@Serializable
data class NewMediaItem(
    val description: String,
    val simpleMediaItem: SimpleMediaItem,
)

@Serializable
data class SimpleMediaItem(
    val fileName: String,
    val uploadToken: String,
)

@Serializable
data class BatchCreateMediaItemsResponse(
    val newMediaItemResults: List<MediaItemResult>,
)

/** Result of creating a new media item.
 * NewMediaItemResult on https://developers.google.com/photos/library/reference/rest/v1/mediaItems/batchCreate
 */
@Serializable
data class MediaItemResult(
    /** The upload token used to create th new media item. Only populated if the media item is simple and required a single upload token. */
    val uploadToken: String? = null,
    /** If an error occurred during the creation of the media item, this field is populated with information related to the error. */
    val status: StatusInfo? = null,
    /** Media item created with the upload token. It's populated if no errors occurred and the media item was created successfully. */
    val mediaItem: MediaItem? = null,
) {
    fun isSuccess() = status == null || status.code == null || status.code == 0
}

@Serializable
data class StatusInfo(
    val code: Int? = null,
    val message: String? = null,
) {
    override fun toString(): String {
        if (code == null) {
            return message ?: ""
        }
        return "$code: $message"
    }
}

@Serializable
data class MediaItem(
    val id: String,
    val productUrl: String? = null,
    val mimeType: String? = null,
    val filename: String? = null,
)

@Serializable
data class UpdateAlbumCoverRequest(
    val coverPhotoMediaItemId: String,
)

@Serializable
data class GooglePhotosErrorResponse(val error: GooglePhotosErrorContent)

@Serializable
data class GooglePhotosErrorContent(val code: Int, val message: String, val status: String)
