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

package com.truepineapps.photouploader.core.localization

/**
 * Provides the currently active locale tag for the application.
 * Other services can depend on this to get the locale for their operations.
 */
interface PlatformLocaleProvider {
    /**
     * Platform-specific function to get the current system's locale tag (e.g., "en-US", "fr-CA").
     * This function will be implemented by each platform (actual implementation injected in Koin).
     *
     * @return The IETF BCP 47 language tag (e.g., "en-US") of the currently active locale.
     */
    fun getPlatformLocaleTag(): String?
}