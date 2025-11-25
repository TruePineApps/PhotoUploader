package com.truepine.photouploader.auth

interface GoogleAuthService {
    /**
     * Triggers the sign-in flow.
     * Returns the OAuth 2.0 Access Token (Bearer Token) as a String,
     * or null if sign-in failed or was cancelled.
     */
    suspend fun signIn(): String?
    
    suspend fun signOut()
    
    /**
     * Checks if the user is already signed in and valid.
     * Returns the Access Token if valid, null otherwise.
     */
    suspend fun restoreSignIn(): String?
}
