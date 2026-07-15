package com.truepineapps.photouploader.core.feature.legal.viewmodel

import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalContent
import com.truepineapps.photouploader.core.util.UiText

sealed interface LegalUiState {
    data object Loading : LegalUiState
    data object Accepted : LegalUiState
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