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

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

enum class NameStyle {
    FULL,
    ABBREVIATED
}

/**
 * Interface for locale-aware date and time formatting services.
 * Implementations should use the locale provided by [PlatformLocaleProvider].
 */
interface DateTimeFormatter {
    fun formatDateTime(dateTime: LocalDateTime): String
    fun formatDate(date: LocalDate): String
    fun formatTime(time: LocalTime): String

    /**
     * Retrieves the full or abbreviated names of months in the current locale
     */
    fun localizedMonthNames(style: NameStyle): List<String>

    /**
     * Checks if the current system/locale preference is for a 24-hour time format.
     *
     * @return `true` if the system is configured for 24-hour format (e.g., 13:00),
     *         `false` if for 12-hour format (e.g., 1:00 PM).
     */
    fun is24HourFormat(): Boolean

    // Optional: Allow one-time locale override if needed,
    // otherwise, it always uses the LocaleProvider's locale.
    // fun formatDateTime(dateTime: LocalDateTime, localeTag: String?): String
}