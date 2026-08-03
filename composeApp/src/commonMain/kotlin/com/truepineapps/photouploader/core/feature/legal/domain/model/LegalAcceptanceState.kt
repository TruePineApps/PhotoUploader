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

package com.truepineapps.photouploader.core.feature.legal.domain.model

sealed interface LegalAcceptanceState {
    /** First launch — no version has ever been accepted. */
    data object FirstLaunch : LegalAcceptanceState
    /** A newer legal version is available than the one previously accepted. */
    data class UpdateRequired(val latestVersion: String) : LegalAcceptanceState
    /** Current version already accepted; proceed to app. */
    data object UpToDate : LegalAcceptanceState
}