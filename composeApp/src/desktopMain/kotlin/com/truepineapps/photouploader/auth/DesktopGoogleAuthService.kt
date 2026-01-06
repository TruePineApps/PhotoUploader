package com.truepineapps.photouploader.auth

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_sign_in_failed
import com.truepineapps.photouploader.resources.network_error
import com.truepineapps.photouploader.resources.unknown_error
import com.truepineapps.photouploader.util.UiTextResource
import com.truepineapps.photouploader.util.UiTextString
import java.io.File
import java.io.InputStreamReader

// User ID, the library uses this string to name the file where it saves the Access Token
private const val USER = "user"

private const val ACCESS_TYPE_OFFLINE = "offline"

private const val CLIENT_SECRETS_JSON = "client_secrets.json"

/**
 * A concrete implementation of [GoogleAuthService] tailored for desktop environments.
 *
 * This service handles the OAuth 2.0 authentication flow specifically for desktop applications,
 * utilizing a local Jetty server to receive authorization callbacks. It manages the lifecycle
 * of Google API credentials, including:
 * - Initiating the OAuth flow via the system browser.
 * - Securely storing and retrieving credentials locally in the user's home directory.
 * - Refreshing and restoring valid sessions.
 * - Clearing credentials upon sign-out.
 *
 * The service requires a `client_secrets.json` file to be present in the application resources that
 * contains the keys and secrets to connect to the corresponding Google Cloud project that allows
 * access to the Google Photos API.It requests scopes strictly for appending to the Google Photos
 * library and reading app-created data.
 *  this application..
 */
class DesktopGoogleAuthService : GoogleAuthService {

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()

    // Scopes determine the level of access you are requesting. Here we want to append to Google Photo.
    // See https://developers.google.com/photos/overview/authorization
    private val scopes = listOf(
        // To upload the photo bytes, create a media item for a photo, create albums, and add enrichments.
        "https://www.googleapis.com/auth/photoslibrary.appendonly",
        // Read access to media items and albums created by PhotoUploader
        "https://www.googleapis.com/auth/photoslibrary.readonly.appcreateddata",
        // To set the album cover
        "https://www.googleapis.com/auth/photoslibrary.edit.appcreateddata",
        // To get the user's name and avatar
        "https://www.googleapis.com/auth/userinfo.profile",
        // To get the user's email
        "https://www.googleapis.com/auth/userinfo.email"
    )

    // Directory to store user credentials for this application.
    private val credentialsFolder =
            File(System.getProperty("user.home"), ".credentials/photouploader")
    private val dataStoreFactory = FileDataStoreFactory(credentialsFolder)

    private fun getFlow(): GoogleAuthorizationCodeFlow {
        // Load client secrets.
        val secretsStream =
                DesktopGoogleAuthService::class.java.getResourceAsStream("/$CLIENT_SECRETS_JSON")
                    ?: throw IllegalStateException("$CLIENT_SECRETS_JSON not found in resources")

        val clientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(secretsStream))

        // Build the flow and set up the data store factory.
        return GoogleAuthorizationCodeFlow.Builder(
            httpTransport,
            jsonFactory,
            clientSecrets,
            scopes
        )
            .setDataStoreFactory(dataStoreFactory)
            .setAccessType(ACCESS_TYPE_OFFLINE)
            .build()
    }

    override suspend fun signIn(): UserProfile? {
        // Check if we are already signed in.
        val existingProfile = restoreSignIn()
        if (existingProfile != null) {
            return existingProfile
        }

        try {
            // Create am embedded server to receive the authorization code.
            val receiver = LocalServerReceiver.Builder().setPort(8888).build()
            // Trigger the sign-in flow: Open the browser, wait for the callback, and shut it down. Use
            // the persisted data store named USER.
            val credential = AuthorizationCodeInstalledApp(getFlow(), receiver).authorize(USER)
            if (credential == null) {
                println("Failed to get credential")
                return null
            }

            return fetchUserProfile(credential)
        } catch (e: Exception) {
            handleException(e)
            return null // Unreachable if handleException always throws, but keeps compiler happy
        }
    }

    override suspend fun signOut() {
        // The library handles token storage. To "sign out", we just delete the stored credentials.
        val flow = getFlow()
        val userCredential = flow.loadCredential(USER)
        if (userCredential != null) {
            flow.credentialDataStore.delete(USER)
        }
    }

    override suspend fun restoreSignIn(): UserProfile? {
        val credential = getFlow().loadCredential(USER)
        return if (credential != null && (credential.expiresInSeconds ?: 0) > 60) {
            try {
                fetchUserProfile(credential)
            } catch (e: Exception) {
                println("Failed to fetch user profile: ${e.message}")
                // If fetching profile fails (e.g. invalid token), we just return null to indicate not signed in
                null
            }
        } else {
            null
        }
    }

    private fun fetchUserProfile(credential: Credential): UserProfile {
        val requestFactory = httpTransport.createRequestFactory { request ->
            credential.initialize(request)
            request.parser = JsonObjectParser(jsonFactory)
        }
        val url = GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo")
        val request = requestFactory.buildGetRequest(url)
        val response = request.execute()

        // The response parsing depends on how you want to handle JSON.
        // Using a Map is usually simplest without creating a UserInfo class.
        val userInfo = response.parseAs(HashMap::class.java)

        val name = userInfo["name"] as? String ?: "Google User"
        val email = userInfo["email"] as? String
        val picture = userInfo["picture"] as? String

        // Note: executing the request might refresh the token, so we get the access token from the credential AFTER the request
        return UserProfile(name, email, picture, credential.accessToken)
    }

    private fun handleException(e: Exception) {
        e.printStackTrace()
        when (e) {
            is HttpResponseException -> {
                val statusCode = e.statusCode
                val message = e.statusMessage ?: Res.string.network_error

                if (statusCode == 401 || statusCode == 403) {
                    throw AuthException.TokenExpired(
                        UiTextResource(
                            Res.string.error_sign_in_failed,
                            message
                        ), statusCode
                    )
                } else {
                    throw AuthException.NetworkError(
                        UiTextResource(
                            Res.string.error_sign_in_failed,
                            message
                        ), statusCode
                    )
                }
            }

            else -> {
                val uiText = if (e.message == null) {
                    UiTextResource(Res.string.unknown_error)
                } else {
                    UiTextString(e.message!!)
                }
                throw AuthException.SignInFailed(uiText)
            }
        }
    }
}
