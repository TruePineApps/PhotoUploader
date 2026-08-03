/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader.foundation.auth.domain.model

import com.truepineapps.photouploader.core.util.UiText

sealed class AuthException(val uiText: UiText, val status: Int? = null) :
    Exception(uiText.toString()) {

    class SignInFailed(uiText: UiText, status: Int? = null) :
        AuthException(uiText, status)

    class TokenExpired(uiText: UiText, status: Int? = null) :
        AuthException(uiText, status)

    class NetworkError(uiText: UiText, status: Int? = null) :
        AuthException(uiText, status)
}