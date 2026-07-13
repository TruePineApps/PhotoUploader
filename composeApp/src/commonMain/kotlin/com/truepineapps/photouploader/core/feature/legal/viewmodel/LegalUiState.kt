package com.truepineapps.photouploader.core.feature.legal.viewmodel

import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalContent

sealed interface LegalUiState {
    data object Loading : LegalUiState
    data object Accepted : LegalUiState
    // TODO: replace String with UiText
    data class Error(val message: String) : LegalUiState
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
            get() = (termsScrolled || termsChecked)
                    && (privacyScrolled || privacyChecked)
                    && backupChecked
    }
}