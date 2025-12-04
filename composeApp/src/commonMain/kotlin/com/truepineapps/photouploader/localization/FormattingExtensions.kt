package com.truepineapps.photouploader.localization

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object LocalizedFormatter : KoinComponent {
    val dateTimeFormatter: DateTimeFormatter by inject()
    val numberFormatter: NumberFormatter by inject()
}


// --- DateTime Extensions ---
fun LocalDateTime.toLocalizedString(): String =
    LocalizedFormatter.dateTimeFormatter.formatDateTime(this)

fun LocalDate.toLocalizedString(): String = LocalizedFormatter.dateTimeFormatter.formatDate(this)
fun LocalTime.toLocalizedString(): String = LocalizedFormatter.dateTimeFormatter.formatTime(this)
fun LocalTime.Companion.is24HourFormat(): Boolean = LocalizedFormatter.dateTimeFormatter.is24HourFormat()

fun LocalDate.Companion.dayMonthFormat(): DateTimeFormat<LocalDate> = Format {
    day()
    char(' ')
    monthName(MonthNames(LocalizedFormatter.dateTimeFormatter.localizedMonthNames(NameStyle.FULL)))
}

fun LocalDate.Companion.shortMonthYearFormat(): DateTimeFormat<LocalDate> = Format {
    monthName(MonthNames(LocalizedFormatter.dateTimeFormatter.localizedMonthNames(NameStyle.ABBREVIATED)))
    char(' ')
    year()
}

fun LocalDate.Companion.fullMonthYearFormat(): DateTimeFormat<LocalDate> = Format {
    monthName(MonthNames(LocalizedFormatter.dateTimeFormatter.localizedMonthNames(NameStyle.FULL)))
    char(' ')
    year()
}


// --- Number Extensions ---
fun Double.toLocalizedDecimalString(
    minIntegerDigits: Int = 1, minFractionDigits: Int = 0, maxFractionDigits: Int = 3
): String = LocalizedFormatter.numberFormatter.formatDecimal(
    this, minIntegerDigits, minFractionDigits, maxFractionDigits
)

fun Double.toLocalizedCurrencyString(
    minIntegerDigits: Int = 1, minFractionDigits: Int = 2, maxFractionDigits: Int = 2
): String = LocalizedFormatter.numberFormatter.formatCurrency(
    amount = this,
    minIntegerDigits = minIntegerDigits,
    minFractionDigits = minFractionDigits,
    maxFractionDigits = maxFractionDigits
)

fun String.toLocaleFormattedDoubleOrNull(): Double? =
    LocalizedFormatter.numberFormatter.parseDecimal(this)