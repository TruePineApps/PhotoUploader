package com.truepineapps.photouploader.ui.util

import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile
import com.truepineapps.photouploader.foundation.auth.domain.repository.GoogleAuthService

class GoogleAuthServiceTestStub(
    private val signInToken: String? = "fake_access_token_12345",
    private val restoreToken: String? = null,
    private val signInShouldFail: Boolean = false
) : GoogleAuthService {
    override suspend fun signIn(): UserProfile? {
        if (signInShouldFail) {
            throw IllegalStateException("Sign-in failed for test")
        }
        return signInToken?.let {
            UserProfile(
                name = "Test User",
                email = "test@example.com",
                avatarUrl = null,
                accessToken = it
            )
        }
    }

    override suspend fun signOut() {
        // No-op for stub
    }

    override suspend fun restoreSignIn(): UserProfile? {
        return restoreToken?.let {
            UserProfile(
                name = "Restored User",
                email = "restored@example.com",
                avatarUrl = null,
                accessToken = it
            )
        }
    }
}
