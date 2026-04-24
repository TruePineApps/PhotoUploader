package com.truepineapps.photouploader.core.feature.settings.domain.repository

import com.truepineapps.photouploader.core.feature.settings.domain.model.UserPreferences
import com.truepineapps.photouploader.core.domain.repository.DataLoadingRepository
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository: DataLoadingRepository {
    /** Return a flow that keeps the actual values of settings in the UserPreference object */
    val preferences: Flow<UserPreferences>

    suspend fun saveLocalePreference(localeTag: String)
 }