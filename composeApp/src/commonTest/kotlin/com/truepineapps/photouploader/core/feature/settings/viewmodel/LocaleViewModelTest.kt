package com.truepineapps.photouploader.core.feature.settings.viewmodel

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.truepineapps.photouploader.core.domain.state.DataLoadingState
import com.truepineapps.photouploader.core.feature.settings.domain.model.DEFAULT_LOCALE_FROM_PLATFORM
import com.truepineapps.photouploader.core.feature.settings.domain.model.UserPreferences
import com.truepineapps.photouploader.core.feature.settings.domain.repository.UserPreferencesRepository
import com.truepineapps.photouploader.core.localization.PlatformLocaleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LocaleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val logger = Logger(StaticConfig())

    private class MockPlatformLocaleManager(var systemTag: String) : PlatformLocaleManager {
        var currentTag: String? = null
        var lastSetTag: String? = "not_set"
        var simulateStaleTag = false
        
        override fun setPlatformLocale(localeTag: String?) {
            lastSetTag = localeTag
            currentTag = localeTag
        }
        
        override fun getPlatformLocaleTag(): String {
            if (simulateStaleTag && lastSetTag == null && currentTag == null) {
                // Return old tag if we are simulating staleness after a reset
                return "nl" 
            }
            return currentTag ?: systemTag
        }
    }

    private class MockUserPreferencesRepository : UserPreferencesRepository {
        private val _preferences = MutableStateFlow(UserPreferences.DEFAULTS)
        override val preferences = _preferences.asStateFlow()
        override val loadingState: Flow<DataLoadingState> = flowOf(DataLoadingState.Success)
        override fun prepareReload() {}
        override suspend fun saveLocalePreference(localeTag: String) {
            _preferences.value = _preferences.value.copy(localeTag = localeTag)
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `switching between languages and system correctly handles platform staleness`() = runTest {
        val platformManager = MockPlatformLocaleManager("en") // System is English
        val repository = MockUserPreferencesRepository()
        val viewModel = LocaleViewModel(repository, platformManager, logger)

        // The preferredLocaleState is a StateFlow created with WhileSubscribed.
        // We must collect it to activate the upstream flow and the side effects in onEach.
        backgroundScope.launch { viewModel.preferredLocaleState.collect() }

        // 1. Initial State: System (en)
        advanceUntilIdle()
        assertLocaleState(viewModel, expectedSelected = DEFAULT_LOCALE_FROM_PLATFORM, expectedCurrent = "en")

        // 2. Switch to Dutch (NL)
        repository.saveLocalePreference("nl")
        advanceUntilIdle()
        assertLocaleState(viewModel, expectedSelected = "nl", expectedCurrent = "nl")

        // 3. Switch back to System (normal)
        repository.saveLocalePreference(DEFAULT_LOCALE_FROM_PLATFORM)
        advanceUntilIdle()
        assertLocaleState(viewModel, expectedSelected = DEFAULT_LOCALE_FROM_PLATFORM, expectedCurrent = "en")

        // 4. Switch to Dutch (NL) again
        repository.saveLocalePreference("nl")
        advanceUntilIdle()
        assertLocaleState(viewModel, expectedSelected = "nl", expectedCurrent = "nl")

        // 5. Switch to System, but simulate stale platform tag (reproduces the bug scenario)
        platformManager.simulateStaleTag = true
        repository.saveLocalePreference(DEFAULT_LOCALE_FROM_PLATFORM)
        advanceUntilIdle()

        // With the fix (LocaleState), even if tag is stale ("nl"), the selectedTag changed to "System"
        // so the state is emitted and the UI will recompose using the now-reset platform.
        assertLocaleState(viewModel, expectedSelected = DEFAULT_LOCALE_FROM_PLATFORM, expectedCurrent = "nl")
    }

    private fun assertLocaleState(
        viewModel: LocaleViewModel,
        expectedSelected: String,
        expectedCurrent: String
    ) {
        val state = viewModel.preferredLocaleState.value
        assertEquals(expectedSelected, state.selectedTag, "Selected tag mismatch")
        assertEquals(expectedCurrent, state.currentTag, "Current tag mismatch")
    }
}
