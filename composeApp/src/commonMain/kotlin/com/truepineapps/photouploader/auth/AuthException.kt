package com.truepineapps.photouploader.auth

import com.truepineapps.photouploader.util.UiText

sealed class AuthException(val uiText: UiText, val status: Int? = null) :
    Exception(uiText.toString()) {
    
    class SignInFailed(uiText: UiText, status: Int? = null) :
        AuthException(uiText, status)

    class TokenExpired(uiText: UiText, status: Int? = null) :
        AuthException(uiText, status)

    class NetworkError(uiText: UiText, status: Int? = null) :
        AuthException(uiText, status)
}
