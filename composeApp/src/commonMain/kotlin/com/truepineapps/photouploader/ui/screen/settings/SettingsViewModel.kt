/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import com.truepineapps.photouploader.data.preferences.DEFAULT_LOCALE
import com.truepineapps.photouploader.data.preferences.DEFAULT_LOCALE_FROM_PLATFORM
import com.truepineapps.photouploader.data.preferences.UserPreferences
import com.truepineapps.photouploader.data.preferences.UserPreferencesRepository
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.preferences
import com.truepineapps.photouploader.ui.screen.LoadingViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.core.component.KoinComponent

/**
 * ViewModel for managing the settings screen in the app.
 * Note that the locale preference must be set through the LocaleViewModel.
 */
class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : LoadingViewModel(userPreferencesRepository), KoinComponent {

    var settingsUiState: StateFlow<SettingsUiState> =
        userPreferencesRepository.preferences
            .map { preferences -> SettingsUiState.fromPreferences(preferences) }
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = SettingsUiState.fromPreferences(UserPreferences.DEFAULTS)
            )

    // Prevent interference with an already running preference update
    private var updatePreferenceJob: Job? = null

    @Composable
    override fun getDisplayValue() = stringResource(Res.string.preferences)

    fun setLocale(localeTag: String?) {
        updatePreferenceJob?.cancel()
        updatePreferenceJob = viewModelScope.launch {
            userPreferencesRepository.saveLocalePreference(
                if (localeTag.isNullOrBlank()) DEFAULT_LOCALE_FROM_PLATFORM else localeTag
            )
        }
    }

}

/**
 * Represents the settings which the user can edit within the app.
 */
data class SettingsUiState(
    val localeTag: String = DEFAULT_LOCALE,
) {
    companion object {
        fun fromPreferences(preferences: UserPreferences) = SettingsUiState(
            localeTag = preferences.localeTag,
        )
    }
}
