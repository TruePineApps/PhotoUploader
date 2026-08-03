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
import com.mohamedrejeb.calf.core.PlatformContext
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalContent
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import com.truepineapps.photouploader.core.util.loadResourceFile

private const val LEGAL_VERSION = "LEGAL_VERSION"

private const val TERMS_OF_SERVICE = "TERMS.md"

private const val PRIVACY_POLICY = "PRIVACY.md"

class LegalLocalDataSource(
    private val fileSystem: PlatformFileSystem,
    private val log: Logger,
) {
    suspend fun readVersion(context: PlatformContext): Result<String> =
        readWritableOrBundled(context, LEGAL_VERSION)

    suspend fun readTerms(context: PlatformContext): Result<String> =
        readWritableOrBundled(context, TERMS_OF_SERVICE)

    suspend fun readPrivacyPolicy(context: PlatformContext): Result<String> =
        readWritableOrBundled(context, PRIVACY_POLICY)

    suspend fun readContent(context: PlatformContext): Result<LegalContent> {
        val version = readVersion(context).getOrElse { return Result.failure(it) }
        val terms = readTerms(context).getOrElse { return Result.failure(it) }
        val privacy = readPrivacyPolicy(context).getOrElse { return Result.failure(it) }
        return Result.success(LegalContent(version, terms, privacy))
    }

    fun saveContent(
        context: PlatformContext,
        version: String,
        terms: String,
        privacy: String
    ) {
        fileSystem.writeText(LEGAL_VERSION, version, context)
        fileSystem.writeText(TERMS_OF_SERVICE, terms, context)
        fileSystem.writeText(PRIVACY_POLICY, privacy, context)
        log.d { "Legal files updated: version=$version\nterms=$terms\nprivacy=$privacy" }
    }

    private suspend fun readWritableOrBundled(context: PlatformContext, fileName: String): Result<String> {
        val written = runCatching { fileSystem.readText(fileName, context) }
        if (written.isSuccess) return written
        // Fall back to the file in commonMain/composeResources/files
        return loadResourceFile(fileName)
    }

}