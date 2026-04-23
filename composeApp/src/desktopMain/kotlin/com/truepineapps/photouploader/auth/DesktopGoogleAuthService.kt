package com.truepineapps.photouploader.auth

import co.touchlab.kermit.Logger
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.truepineapps.photouploader.core.util.UiTextResource
import com.truepineapps.photouploader.core.util.UiTextString
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_sign_in_failed
import com.truepineapps.photouploader.resources.network_error
import com.truepineapps.photouploader.resources.session_expired
import com.truepineapps.photouploader.resources.unknown_error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStreamReader
import kotlin.coroutines.cancellation.CancellationException

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
class DesktopGoogleAuthService(
    private val log: Logger,
) : GoogleAuthService {

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
            httpTransport, jsonFactory, clientSecrets, scopes
        ).setDataStoreFactory(dataStoreFactory)
            .setAccessType(ACCESS_TYPE_OFFLINE)
            .build()
    }

    override suspend fun signIn(): UserProfile? {
        // Check if we are already signed in.
        val existingProfile = restoreSignIn()
        if (existingProfile != null) {
            return existingProfile
        }

        return try {
            withContext(Dispatchers.IO) {
                /*  Create an embedded server to receive the authorization code. */

                // LocalServerReceiver will automatically find an available free port.
                // Google Cloud accepts any port on localhost for Desktop Client IDs.
                val receiver = LocalServerReceiver.Builder().build()

                // Launch a separate child coroutine to monitor cancellation.
                // If this 'withContext' block is canceled, this child is canceled immediately.
                // Its 'finally' block will run instantly, allowing us to kill the receiver.
                val cancellationMonitor = launch {
                    try {
                        // Suspend indefinitely until canceled
                        awaitCancellation()
                    } finally {
                        // This runs immediately when the job is canceled
                        try {
                            receiver.stop()
                        } catch (e: Exception) {
                            log.d(e) { "signIn CancelMonitor: Failed to stop receiver" }
                        }
                    }
                }

                try {
                    // AuthorizationCodeInstalledApp...authorize() blocks the thread.
                    // Because we are in withContext(Dispatchers.IO), if the parent job is canceled
                    // (e.g. user clicks cancel in UI), this block gets a CancellationException.
                    // To make it responsive, we wrap the blocking call.
                    // To support cancellation, we should run it interruptingly.
                    runInterruptible(Dispatchers.IO) {
                        try {
                            // Trigger the sign-in flow: Open the browser, wait for the callback,
                            // and shut it down. Use the persisted data store named USER.
                            val credential = AuthorizationCodeInstalledApp(getFlow(), receiver)
                                .authorize(USER)

                            if (credential != null && cancellationMonitor.isActive) {
                                fetchUserProfile(credential)
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            if (cancellationMonitor.isCancelled) {
                                // Canceling hard stops the receiver, which may throw any error.
                                // Ignore these side effects.
                                return@runInterruptible null
                            } else {
                                throw e
                            }
                        }
                    }
                } finally {
                    /* Cleanup */

                    // Cancel the monitor so it doesn't hang around
                    cancellationMonitor.cancel()

                    // Make sure the receiver is stopped as soon as possible
                    try {
                        receiver.stop()
                    } catch (e: Exception) {
                        log.e(e) { "signIn: Failed to stop receiver" }
                    }
                }
            }
        } catch (e: CancellationException) {
            log.d { "Sign-in canceled via UI: ${e.message}" }
            throw e
        } catch (e: Exception) {
            handleException(e)
            null // Unreachable if handleException always throws, but keeps compiler happy
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

    /**
     * Attempts to restore a user session by loading previously stored credentials.
     *
     * This function checks for locally saved credentials for the user. If valid credentials are found
     * and the access token is not about to expire (has more than 60 seconds of validity), it attempts
     * to fetch the user's profile information.
     * Invalid credentials are deleted.
     *
     * @return A [UserProfile] object if a valid session is successfully restored and the user profile
     *         is fetched. Returns `null` if no credentials are found, if the token is expired, or if
     *         fetching the user profile fails for any reason (e.g., the token was revoked).
     */
    override suspend fun restoreSignIn(): UserProfile? {
        val flow = getFlow()
        val credential = flow.loadCredential(USER)
        return if (credential == null) {
            null
        } else if ((credential.expiresInSeconds ?: 0) > 60) {
            try {
                fetchUserProfile(credential)
            } catch (e: AuthException.TokenExpired) {
                log.d { "restoreSignIn: Delete token because expired exception: ${e.message}" }
                flow.credentialDataStore.delete(USER)
                null
            } catch (e: Exception) {
                log.e(e) { "Failed to fetch user profile" }
                // If fetching profile fails (e.g. invalid token), we just return null to indicate
                // not signed in
                null
            }
        } else {
            log.d { "restoreSignIn: Delete token because expired" }
            flow.credentialDataStore.delete(USER)
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
        val userInfo = response.parseAs(java.util.HashMap::class.java)

        val name = userInfo["name"] as? String ?: "Google User"
        val email = userInfo["email"] as? String
        val picture = userInfo["picture"] as? String

        // Note: executing the request might refresh the token, so we get the access token from the credential AFTER the request
        return UserProfile(name, email, picture, credential.accessToken)
    }

    private fun handleException(e: Exception) {
        if (e is AuthException) throw e // Re-throw our own exceptions immediately
        log.e(e) { "Auth Exception" }

        when (e) {
            is TokenResponseException -> {
                // This is the most specific token error from Google's OAuth library (e.g., from refresh token attempts)
                val statusCode = e.statusCode
                val errorDescription = e.details?.errorDescription
                val error = e.details?.error // Specific OAuth error code like "invalid_grant"

                checkAndThrowTokenExpired(statusCode, error, errorDescription)

                // If not a token expired issue, fallback to a generic network error
                val message = errorDescription ?: e.statusMessage ?: Res.string.network_error.toString()
                throw AuthException.NetworkError(
                    UiTextResource(Res.string.error_sign_in_failed, UiTextString(message)),
                    statusCode
                )
            }
            is GoogleJsonResponseException -> {
                // This exception occurs for errors from Google APIs that return a JSON error body.
                // It is more specific than HttpResponseException.
                val statusCode = e.statusCode
                val errorDescription = e.details?.message // Generic message from Google API
                // GoogleJsonResponseException.details has `errors` (a list of ErrorInfo), not `error` directly.
                val firstErrorReason = e.details?.errors?.firstOrNull()?.reason // More specific error reason

                // Check if this error indicates token expiry/revocation based on status code and reason
                checkAndThrowTokenExpired(statusCode, firstErrorReason, errorDescription)

                // If not a token expired issue, fallback to a generic network error
                val message = errorDescription ?: Res.string.network_error.toString()
                throw AuthException.NetworkError(
                    UiTextResource(Res.string.error_sign_in_failed, UiTextString(message)),
                    statusCode
                )
            }
            is HttpResponseException -> {
                // This is the most generic HTTP response exception.
                // It will catch any HTTP error not specifically handled by the above Google-specific exceptions.
                val statusCode = e.statusCode
                val message = e.statusMessage ?: Res.string.network_error.toString()
                // No specific 'error' field in generic HttpResponseException

                // Check for generic 401/403 (unauthorized/forbidden) which strongly imply auth issues
                checkAndThrowTokenExpired(statusCode, null, message)

                // If not a token expired issue, fallback to a generic network error
                throw AuthException.NetworkError(
                    UiTextResource(Res.string.error_sign_in_failed, UiTextString(message)),
                    statusCode
                )
            }
            else -> {
                // Catch-all for any other unhandled exceptions
                val uiText = if (e.message == null) {
                    UiTextResource(Res.string.unknown_error)
                } else {
                    UiTextString(e.message!!)
                }
                throw AuthException.SignInFailed(uiText)
            }
        }
    }

    /**
     * Helper to determine if an HTTP error indicates an expired/revoked token.
     * Throws [AuthException.TokenExpired] if conditions are met.
     */
    private fun checkAndThrowTokenExpired(
        statusCode: Int,
        error: String?, // For specific OAuth error codes like "invalid_grant"
        errorDescription: String?, // For descriptive messages like "Token has been expired or revoked."
    ) {
        val isInvalidGrant = statusCode == 400 && error == "invalid_grant"
        val isExpiredOrRevokedMessage = errorDescription?.contains("expired or revoked", ignoreCase = true) == true
        val isUnauthorizedOrForbidden = statusCode == 401 || statusCode == 403

        if (isInvalidGrant || isExpiredOrRevokedMessage || isUnauthorizedOrForbidden) {
            val message = errorDescription ?: Res.string.session_expired.toString()
            throw AuthException.TokenExpired(
                UiTextResource(Res.string.error_sign_in_failed, UiTextString(message)),
                statusCode
            )
        }
    }
}
