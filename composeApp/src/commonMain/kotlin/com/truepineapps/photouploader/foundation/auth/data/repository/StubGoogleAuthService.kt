package com.truepineapps.photouploader.foundation.auth.data.repository

import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile
import com.truepineapps.photouploader.foundation.auth.domain.repository.GoogleAuthService
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class StubGoogleAuthService : GoogleAuthService {
    override suspend fun signIn(): UserProfile {
        //delay(1000) // Simulate network delay
        return UserProfile(
            name = "Test User",
            email = "test@example.com",
            avatarUrl = null,
            accessToken = "fake_access_token_12345"
        )
    }

    override suspend fun signOut() {
        delay(500.milliseconds)
    }

    override suspend fun restoreSignIn(): UserProfile? {
        return null
    }

    override fun shutdown() {
        // No-op for stub
    }
}