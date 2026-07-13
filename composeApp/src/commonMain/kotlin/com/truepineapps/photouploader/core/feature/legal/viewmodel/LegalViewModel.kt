package com.truepineapps.photouploader.core.feature.legal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mohamedrejeb.calf.core.PlatformContext
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalAcceptanceState
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalContent
import com.truepineapps.photouploader.core.feature.legal.domain.repository.LegalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char

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
                onFailure = {
                    log.d { "checkAcceptance failed: ${it.message ?: "unknown error"}" }
                    _state.value = LegalUiState.Error(
                        it.message
                            ?: "Failed to verify legal documents. Please check your internet connection and try again."
                    )
                }
            )
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
            // Only carry content when the dialog will be shown
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