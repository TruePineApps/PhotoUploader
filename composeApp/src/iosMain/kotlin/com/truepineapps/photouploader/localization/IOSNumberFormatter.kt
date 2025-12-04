package com.truepineapps.photouploader.localization

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.currentLocale

class IOSNumberFormatter : NumberFormatter, KoinComponent {
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

    override fun formatDecimal(
        number: Double,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val formatter = NSNumberFormatter().apply {
            numberStyle = NSNumberFormatterDecimalStyle
            locale = getLocaleForFormatting()
            minimumIntegerDigits = minIntegerDigits.toULong()
            minimumFractionDigits = minFractionDigits.toULong()
            maximumFractionDigits = maxFractionDigits.toULong()
        }
        return formatter.stringFromNumber(NSNumber(number)) ?: ""
    }

    override fun formatCurrency(
        amount: Double,
        currencyCode: String,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val formatter = NSNumberFormatter().apply {
            numberStyle = NSNumberFormatterCurrencyStyle
            locale = getLocaleForFormatting()
            minimumIntegerDigits = minIntegerDigits.toULong()
            minimumFractionDigits = minFractionDigits.toULong()
            maximumFractionDigits = maxFractionDigits.toULong()
        }
        formatter.currencyCode = currencyCode
        return formatter.stringFromNumber(NSNumber(amount)) ?: ""
    }
}