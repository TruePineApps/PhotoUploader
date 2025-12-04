package com.truepineapps.photouploader.data.preferences

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.truepineapps.photouploader.data.DataLoadingState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update

class UserPreferencesSettingsRepository(
    private val settings: Settings,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : UserPreferencesRepository {
    //region Keys
    companion object {
        private const val KEY_LOCALE_TAG = "locale_tag"
    }
    //endregion Keys

    //region Flow
    private val _preferences = MutableStateFlow(UserPreferences.DEFAULTS)
    override val preferences = _preferences as StateFlow<UserPreferences>

    override val loadingState: Flow<DataLoadingState> = flow {
        try {
            println("Settings loadingState: Emit loading...")
            emit(DataLoadingState.Loading)
            println("Settings loadingState: Loading initial preferences...")
            loadInitialPreferences()
            println("Settings loadingState: Emit success...")
            emit(DataLoadingState.Success)
            println("Settings loadingState: Done emitting")
        } catch (e: Exception) {
            println("Settings loadingState: Emit error")
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
            println("Error saving locale preference")
        }
    }

    //endregion Save

    //region Update
    private fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        _preferences.update(transform)
    }
    //endregion Update
}