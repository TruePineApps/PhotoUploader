/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader.core.feature.legal.data.source

import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalConfig
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalRemoteException
import com.truepineapps.photouploader.core.util.UiTextResource
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_remote_file_not_found
import com.truepineapps.photouploader.resources.legal_version_file
import com.truepineapps.photouploader.resources.privacy_policy
import com.truepineapps.photouploader.resources.terms_of_service
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.jetbrains.compose.resources.StringResource

class LegalRemoteDataSource(
    private val httpClient: HttpClient,
    private val legalConfig: LegalConfig,
    private val log: Logger,
) {
    suspend fun fetchLatestVersion(): Result<String> =
        fetchRemoteContent(legalConfig.versionUrl, Res.string.legal_version_file)

    suspend fun fetchTermsOfService(): Result<String> =
        fetchRemoteContent(legalConfig.termsUrl, Res.string.terms_of_service)

    suspend fun fetchPrivacyPolicy(): Result<String> =
        fetchRemoteContent(legalConfig.privacyPolicyUrl, Res.string.privacy_policy)

    private suspend fun fetchRemoteContent(url: String, name: StringResource): Result<String> =
        runCatching {
            val response = httpClient.get(url)
            if (response.status == HttpStatusCode.OK) {
                response.bodyAsText().trim()
            } else {
                val httpStatus = response.status.value
                log.d("Fetch content for $name returns HTTP status $httpStatus")
                throw LegalRemoteException(
                    UiTextResource(Res.string.error_remote_file_not_found, name, httpStatus)
                )
            }
        }
}