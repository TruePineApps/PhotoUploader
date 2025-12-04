package com.truepineapps.photouploader.localization

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