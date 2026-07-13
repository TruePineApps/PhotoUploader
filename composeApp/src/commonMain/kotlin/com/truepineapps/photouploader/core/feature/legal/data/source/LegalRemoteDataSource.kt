package com.truepineapps.photouploader.core.feature.legal.data.source

import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalConfig
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalRemoteException
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

// TODO: String resources to UiText
class LegalRemoteDataSource(
    private val httpClient: HttpClient,
    private val legalConfig: LegalConfig,
    private val log: Logger,
) {
    suspend fun fetchLatestVersion(): Result<String> =
        fetchRemoteContent(legalConfig.versionUrl, "legal version file")

    suspend fun fetchTermsOfService(): Result<String> =
        fetchRemoteContent(legalConfig.termsUrl, "Terms of Service")

    suspend fun fetchPrivacyPolicy(): Result<String> =
        fetchRemoteContent(legalConfig.privacyPolicyUrl, "Privacy Policy")

    private suspend fun fetchRemoteContent(url: String, name: String): Result<String> = runCatching {
        val response = httpClient.get(url)
        if (response.status == HttpStatusCode.OK) {
            response.bodyAsText().trim()
        } else {
            val httpStatus = response.status.value
            log.d("fetch content for $name returns HTTP status $httpStatus")
            throw LegalRemoteException("Remote $name not found ($httpStatus)")
        }
    }
}