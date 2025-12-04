package com.truepineapps.photouploader.auth

import kotlinx.coroutines.delay

class StubGoogleAuthService : GoogleAuthService {
    override suspend fun signIn(): String? {
        //delay(1000) // Simulate network delay
        return "fake_access_token_12345" // Simulate successful login
    }

    override suspend fun signOut() {
        delay(500)
    }

    override suspend fun restoreSignIn(): String? {
        return null
    }
}
