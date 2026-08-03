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

import co.touchlab.kermit.Logger
import io.ktor.util.date.Month
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.format.FormatStyle
import java.util.Locale
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.format.DateTimeFormatter as JavaDateTimeFormatter

class AndroidDateTimeFormatter : DateTimeFormatter, KoinComponent {
    private val log: Logger by inject()
    override fun formatDateTime(dateTime: LocalDateTime): String  =
        JavaDateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
            .format(toJavaDateTime(dateTime.date, dateTime.time))

    override fun formatDate(date: LocalDate): String =
        JavaDateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .format(toJavaDateTime(date, null))

    override fun formatTime(time: LocalTime): String =
        JavaDateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .format(toJavaDateTime(null, time))

    override fun localizedMonthNames(style: NameStyle): List<String> {
        val pattern = if (style == NameStyle.ABBREVIATED) "LL" else "LLLL"
        val formatter = JavaDateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        val monthNames = mutableListOf<String>()
        val javaDateTime = JavaLocalDateTime.of(1970, 1, 1, 0, 0)
        for (monthNumber in 1..12) {
            monthNames.add(formatter.format(javaDateTime.withMonth(monthNumber)))
        }
        return monthNames
    }

    override fun is24HourFormat(): Boolean {
        // Heuristic: Format 11 PM using the default locale's short time style.
        // If it contains "23", assume 24-hour format.
        val referenceTime = JavaLocalDateTime.of(1970, 1, 1, 23, 0)
        val formatter = JavaDateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault()) // Use the JVM's default locale

        val referenceString = formatter.format(referenceTime)
        log.d { "is24HourFormat: $referenceString" }
        return referenceString.contains("23")
    }

    private fun toJavaDateTime(date: LocalDate?, time: LocalTime?): JavaLocalDateTime {
        val year = date?.year ?: 1970
        val monthNumber = date?.month?.ordinal ?: Month.JANUARY.ordinal
        val dayOfMonth = date?.day ?: 1
        val hour = time?.hour ?: 0
        val minute = time?.minute ?: 0
        val second = time?.second ?: 0
        val nanosecond = time?.nanosecond ?: 0
        return JavaLocalDateTime.of(year, monthNumber, dayOfMonth, hour, minute, second, nanosecond)
    }
}