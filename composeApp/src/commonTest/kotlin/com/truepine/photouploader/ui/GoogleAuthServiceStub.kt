package com.truepine.photouploader.ui

import com.truepine.photouploader.auth.GoogleAuthService

// Reusable stub class for different test scenarios
class GoogleAuthServiceStub(
    private val signInToken: String? = null,
    private val restoreToken: String? = null
) : GoogleAuthService {
    override suspend fun signIn(): String? = signInToken
    override suspend fun signOut() {}
    override suspend fun restoreSignIn(): String? = restoreToken
}