package com.truepineapps.photouploader.core.feature.settings.viewmodel

import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ViewModel responsible for managing the application's locale state and preferences.
 * It interacts with the UserPreferencesRepository and platform-specific functions.
 */
class LocaleViewModel(
    userPreferencesRepository: UserPreferencesRepository,
) : LoadingViewModel(userPreferencesRepository), KoinComponent {

    private val platformLocaleManager: PlatformLocaleManager by inject()

    override fun getDisplayNameText(): UiText = UiTextResource(Res.string.locale)

    /**
     * A StateFlow representing the currently effective application locale tag.
     * This will be either the user's preferred language (read from settings)
     * or the system locale tag if the preference is set to "System".
     *
     * This is updated when setPreferredAppLocale is called.
     *
     * Changes to this StateFlow trigger recomposition in Composables
     * observing it, causing resources like stringResource and
     * painterResource to be resolved for the new locale.
     */
    var preferredLocaleState: StateFlow<String> =
        userPreferencesRepository.preferences
            .map { preferences -> getEffectiveLocale(preferences) }
            .distinctUntilChanged()
            .onEach { localeTag -> setPreferredAppLocale(localeTag) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = DEFAULT_LOCALE
            )

    private fun getEffectiveLocale(preferences: UserPreferences) =
        if (preferences.localeTag == DEFAULT_LOCALE_FROM_PLATFORM) {
            platformLocaleManager.getPlatformLocaleTag() ?: DEFAULT_LOCALE
        } else {
            preferences.localeTag
        }

    /**
     * Sets the user's preferred application locale.
     * This updates the preference in the repository and triggers the platform-specific
     * function to apply the locale.
     *
     * @param localeTag The BCP 47 language tag to set as preferred.
     *                  Passing null, an empty string, or "System" will result in
     *                  using the system's default locale.
     */
    private fun setPreferredAppLocale(localeTag: String?) {
        viewModelScope.launch {
            // Apply the locale to the platform.
            // If the preference is now "System", then the platform should be told to use its
            // system default, which is represented by "null". Otherwise, apply the specific
            // chosen locale.
            platformLocaleManager.setPlatformLocale(
                if (localeTag.isNullOrBlank() || localeTag == DEFAULT_LOCALE_FROM_PLATFORM) null else localeTag
            )
        }
    }

}