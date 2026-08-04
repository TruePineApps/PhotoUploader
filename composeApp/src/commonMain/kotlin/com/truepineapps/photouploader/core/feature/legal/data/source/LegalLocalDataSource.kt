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
import com.truepineapps.photouploader.core.feature.settings.domain.model.DEFAULT_LOCALE_FROM_PLATFORM
import com.truepineapps.photouploader.core.feature.settings.domain.repository.UserPreferencesRepository
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import com.truepineapps.photouploader.core.localization.PlatformLocaleProvider
import com.truepineapps.photouploader.core.util.loadResourceFile
import kotlinx.coroutines.flow.first

private const val LEGAL_VERSION = "LEGAL_VERSION"
private const val TERMS_OF_SERVICE = "TERMS"
private const val PRIVACY_POLICY = "PRIVACY"
private const val MARKDOWN_EXT = ".md"

class LegalLocalDataSource(
    private val fileSystem: PlatformFileSystem,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val localeProvider: PlatformLocaleProvider,
    private val log: Logger,
) {
    suspend fun readVersion(context: PlatformContext): Result<String> =
        readDownloadedOrBundled(context, LEGAL_VERSION)

    suspend fun readTerms(context: PlatformContext): Result<String> =
        readLocalizedOrBundled(context, "TERMS")

    suspend fun readPrivacyPolicy(context: PlatformContext): Result<String> =
        readLocalizedOrBundled(context, "PRIVACY")

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
        fileSystem.writeText(TERMS_OF_SERVICE + MARKDOWN_EXT, terms, context)
        fileSystem.writeText(PRIVACY_POLICY + MARKDOWN_EXT, privacy, context)
        log.d { "Legal files updated:\n" +
                "version=$version\n" +
                "terms=${terms.substringBefore("---")}\n" +
                "privacy=${privacy.substringBefore("---")}"
        }
    }

    private suspend fun readLocalizedOrBundled(context: PlatformContext, baseName: String): Result<String> {
        val locale = resolveLocale()
        val isEnglish = locale == null || locale == "en"

        val localizedFileName = "$baseName.$locale$MARKDOWN_EXT"
        val defaultFileName = "$baseName$MARKDOWN_EXT"

        log.d { "Resolving legal document for locale: $locale" }

        // Define prioritized file providers. Using a list allows for a clean, declarative fallback chain.
        val providers = listOfNotNull(
            // 1. Downloaded Updates (Highest Priority)
            if (!isEnglish) suspend { attemptReadDownloaded(localizedFileName, context) } else null,
            suspend { attemptReadDownloaded(defaultFileName, context) },

            // 2. Bundled Resources (Shipped with App - Fallback)
            if (!isEnglish) suspend { attemptReadBundled(localizedFileName) } else null,
            suspend { attemptReadBundled(defaultFileName) }
        )

        // Try providers in order, returning the first successful result
        for (provider in providers) {
            val result = provider()
            if (result.isSuccess) return result
        }

        return Result.failure(Exception("Legal document $baseName not found in any location"))
    }

    private suspend fun readDownloadedOrBundled(context: PlatformContext, fileName: String): Result<String> {
        val downloaded = attemptReadDownloaded(fileName, context)
        if (downloaded.isSuccess) return downloaded
        return attemptReadBundled(fileName)
    }

    /**
     * Resolves the current locale tag based on user preferences and platform settings.
     * @return The language code (e.g., "nl") or null if not resolvable.
     */
    private suspend fun resolveLocale(): String? {
        val preferences = userPreferencesRepository.preferences.first()
        val tag = if (preferences.localeTag == DEFAULT_LOCALE_FROM_PLATFORM) {
            localeProvider.getPlatformLocaleTag()
        } else {
            preferences.localeTag
        }
        return tag?.substringBefore("-")?.lowercase()
    }

    private fun attemptReadDownloaded(fileName: String, context: PlatformContext): Result<String> {
        return runCatching { fileSystem.readText(fileName, context) }
            .onSuccess { log.d { "Found downloaded file: $fileName" } }
    }

    private suspend fun attemptReadBundled(fileName: String): Result<String> {
        return loadResourceFile(fileName)
            .onSuccess { log.d { "Found bundled file: $fileName" } }
    }

}