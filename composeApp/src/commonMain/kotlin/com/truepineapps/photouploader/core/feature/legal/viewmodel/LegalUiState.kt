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

package com.truepineapps.photouploader.core.feature.legal.viewmodel

import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalContent
import com.truepineapps.photouploader.core.util.UiText

sealed interface LegalUiState {
    data object Loading : LegalUiState
    data object Accepted : LegalUiState
    data class DocumentLoaded(val content: LegalContent) : LegalUiState
    data class Error(val messages: List<UiText>) : LegalUiState
    data class ShowLegal(
        val content: LegalContent,
        val isUpdate: Boolean,
        val termsScrolled: Boolean,
        val termsChecked: Boolean,
        val privacyScrolled: Boolean,
        val privacyChecked: Boolean,
        val backupChecked: Boolean,
    ) : LegalUiState {
        val canAccept: Boolean
            get() = termsScrolled && termsChecked
                    && privacyScrolled && privacyChecked
                    && backupChecked
    }
}
