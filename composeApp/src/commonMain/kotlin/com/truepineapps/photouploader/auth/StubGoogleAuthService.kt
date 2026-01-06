package com.truepineapps.photouploader.auth

import kotlinx.coroutines.delay

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
        delay(500)
    }

    override suspend fun restoreSignIn(): UserProfile? {
        return null
    }
}
