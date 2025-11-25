package com.truepine.photouploader.auth

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import java.io.File
import java.io.InputStreamReader

// User ID, the library uses this string to name the file where it saves the Access Token
private const val USER = "user"

class DesktopGoogleAuthService : GoogleAuthService {

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()

    // Scopes determine the level of access you are requesting. Here we want to append to Google Photo.
    // See https://developers.google.com/photos/overview/authorization
    private val scopes = listOf(
        "https://www.googleapis.com/auth/photoslibrary.appendonly",
        "https://www.googleapis.com/auth/photoslibrary.readonly.appcreateddata"
    )

    // Directory to store user credentials for this application.
    private val credentialsFolder =
            File(System.getProperty("user.home"), ".credentials/photouploader")
    private val dataStoreFactory = FileDataStoreFactory(credentialsFolder)

    private fun getFlow(): GoogleAuthorizationCodeFlow {
        // Load client secrets.
        // TODO: set client_id, project_id and client_secret.
        val secretsStream =
                DesktopGoogleAuthService::class.java.getResourceAsStream("/client_secrets.json")
                    ?: throw IllegalStateException("client_secrets.json not found in resources")

        val clientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(secretsStream))

        // Build the flow and set up the data store factory.
        return GoogleAuthorizationCodeFlow.Builder(
            httpTransport,
            jsonFactory,
            clientSecrets,
            scopes
        )
            .setDataStoreFactory(dataStoreFactory)
            .setAccessType("offline")
            .build()
    }

    override suspend fun signIn(): String? {
        // Check if we are already signed in.
        val existingToken = restoreSignIn()
        if (existingToken != null) {
            return existingToken
        }

        // Create am embedded server to receive the authorization code.
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        // Trigger the sign-in flow: Open the browser, wait for the callback, and shut it down. Use
        // the persisted data store named USER.
        val credential = AuthorizationCodeInstalledApp(getFlow(), receiver).authorize(USER)
        // Return the access token for Google Photo.
        return credential?.accessToken
    }

    override suspend fun signOut() {
        // The library handles token storage. To "sign out", we just delete the stored credentials.
        val flow = getFlow()
        val userCredential = flow.loadCredential(USER)
        if (userCredential != null) {
            flow.credentialDataStore.delete(USER)
        }
    }

    override suspend fun restoreSignIn(): String? {
        val credential = getFlow().loadCredential(USER)
        return if (credential != null && (credential.expiresInSeconds ?: 0) > 60) {
            credential.accessToken
        } else {
            null
        }
    }
}
