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

package com.truepineapps.photouploader.foundation.auth.domain.repository

import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile
import com.truepineapps.photouploader.foundation.auth.domain.model.AuthException

/**
 * A flow to sign in and out of Google using OAuth2
 */
interface GoogleAuthService {
    /**
     * Triggers the sign-in flow.
     * If the user is already signed in and valid, then the valid token will be returned.
     * @Return the User Profile containing the OAuth 2.0 Access Token, or null if sign-in
     * was canceled by the user.
     * @Throws [AuthException] if the sign-in process fails due to network or other errors.
     */
    suspend fun signIn(): UserProfile?

    suspend fun signOut()

    /**
     * Checks if the user is already signed in and valid.
     * @Return the User Profile containing the Access Token if valid, null otherwise.
     */
    suspend fun restoreSignIn(): UserProfile?

    /**
     * Cleanup any open http activity
     */
    fun shutdown()
}