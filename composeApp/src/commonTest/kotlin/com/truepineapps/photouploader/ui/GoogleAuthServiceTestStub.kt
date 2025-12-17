package com.truepineapps.photouploader.ui

import com.truepineapps.photouploader.auth.GoogleAuthService

// Reusable stub class for different test scenarios
class GoogleAuthServiceTestStub(
    private val signInToken: String? = null,
    private val restoreToken: String? = null
) : GoogleAuthService {
    override suspend fun signIn(): String? = signInToken
    override suspend fun signOut() {}
    override suspend fun restoreSignIn(): String? = restoreToken
}