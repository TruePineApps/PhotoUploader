package com.truepineapps.photouploader.core.feature.settings.data.repository

import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.truepineapps.photouploader.core.domain.state.DataLoadingState
import com.truepineapps.photouploader.core.feature.settings.domain.model.UserPreferences
import com.truepineapps.photouploader.core.feature.settings.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UserPreferencesSettingsRepository(
    private val settings: Settings,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : UserPreferencesRepository, KoinComponent {
    //region Keys
    companion object {
        private const val KEY_LOCALE_TAG = "locale_tag"
    }
    //endregion Keys
    private val log: Logger by inject()

    //region Flow
    private val _preferences = MutableStateFlow(UserPreferences.DEFAULTS)
    override val preferences = _preferences.asStateFlow()

    override val loadingState: Flow<DataLoadingState> = flow {
        try {
            log.d { "Settings loadingState: Emit loading..." }
            emit(DataLoadingState.Loading)
            log.d { "Settings loadingState: Loading initial preferences..." }
            loadInitialPreferences()
            log.d { "Settings loadingState: Emit success..." }
            emit(DataLoadingState.Success)
            log.d { "Settings loadingState: Done emitting" }
        } catch (e: Exception) {
            log.e(e) { "Settings loadingState: Emit error" }
            emit(DataLoadingState.Error(e))
        }
    }.flowOn(defaultDispatcher)

    override fun prepareReload() {
        // Nothing to do, collecting the flow again will return the latest values
    }

    //endregion Flow

    //region Load
    private fun loadInitialPreferences() {
        val loadedPreferences = UserPreferences(
            localeTag = settings.getString(
                KEY_LOCALE_TAG, UserPreferences.DEFAULTS.localeTag
            ),
        )
        _preferences.value = loadedPreferences
    }

    //endregion Load

    //region Save

    override suspend fun saveLocalePreference(localeTag: String) {
        try {
            settings[KEY_LOCALE_TAG] = localeTag
            updatePreferences { it.copy(localeTag = localeTag) }
        } catch (e: Exception) {
            log.e(e) { "Error saving locale preference" }
        }
    }

    //endregion Save

    //region Update
    private fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        _preferences.update(transform)
    }
    //endregion Update
}