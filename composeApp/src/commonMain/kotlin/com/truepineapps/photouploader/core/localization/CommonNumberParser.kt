package com.truepineapps.photouploader.core.localization

// This would be part of your commonMain logic, possibly within a helper class
// or even as a default method on an interface if you structure it that way.

object CommonNumberParser {

    // The current implementation assumes that the locale has '.' or ',' as decimal separator.
    fun parseDecimalCommon(formattedString: String, formatter: NumberFormatter): Double? {
        val decimalSeparator = deduceDecimalSeparator(formatter)

        var normalizedString = formattedString.trim()
        // Replace non-breaking spaces, common in some locales for grouping
        normalizedString = normalizedString.replace('\u00A0', ' ')
        // Remove all spaces
        normalizedString = normalizedString.replace(" ", "")
        // Remove grouping separator
        normalizedString = normalizedString.replace(
            if (decimalSeparator == '.') "," else ".",
            ""
        )

        // Convert the identified locale-specific decimal separator to a standard dot for String.toDouble
        if (decimalSeparator != '.') {
            normalizedString = normalizedString.replace(decimalSeparator, '.')
        }
        return normalizedString.toDoubleOrNull()
    }

    private fun deduceDecimalSeparator(formatter: NumberFormatter): Char {
        val sampleDecimal = 1.5
        val formattedSampleDecimal = formatter.formatDecimal(
            sampleDecimal,
            minFractionDigits = 1,
            maxFractionDigits = 1
        )
        return if (formattedSampleDecimal.contains(',')) ',' else '.'
    }

}