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
 * Manages the application-wide preferred locale.
 */
interface PlatformLocaleManager :
    PlatformLocaleProvider { // Extends LocaleProvider for convenience
    /**
     * Platform-specific function to set the chosen locale tag to the application's environment.
     * Passing null will result in using the system's default locale.
     * Passing an invalid locale tag will not change the current locale of the system.
     * On Android, this would call AppCompatDelegate.setApplicationLocales().
     * On Web, this might involve a page reload or other framework-specific actions.
     * On other platforms, it might set the default locale or do nothing if not applicable.
     * The actual implementation is injected in Koin
     *
     * @param localeTag The BCP 47 language tag  (e.g., "en-US", "fr-FR") to apply. If null, it
     *                  implies that the application should revert to using the system's default
     *                  locale. An invalid tag will be ignored.
     */
    fun setPlatformLocale(localeTag: String?)
}