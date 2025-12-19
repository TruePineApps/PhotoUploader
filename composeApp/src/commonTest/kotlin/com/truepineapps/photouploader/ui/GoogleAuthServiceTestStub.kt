package com.truepineapps.photouploader.ui

import com.truepineapps.photouploader.auth.GoogleAuthService

class GoogleAuthServiceTestStub(
    private val signInToken: String? = "fake_access_token_12345",
    private val restoreToken: String? = null,
    private val signInShouldFail: Boolean = false
) : GoogleAuthService {
    override suspend fun signIn(): String? {
        if (signInShouldFail) {
            throw IllegalStateException("Sign-in failed for test")
        }
        return signInToken
    }

    override suspend fun signOut() {
        // No-op for stub
    }

    override suspend fun restoreSignIn(): String? {
        return restoreToken
    }
}
