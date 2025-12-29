package com.truepineapps.photouploader.network

import com.truepineapps.photouploader.util.UiText

sealed class UploadException(val uiText: UiText) : Exception(uiText.toString()) {
    class GlobalException(uiText: UiText) : UploadException(uiText)
    class AlbumException(uiText: UiText) : UploadException(uiText)
    class PhotoException(uiText: UiText) : UploadException(uiText)
}
