package com.truepineapps.photouploader.network

import com.truepineapps.photouploader.util.UiText
import io.ktor.http.HttpStatusCode

sealed class UploadException(val uiText: UiText, val status: HttpStatusCode?) :
    Exception(uiText.toString()) {
    class GlobalException(uiText: UiText, status: HttpStatusCode? = null) :
        UploadException(uiText, status)

    class AlbumException(uiText: UiText, status: HttpStatusCode? = null) :
        UploadException(uiText, status)

    class PhotoException(uiText: UiText, status: HttpStatusCode? = null) :
        UploadException(uiText, status)
}
