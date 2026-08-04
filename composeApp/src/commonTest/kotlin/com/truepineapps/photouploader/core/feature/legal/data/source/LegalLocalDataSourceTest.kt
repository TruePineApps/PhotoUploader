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
import co.touchlab.kermit.StaticConfig
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.core.domain.state.DataLoadingState
import com.truepineapps.photouploader.core.feature.settings.domain.model.DEFAULT_LOCALE_FROM_PLATFORM
import com.truepineapps.photouploader.core.feature.settings.domain.model.UserPreferences
import com.truepineapps.photouploader.core.feature.settings.domain.repository.UserPreferencesRepository
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import com.truepineapps.photouploader.core.localization.PlatformLocaleProvider
import com.truepineapps.photouploader.ui.util.createTestPlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.Sink
import okio.Source
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val LOCALE_EN = "en"
private const val LOCALE_EN_US = "en-US"
private const val LOCALE_NL = "nl"
private const val LOCALE_NL_NL = "nl-NL"
private const val TERMS_NL_MD = "TERMS.nl.md"
private const val TERMS_MD = "TERMS.md"

class LegalLocalDataSourceTest {

    private val logger = Logger(StaticConfig())
    private lateinit var mockFileSystem: MockFileSystem
    private lateinit var mockLocaleProvider: MockLocaleProvider
    private lateinit var mockUserPreferencesRepository: MockUserPreferencesRepository
    private lateinit var dataSource: LegalLocalDataSource
    private val context = createTestPlatformContext()

    private class MockUserPreferencesRepository : UserPreferencesRepository {
        private val _preferences = MutableStateFlow(UserPreferences.DEFAULTS)
        override val preferences = _preferences.asStateFlow()
        override val loadingState: Flow<DataLoadingState> = flowOf(DataLoadingState.Success)
        override fun prepareReload() {}
        override suspend fun saveLocalePreference(localeTag: String) {
            _preferences.value = UserPreferences(localeTag = localeTag)
        }

        fun setLocale(localeTag: String) {
            _preferences.value = UserPreferences(localeTag = localeTag)
        }
    }

    private class MockFileSystem : PlatformFileSystem {
        val files = mutableMapOf<String, String>()
        val readRequests = mutableListOf<String>()

        override fun readText(fileName: String, context: PlatformContext): String {
            readRequests.add(fileName)
            return files[fileName] ?: throw Exception("File not found: $fileName")
        }

        override fun writeText(fileName: String, text: String, context: PlatformContext) {
            files[fileName] = text
        }

        override fun list(file: KmpFile, context: PlatformContext): List<KmpFile> = emptyList()
        override fun isDir(file: KmpFile, context: PlatformContext): Boolean = false
        override fun isDirectory(file: KmpFile, context: PlatformContext): Boolean = false
        override fun getDisplayName(file: KmpFile, context: PlatformContext): String = ""
        override fun getPath(file: KmpFile, context: PlatformContext): String? = null
        override fun getName(file: KmpFile, context: PlatformContext): String? = null
        override fun source(file: KmpFile, context: PlatformContext): Source =
            throw UnsupportedOperationException()

        override fun sink(file: KmpFile, context: PlatformContext): Sink =
            throw UnsupportedOperationException()
    }

    private class MockLocaleProvider(var localeTag: String?) : PlatformLocaleProvider {
        override fun getPlatformLocaleTag(): String? = localeTag
    }

    @BeforeTest
    fun setUp() {
        mockFileSystem = MockFileSystem()
        mockLocaleProvider = MockLocaleProvider(LOCALE_EN)
        mockUserPreferencesRepository = MockUserPreferencesRepository()
        dataSource = LegalLocalDataSource(
            mockFileSystem,
            mockUserPreferencesRepository,
            mockLocaleProvider,
            logger
        )
    }

    @Test
    fun `readTerms loads Dutch TERMS when app locale is Dutch`() = runTest {
        val termsContent = "Dutch Terms"
        mockUserPreferencesRepository.setLocale(LOCALE_NL_NL)
        mockFileSystem.writeText(TERMS_NL_MD, termsContent, context)

        val result = dataSource.readTerms(context)

        assertTrue(result.isSuccess)
        assertEquals(1, mockFileSystem.readRequests.size)
        assertTrue(mockFileSystem.readRequests.contains(TERMS_NL_MD))
        assertEquals(termsContent, result.getOrNull())
    }

    @Test
    fun `readTerms loads Dutch TERMS when system locale is Dutch and app locale is System`() =
        runTest {
            val termsContent = "Dutch Terms"
            mockUserPreferencesRepository.setLocale(DEFAULT_LOCALE_FROM_PLATFORM)
            mockLocaleProvider.localeTag = LOCALE_NL_NL
            mockFileSystem.writeText(TERMS_NL_MD, termsContent, context)

            val result = dataSource.readTerms(context)

            assertTrue(result.isSuccess)
            assertEquals(1, mockFileSystem.readRequests.size)
            assertTrue(mockFileSystem.readRequests.contains(TERMS_NL_MD))
            assertEquals(termsContent, result.getOrNull())
        }

    @Test
    fun `readTerms falls back to default TERMS when Dutch translation is missing`() = runTest {
        val termsContent = "Default Terms"
        mockLocaleProvider.localeTag = LOCALE_NL
        // TERMS.nl.md not in file system, will try to read it, fail, then try TERMS.md
        mockFileSystem.writeText(TERMS_MD, termsContent, context)

        val result = dataSource.readTerms(context)

        assertTrue(result.isSuccess)
        assertEquals(2, mockFileSystem.readRequests.size)
        assertTrue(mockFileSystem.readRequests.contains(TERMS_NL_MD))
        assertTrue(mockFileSystem.readRequests.contains(TERMS_MD))
        assertEquals(termsContent, result.getOrNull())
    }

    @Test
    fun `readTerms uses default TERMS directly when locale is English`() = runTest {
        val termsContent = "English Terms"
        mockLocaleProvider.localeTag = LOCALE_EN_US
        mockFileSystem.writeText(TERMS_MD, termsContent, context)

        val result = dataSource.readTerms(context)

        assertTrue(result.isSuccess)
        assertEquals(1, mockFileSystem.readRequests.size)
        assertEquals(TERMS_MD, mockFileSystem.readRequests[0])
        assertEquals(termsContent, result.getOrNull())
    }
}
