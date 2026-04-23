package com.truepineapps.photouploader.core.localization

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import java.time.format.FormatStyle
import java.util.Locale
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.format.DateTimeFormatter as JavaDateTimeFormatter


class DesktopDateTimeFormatter : DateTimeFormatter {
    override fun formatDateTime(dateTime: LocalDateTime): String  =
        JavaDateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
            .format(toJavaDateTime(dateTime.date, dateTime.time))

    override fun formatDate(date: LocalDate): String  =
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
        return referenceString.contains("23")
    }

    private fun toJavaDateTime(date: LocalDate?, time: LocalTime?): JavaLocalDateTime {
        val year = date?.year ?: 1970
        val monthNumber = date?.month?.number ?: 1
        val dayOfMonth = date?.day ?: 1
        val hour = time?.hour ?: 0
        val minute = time?.minute ?: 0
        val second = time?.second ?: 0
        val nanosecond = time?.nanosecond ?: 0
        return JavaLocalDateTime.of(year, monthNumber, dayOfMonth, hour, minute, second, nanosecond)
    }

}