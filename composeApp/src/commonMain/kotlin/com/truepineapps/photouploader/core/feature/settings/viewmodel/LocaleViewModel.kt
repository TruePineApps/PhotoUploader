package com.truepineapps.photouploader.core.feature.settings.viewmodel

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.feature.settings.domain.model.DEFAULT_LOCALE
import com.truepineapps.photouploader.core.feature.settings.domain.model.DEFAULT_LOCALE_FROM_PLATFORM
import com.truepineapps.photouploader.core.feature.settings.domain.model.UserPreferences
import com.truepineapps.photouploader.core.feature.settings.domain.repository.UserPreferencesRepository
import com.truepineapps.photouploader.core.localization.PlatformLocaleManager
import com.truepineapps.photouploader.core.presentation.base.LoadingViewModel
import com.truepineapps.photouploader.core.util.UiText
import com.truepineapps.photouploader.core.util.UiTextResource
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
/**
 * ViewModel responsible for managing the application's locale state and preferences.
 * It interacts with the UserPreferencesRepository and platform-specific functions.
 */
class LocaleViewModel(
    userPreferencesRepository: UserPreferencesRepository,
    private val platformLocaleManager: PlatformLocaleManager,
    private val log: Logger,
) : LoadingViewModel(userPreferencesRepository) {

    override fun getDisplayNameText(): UiText = UiTextResource(Res.string.locale)

    /**
     * A StateFlow representing the currently effective application locale state.
     * This includes the real BCP 47 tag for resources and the raw selection to ensure
     * that switching back to "System" always triggers an update even if the resolved
     * language is the same.
     */
    var preferredLocaleState: StateFlow<LocaleState> =
        userPreferencesRepository.preferences
            .onEach { preferences ->
                log.d { "Locale preference changed to '${preferences.localeTag}'. Updating platform..." }
                // Apply the locale to the platform immediately.
                platformLocaleManager.setPlatformLocale(
                    if (preferences.localeTag == DEFAULT_LOCALE_FROM_PLATFORM) null else preferences.localeTag
                )
            }
            .map { preferences ->
                LocaleState(
                    currentTag = getEffectiveLocale(preferences),
                    selectedTag = preferences.localeTag
                )
            }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = LocaleState(DEFAULT_LOCALE, "")
            )

    private fun getEffectiveLocale(preferences: UserPreferences) =
        if (preferences.localeTag == DEFAULT_LOCALE_FROM_PLATFORM) {
            platformLocaleManager.getPlatformLocaleTag() ?: DEFAULT_LOCALE
        } else {
            preferences.localeTag
        }

}

/**
 * Represents the locale state emitted by the ViewModel.
 * When [selectedTag] changes, [currentTag] should follow.
 */
data class LocaleState(
    /** The actual BCP 47 tag used for resource resolution (e.g., "en-US"). */
    val currentTag: String,
    /** The raw selection from the UI (e.g., "System", "en", "nl"). */
    val selectedTag: String
)