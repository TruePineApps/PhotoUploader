package com.truepineapps.photouploader.core.feature.legal.domain.model

/**
 * Configuration model representing the URLs for legal documentation and application versioning.
 * Singleton that is initialized in the Koin module with the right URLs.
 * The expected version format is dd-mm-yyyy.
 * The legal documents are expected to be in Morkdown format.
 *
 * @property versionUrl The URL pointing to the file with the version of the latest update.
 * @property termsUrl The URL pointing to the Terms of Service document.
 * @property privacyPolicyUrl The URL pointing to the Privacy Policy document.
 */
data class LegalConfig(
    val versionUrl: String,
    val termsUrl: String,
    val privacyPolicyUrl: String,
)