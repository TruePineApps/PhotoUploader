package com.truepineapps.photouploader.core.feature.legal.domain.model

sealed interface LegalAcceptanceState {
    /** First launch — no version has ever been accepted. */
    data object FirstLaunch : LegalAcceptanceState
    /** A newer legal version is available than the one previously accepted. */
    data class UpdateRequired(val latestVersion: String) : LegalAcceptanceState
    /** Current version already accepted; proceed to app. */
    data object UpToDate : LegalAcceptanceState
}