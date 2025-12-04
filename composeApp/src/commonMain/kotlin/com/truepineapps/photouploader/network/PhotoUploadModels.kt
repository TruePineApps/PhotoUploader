package com.truepineapps.photouploader.network

import kotlinx.serialization.Serializable

// Data classes for API requests and responses

@Serializable
data class CreateAlbumRequest(
    val album: AlbumData
)

@Serializable
data class AlbumData(
    val title: String
)

@Serializable
data class CreateAlbumResponse(val id: String? = null)

@Serializable
data class AlbumResponse(
    val id: String,
    val title: String,
    val productUrl: String? = null,
    val isWriteable: Boolean? = null
)

@Serializable
data class BatchCreateMediaItemsRequest(
    val albumId: String,
    val newMediaItems: List<NewMediaItem>
)

@Serializable
data class NewMediaItem(
    val description: String,
    val simpleMediaItem: SimpleMediaItem
)

@Serializable
data class SimpleMediaItem(
    val fileName: String,
    val uploadToken: String
)

@Serializable
data class BatchCreateMediaItemsResponse(
    val newMediaItemResults: List<MediaItemResult>
)

@Serializable
data class MediaItemResult(
    val uploadToken: String,
    val status: StatusInfo,
    val mediaItem: MediaItem? = null
)

@Serializable
data class StatusInfo(
    val code: Int,
    val message: String? = null
)

@Serializable
data class MediaItem(
    val id: String,
    val productUrl: String? = null,
    val mimeType: String? = null,
    val filename: String? = null
)

data class UploadedPhoto(
    val uploadToken: String,
    val fileName: String
)