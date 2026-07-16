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