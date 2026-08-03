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

package com.truepineapps.photouploader.core.feature.legal.domain.repository

import com.mohamedrejeb.calf.core.PlatformContext
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalContent

interface LegalRepository {
    /** Fetches the latest version string and full legal texts from the remote. */
    suspend fun fetchLegalContent(context: PlatformContext): Result<LegalContent>

    /** Reads the currently saved legal content from local storage. */
    suspend fun getLocalLegalContent(context: PlatformContext): Result<LegalContent>

    /** Returns the version string last accepted by the user, or null if never accepted. */
    fun getAcceptedVersion(): String?

    /** Persists the accepted version string. */
    fun saveAcceptedVersion(version: String)
}