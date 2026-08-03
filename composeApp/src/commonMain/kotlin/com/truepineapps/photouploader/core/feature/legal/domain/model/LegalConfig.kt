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

package com.truepineapps.photouploader.core.feature.legal.domain.model

/**
 * Configuration model representing the URLs for legal documentation and application versioning.
 * Singleton that is initialized in the Koin module with the right URLs.
 * The expected version format is dd-mm-yyyy.
 * The legal documents are expected to be in Markdown format.
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