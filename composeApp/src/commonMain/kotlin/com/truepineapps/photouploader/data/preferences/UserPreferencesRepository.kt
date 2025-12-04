/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.data.preferences

import com.truepineapps.photouploader.data.DataLoadingRepository
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository: DataLoadingRepository {
    /** Return a flow that keeps the actual values of settings in the UserPreference object */
    val preferences: Flow<UserPreferences>

    suspend fun saveLocalePreference(localeTag: String)
 }