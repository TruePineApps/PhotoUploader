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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mohamedrejeb.calf.core.PlatformContext
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalAcceptanceState
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalContent
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalRemoteException
import com.truepineapps.photouploader.core.feature.legal.domain.repository.LegalRepository
import com.truepineapps.photouploader.core.util.UiText
import com.truepineapps.photouploader.core.util.UiTextResource
import com.truepineapps.photouploader.core.util.UiTextString
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.server_error
import com.truepineapps.photouploader.resources.unknown_error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.StringResource

class LegalViewModel(
    private val legalRepository: LegalRepository,
    private val log: Logger,
) : ViewModel() {

    private val _state = MutableStateFlow<LegalUiState>(LegalUiState.Loading)
    val state: StateFlow<LegalUiState> = _state.asStateFlow()

    fun onIntent(context: PlatformContext, intent: LegalIntent) {
        log.d { "Intent: ${intent::class.simpleName}" }
        when (intent) {
            is LegalIntent.TermsScrolledToBottom -> updateState { it.copy(termsScrolled = true) }
            is LegalIntent.TermsChecked -> updateState { it.copy(termsChecked = intent.checked) }
            is LegalIntent.PrivacyScrolledToBottom -> updateState { it.copy(privacyScrolled = true) }
            is LegalIntent.PrivacyChecked -> updateState { it.copy(privacyChecked = intent.checked) }
            is LegalIntent.BackupChecked -> updateState { it.copy(backupChecked = intent.checked) }
            is LegalIntent.Accept -> acceptAndProceed()
            is LegalIntent.Retry -> checkAcceptance(context)
        }
    }

    fun checkAcceptance(context: PlatformContext) {
        _state.value = LegalUiState.Loading
        viewModelScope.launch {
            checkLegalAcceptance(context).fold(
                onSuccess = { (acceptanceState, content) ->
                    log.d { "checkAcceptance: state = ${acceptanceState::class.simpleName}" }
                    when (acceptanceState) {
                        LegalAcceptanceState.UpToDate -> _state.value = LegalUiState.Accepted
                        else -> _state.value = LegalUiState.ShowLegal(
                            content = content!!,
                            isUpdate = acceptanceState is LegalAcceptanceState.UpdateRequired,
                            termsScrolled = false,
                            termsChecked = false,
                            privacyScrolled = false,
                            privacyChecked = false,
                            backupChecked = false,
                        )
                    }
                },
                onFailure = { throwable ->
                    log.d { "checkAcceptance failed: ${throwable.message ?: "unknown error"}" }

                    val messages = mutableListOf<UiText>()
                    messages.add(
                        mapThrowableToUiText(
                            throwable,
                            if (throwable.suppressedExceptions.isEmpty()) Res.string.server_error else Res.string.unknown_error
                        )
                    )

                    throwable.suppressedExceptions.firstOrNull()?.let { suppressed ->
                        messages.add(mapThrowableToUiText(suppressed, Res.string.server_error))
                    }

                    _state.value = LegalUiState.Error(messages)
                }
            )
        }
    }

    private fun mapThrowableToUiText(throwable: Throwable, defaultMessage: StringResource): UiText =
        when (throwable) {
            is LegalRemoteException -> throwable.uiText
            else -> {
                val message = throwable.message
                if (message.isNullOrBlank()) {
                    UiTextResource(defaultMessage)
                } else {
                    UiTextString(message)
                }
            }
        }

    /**
     * Determines whether the user needs to see the legal flow.
     *
     * Returns a pair of (state, content?).  Content is only fetched when it will
     * actually be shown to the user, avoiding a redundant network round-trip on
     * every cold start where acceptance is already current.
     */
    private suspend fun checkLegalAcceptance(context: PlatformContext): Result<Pair<LegalAcceptanceState, LegalContent?>> =
        legalRepository.fetchLegalContent(context).map { content ->
            val accepted = legalRepository.getAcceptedVersion()
            val state = when {
                accepted == null -> LegalAcceptanceState.FirstLaunch
                isUpdateRequired(
                    accepted,
                    content.latestVersion
                ) -> LegalAcceptanceState.UpdateRequired(content.latestVersion)

                else -> LegalAcceptanceState.UpToDate
            }
            // Only carry content when the dialog is shown
            val contentOrNull = if (state is LegalAcceptanceState.UpToDate) null else content
            state to contentOrNull
        }

    private fun isUpdateRequired(acceptedVersion: String, latestVersion: String): Boolean {
        val formatter = LocalDate.Format {
            day(); char('-'); monthNumber(); char('-'); year()
        }
        return runCatching {
            val accepted = LocalDate.parse(acceptedVersion, formatter)
            val latest = LocalDate.parse(latestVersion, formatter)
            latest > accepted
        }.getOrElse {
            // If either string fails to parse, treat as update required
            // to ensure the user always sees valid documents
            true
        }
    }

    fun loadDocument(context: PlatformContext) {
        _state.value = LegalUiState.Loading
        viewModelScope.launch {
            legalRepository.getLocalLegalContent(context).fold(
                onSuccess = { content ->
                    _state.value = LegalUiState.DocumentLoaded(content)
                },
                onFailure = { throwable ->
                    _state.value = LegalUiState.Error(
                        listOf(
                            mapThrowableToUiText(
                                throwable,
                                Res.string.server_error
                            )
                        )
                    )
                }
            )
        }
    }

    private fun acceptAndProceed() {
        val current = _state.value as? LegalUiState.ShowLegal ?: return
        legalRepository.saveAcceptedVersion(current.content.latestVersion)
        _state.value = LegalUiState.Accepted
    }

    private fun updateState(transform: (LegalUiState.ShowLegal) -> LegalUiState.ShowLegal) {
        _state.update { current ->
            if (current is LegalUiState.ShowLegal) transform(current) else current
        }
    }
}