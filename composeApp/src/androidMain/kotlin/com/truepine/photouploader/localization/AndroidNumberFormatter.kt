package com.truepine.photouploader.localization

import org.koin.core.component.KoinComponent
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class AndroidNumberFormatter : NumberFormatter, KoinComponent {
    override fun formatDecimal(
        number: Double,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val decimalFormat = DecimalFormat.getInstance(Locale.getDefault()) as DecimalFormat
        decimalFormat.minimumIntegerDigits = minIntegerDigits
        decimalFormat.minimumFractionDigits = minFractionDigits
        decimalFormat.maximumFractionDigits = maxFractionDigits
        return decimalFormat.format(number)
    }

    override fun formatCurrency(
        amount: Double,
        currencyCode: String,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        currencyFormat.currency = Currency.getInstance(currencyCode)
        currencyFormat.minimumIntegerDigits = minIntegerDigits
        currencyFormat.minimumFractionDigits = minFractionDigits
        currencyFormat.maximumFractionDigits = maxFractionDigits
        return currencyFormat.format(amount)
    }

}