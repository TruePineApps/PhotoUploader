package com.truepineapps.photouploader.core.localization

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object LocalizedFormatter : KoinComponent {
    val dateTimeFormatter: com.truepineapps.photouploader.core.localization.DateTimeFormatter by inject()
    val numberFormatter: com.truepineapps.photouploader.core.localization.NumberFormatter by inject()
}


// --- DateTime Extensions ---
fun LocalDateTime.toLocalizedString(): String =
    _root_ide_package_.com.truepineapps.photouploader.core.localization.LocalizedFormatter.dateTimeFormatter.formatDateTime(this)

fun LocalDate.toLocalizedString(): String = _root_ide_package_.com.truepineapps.photouploader.core.localization.LocalizedFormatter.dateTimeFormatter.formatDate(this)
fun LocalTime.toLocalizedString(): String = _root_ide_package_.com.truepineapps.photouploader.core.localization.LocalizedFormatter.dateTimeFormatter.formatTime(this)
fun LocalTime.Companion.is24HourFormat(): Boolean = _root_ide_package_.com.truepineapps.photouploader.core.localization.LocalizedFormatter.dateTimeFormatter.is24HourFormat()

fun LocalDate.Companion.dayMonthFormat(): DateTimeFormat<LocalDate> = Format {
    day()
    char(' ')
    monthName(MonthNames(
        _root_ide_package_.com.truepineapps.photouploader.core.localization.LocalizedFormatter.dateTimeFormatter.localizedMonthNames(
            _root_ide_package_.com.truepineapps.photouploader.core.localization.NameStyle.FULL)))
}

fun LocalDate.Companion.shortMonthYearFormat(): DateTimeFormat<LocalDate> = Format {
    monthName(MonthNames(
        _root_ide_package_.com.truepineapps.photouploader.core.localization.LocalizedFormatter.dateTimeFormatter.localizedMonthNames(
            _root_ide_package_.com.truepineapps.photouploader.core.localization.NameStyle.ABBREVIATED)))
    char(' ')
    year()
}

fun LocalDate.Companion.fullMonthYearFormat(): DateTimeFormat<LocalDate> = Format {
    monthName(MonthNames(
        _root_ide_package_.com.truepineapps.photouploader.core.localization.LocalizedFormatter.dateTimeFormatter.localizedMonthNames(
            _root_ide_package_.com.truepineapps.photouploader.core.localization.NameStyle.FULL)))
    char(' ')
    year()
}


// --- Number Extensions ---
fun Double.toLocalizedDecimalString(
    minIntegerDigits: Int = 1, minFractionDigits: Int = 0, maxFractionDigits: Int = 3
): String = _root_ide_package_.com.truepineapps.photouploader.core.localization.LocalizedFormatter.numberFormatter.formatDecimal(
    this, minIntegerDigits, minFractionDigits, maxFractionDigits
)

fun Double.toLocalizedCurrencyString(
    minIntegerDigits: Int = 1, minFractionDigits: Int = 2, maxFractionDigits: Int = 2
): String = _root_ide_package_.com.truepineapps.photouploader.core.localization.LocalizedFormatter.numberFormatter.formatCurrency(
    amount = this,
    minIntegerDigits = minIntegerDigits,
    minFractionDigits = minFractionDigits,
    maxFractionDigits = maxFractionDigits
)

fun String.toLocaleFormattedDoubleOrNull(): Double? =
    _root_ide_package_.com.truepineapps.photouploader.core.localization.LocalizedFormatter.numberFormatter.parseDecimal(this)