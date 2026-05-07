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