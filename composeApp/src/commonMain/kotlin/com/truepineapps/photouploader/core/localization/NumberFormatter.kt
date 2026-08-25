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

/**
 * Interface for locale-aware number formatting services.
 * Implementations should use the locale provided by [PlatformLocaleProvider].
 */
interface NumberFormatter {
    /**
     * Format a decimal number.
     * @param number The number to format.
     * @param minIntegerDigits The minimum number of integer digits to use.
     * @param minFractionDigits The minimum number of fraction digits to use.
     * @param maxFractionDigits The maximum number of fraction digits to use.
     */
    fun formatDecimal(
        number: Double,
        minIntegerDigits: Int = 1,
        minFractionDigits: Int = 0,
        maxFractionDigits: Int = 3 // Default sensible max for general decimals
    ): String

    /**
     * Format a currency amount.
     * @param amount The amount to format.
     * @param currencyCode The ISO 4217 currency code, e.g. "EUR", "USD", "GBP".
     * @param minIntegerDigits The minimum number of integer digits to use.
     * @param minFractionDigits The minimum number of fraction digits to use.
     * @param maxFractionDigits The maximum number of fraction digits to use.
     */
    fun formatCurrency(
        amount: Double,
        currencyCode: String = "EUR",
        minIntegerDigits: Int = 1,
        minFractionDigits: Int = 2,
        maxFractionDigits: Int = 2
    ): String

    // This default implementation requires the 'this' instance of NumberFormatter.
    fun parseDecimal(formattedString: String): Double? =
        CommonNumberParser.parseDecimalCommon(formattedString, this)

    // Could add formatInt, formatPercentage etc. later
}