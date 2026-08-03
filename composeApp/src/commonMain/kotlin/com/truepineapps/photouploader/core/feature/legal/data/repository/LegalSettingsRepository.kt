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

package com.truepineapps.photouploader.core.feature.legal.data.repository

import co.touchlab.kermit.Logger
import com.mohamedrejeb.calf.core.PlatformContext
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.truepineapps.photouploader.core.feature.legal.data.source.LegalLocalDataSource
import com.truepineapps.photouploader.core.feature.legal.data.source.LegalRemoteDataSource
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalContent
import com.truepineapps.photouploader.core.feature.legal.domain.repository.LegalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class LegalSettingsRepository(
    private val settings: Settings,
    private val localDataSource: LegalLocalDataSource,
    private val remoteDataSource: LegalRemoteDataSource,
    private val log: Logger,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : LegalRepository {

    override suspend fun fetchLegalContent(context: PlatformContext): Result<LegalContent> =
        withContext(defaultDispatcher) {
            runCatching {
                val versionDeferred = async { remoteDataSource.fetchLatestVersion() }
                val termsDeferred = async { remoteDataSource.fetchTermsOfService() }
                val privacyDeferred = async { remoteDataSource.fetchPrivacyPolicy() }

                val version = versionDeferred.await().getOrThrow()
                val terms = termsDeferred.await().getOrThrow()
                val privacy = privacyDeferred.await().getOrThrow()

                localDataSource.saveContent(context, version, terms, privacy)
                LegalContent(version, terms, privacy)
            }.recoverCatching { remoteException ->
                log.e(remoteException) { "Remote fetch failed, attempting local fallback" }

                // Attempt local read, and if it fails, associate the remote error
                localDataSource.readContent(context).getOrElse { localException ->
                    localException.addSuppressed(remoteException)
                    throw localException
                }
            }
        }

    override suspend fun getLocalLegalContent(context: PlatformContext): Result<LegalContent> =
        withContext(defaultDispatcher) {
            localDataSource.readContent(context)
        }

    override fun getAcceptedVersion(): String? =
        settings.getStringOrNull(PREF_KEY_ACCEPTED_VERSION)

    override fun saveAcceptedVersion(version: String) {
        settings[PREF_KEY_ACCEPTED_VERSION] = version
    }

    companion object {
        private const val PREF_KEY_ACCEPTED_VERSION = "legal_accepted_version"
    }

}