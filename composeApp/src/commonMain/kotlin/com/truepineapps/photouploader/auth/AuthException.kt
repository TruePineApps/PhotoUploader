package com.truepineapps.photouploader.auth

import com.truepineapps.photouploader.core.util.UiText

sealed class AuthException(val uiText: com.truepineapps.photouploader.core.util.UiText, val status: Int? = null) :
    Exception(uiText.toString()) {
    
    class SignInFailed(uiText: com.truepineapps.photouploader.core.util.UiText, status: Int? = null) :
        AuthException(uiText, status)

    class TokenExpired(uiText: com.truepineapps.photouploader.core.util.UiText, status: Int? = null) :
        AuthException(uiText, status)

    class NetworkError(uiText: com.truepineapps.photouploader.core.util.UiText, status: Int? = null) :
        AuthException(uiText, status)
}
