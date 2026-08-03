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

import com.truepineapps.photouploader.core.util.format
import com.truepineapps.photouploader.core.util.formatDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterLongStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.systemTimeZone

class IOSDateTimeFormatter : DateTimeFormatter, KoinComponent {
    private val platformLocaleProvider: PlatformLocaleProvider by inject()

    /**
     * Returns the locale to use for formatting. Returns JavaScript 'undefined' if not locale is
     * set, since that implies the system default in the formatting functions.
     */
    private fun getLocaleForFormatting(): NSLocale {
        val locale = platformLocaleProvider.getPlatformLocaleTag()
        return if (locale == null) {
            NSLocale.currentLocale
        } else {
            NSLocale(locale)
        }
    }

    override fun formatDateTime(dateTime: LocalDateTime): String =
        getFormattedStringForDate(
            dateTime,
            NSDateFormatterLongStyle,
            NSDateFormatterShortStyle
        ) ?: dateTime.formatDateTime()

    override fun formatDate(date: LocalDate): String = getFormattedStringForDate(
            LocalDateTime(date.year, date.month.number, date.day, 0, 0),
            NSDateFormatterLongStyle,
            NSDateFormatterNoStyle
        ) ?: date.format()

    override fun formatTime(time: LocalTime): String =
        getFormattedStringForDate(
            LocalDateTime(1970, 1, 1, time.hour, time.minute),
            NSDateFormatterNoStyle,
            NSDateFormatterShortStyle
        ) ?: time.format()

    override fun localizedMonthNames(style: NameStyle): List<String> {
        val pattern = if (style == NameStyle.ABBREVIATED) "MM" else "MMMM"
        val formatter = NSDateFormatter().apply {
            locale = getLocaleForFormatting()
            timeZone = NSTimeZone.systemTimeZone()
        }
        formatter.setLocalizedDateFormatFromTemplate(pattern)

        val calendar = NSCalendar.currentCalendar
        val components = NSDateComponents().apply {
            year = 1970
            day = 1
            hour = 0
            minute = 0
            second = 0
        }
        val monthNames = mutableListOf<String>()
        for (monthNumber in 1..12) {
            components.month = monthNumber.toLong()
            val date = (calendar.dateFromComponents(components)
                ?: throw IllegalStateException("localizedMonthNames: Failed NSDate conversion for month $monthNumber"))
            monthNames.add(
                formatter.stringFromDate(date)
            )
        }
        return monthNames
    }

    override fun is24HourFormat(): Boolean {
        // Check if the locale's date format string contains 'a' (for AM/PM marker).
        val dateFormatString =
            NSDateFormatter.dateFormatFromTemplate("j", 0u, getLocaleForFormatting())
        // "j" is the preferred hour format skeleton (h, H, K, k).
        // If it contains 'a' (AM/PM marker), it's 12-hour.
        // If it's 'H' or 'k', it's 24-hour. 'h' or 'K' is 12-hour.

        // This is considered a more robust way on iOS:
        return dateFormatString?.contains('a', ignoreCase = true) == false
    }

    private fun getFormattedStringForDate(
        localDateTime: LocalDateTime,
        dateStyle: ULong,
        timeStyle: ULong
    ): String? {
        val nsDate = localDateTime.toNsDate()
        return if (nsDate == null) {
            null
        } else {
            val formatter = NSDateFormatter().apply {
                this.dateStyle = dateStyle
                this.timeStyle = timeStyle
                this.locale = getLocaleForFormatting()
                this.timeZone = NSTimeZone.systemTimeZone()
            }
            formatter.stringFromDate(nsDate)
        }
    }

    private fun LocalDateTime.toNsDate(): NSDate? {
        val calendar = NSCalendar.currentCalendar
        val components = NSDateComponents()
        components.year = this.year.toLong()
        components.month = month.number.toLong()
        components.day = day.toLong()
        components.hour = this.hour.toLong()
        components.minute = this.minute.toLong()
        components.second = this.second.toLong()
        return calendar.dateFromComponents(components)
    }
}