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

/**
 * Represents the user's intended actions or events occurring on the Legal consent screen.
 * These intents are processed by the ViewModel to update the UI state and handle business logic.
 */
sealed interface LegalIntent {
    data object TermsScrolledToBottom : LegalIntent
    data class TermsChecked(val checked: Boolean) : LegalIntent
    data object PrivacyScrolledToBottom : LegalIntent
    data class PrivacyChecked(val checked: Boolean) : LegalIntent
    data class BackupChecked(val checked: Boolean) : LegalIntent
    data object Accept : LegalIntent
    data object Retry : LegalIntent
}