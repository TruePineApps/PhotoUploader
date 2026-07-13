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