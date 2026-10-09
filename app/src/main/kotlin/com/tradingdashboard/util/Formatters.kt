package com.tradingdashboard.util

import com.tradingdashboard.presentation.viewmodel.Currency
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Formatting utilities for dates, currency, and numbers
 */
object Formatters {

    // Date Formatters
    private val dateFormatter = SimpleDateFormat(Constants.DateTime.DATE_FORMAT, Locale.getDefault())
    private val timeFormatter = SimpleDateFormat(Constants.DateTime.TIME_FORMAT, Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat(Constants.DateTime.DATETIME_FORMAT, Locale.getDefault())
    private val isoFormatter = SimpleDateFormat(Constants.DateTime.ISO_FORMAT, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Format timestamp to date string (e.g., "Oct 5, 2026")
     */
    fun formatDate(timestamp: Long): String {
        return try {
            dateFormatter.format(Date(timestamp))
        } catch (e: Exception) {
            "—"
        }
    }

    /**
     * Format timestamp to time string (e.g., "14:30:45")
     */
    fun formatTime(timestamp: Long): String {
        return try {
            timeFormatter.format(Date(timestamp))
        } catch (e: Exception) {
            "—"
        }
    }

    /**
     * Format timestamp to datetime string (e.g., "Oct 5, 2026 14:30")
     */
    fun formatDateTime(timestamp: Long): String {
        return try {
            dateTimeFormatter.format(Date(timestamp))
        } catch (e: Exception) {
            "—"
        }
    }

    /**
     * Format timestamp to ISO 8601 format
     */
    fun formatToISO(timestamp: Long): String {
        return try {
            isoFormatter.format(Date(timestamp))
        } catch (e: Exception) {
            "—"
        }
    }

    /**
     * Parse ISO 8601 format to timestamp
     */
    fun parseISO(isoString: String): Long {
        return try {
            isoFormatter.parse(isoString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * Get time ago string (e.g., "2 hours ago")
     */
    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000} min ago"
            diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
            diff < 604_800_000 -> "${diff / 86_400_000} days ago"
            else -> formatDate(timestamp)
        }
    }

    // Currency Formatters
    private val numberFormat = NumberFormat.getInstance(Locale.getDefault()).apply {
        maximumFractionDigits = Constants.UI.DECIMAL_PLACES_AMOUNT
        minimumFractionDigits = 0
    }

    /**
     * Format amount as currency string
     * Examples: "$1,234.56", "€1,234.56"
     */
    fun formatCurrency(
        amount: Double,
        currency: Currency = Currency.USD
    ): String {
        return try {
            val symbol = Constants.CurrencyConfig.CURRENCY_SYMBOLS[currency] ?: "$"
            val formatted = DecimalFormat("#,##0.##").format(amount)
            "$symbol$formatted"
        } catch (e: Exception) {
            "—"
        }
    }

    /**
     * Format amount with currency code
     * Examples: "1,234.56 USD", "1,234.56 EUR"
     */
    fun formatCurrencyWithCode(
        amount: Double,
        currency: Currency = Currency.USD
    ): String {
        return try {
            val formatted = DecimalFormat("#,##0.##").format(amount)
            "$formatted ${currency.name}"
        } catch (e: Exception) {
            "—"
        }
    }

    /**
     * Format P&L with sign and color indicator
     * Returns string like "+$234.56" or "-$123.45"
     */
    fun formatPnL(amount: Double, currency: Currency = Currency.USD): String {
        return try {
            val symbol = Constants.CurrencyConfig.CURRENCY_SYMBOLS[currency] ?: "$"
            val sign = if (amount >= 0) "+" else ""
            val formatted = DecimalFormat("#,##0.##").format(amount)
            "$sign$symbol$formatted"
        } catch (e: Exception) {
            "—"
        }
    }

    /**
     * Format percentage with sign
     * Examples: "+12.5%", "-8.3%"
     */
    fun formatPercentage(
        percentage: Double,
        decimalPlaces: Int = Constants.UI.DECIMAL_PLACES_PERCENTAGE
    ): String {
        return try {
            val format = DecimalFormat("#,##0.${"#".repeat(decimalPlaces)}")
            val sign = if (percentage >= 0) "+" else ""
            "$sign${format.format(percentage)}%"
        } catch (e: Exception) {
            "—"
        }
    }

    /**
     * Format percentage without sign
     * Examples: "12.5%", "8.3%"
     */
    fun formatPercentageAbs(
        percentage: Double,
        decimalPlaces: Int = Constants.UI.DECIMAL_PLACES_PERCENTAGE
    ): String {
        return try {
            val format = DecimalFormat("#,##0.${"#".repeat(decimalPlaces)}")
            "${format.format(Math.abs(percentage))}%"
        } catch (e: Exception) {
            "—"
        }
    }

    // Price Formatters
    /**
     * Format price with specified decimal places
     * Examples: "45,250.5000" (for crypto)
     */
    fun formatPrice(
        price: Double,
        decimalPlaces: Int = Constants.UI.DECIMAL_PLACES_PRICE
    ): String {
        return try {
            val format = DecimalFormat("#,##0.${"0".repeat(decimalPlaces)}")
            format.format(price)
        } catch (e: Exception) {
            "—"
        }
    }

    /**
     * Format price without commas (for displays)
     */
    fun formatPriceSimple(
        price: Double,
        decimalPlaces: Int = Constants.UI.DECIMAL_PLACES_PRICE
    ): String {
        return try {
            val format = DecimalFormat("0.${"0".repeat(decimalPlaces)}")
            format.format(price)
        } catch (e: Exception) {
            "—"
        }
    }

    // Large Number Formatters
    /**
     * Format large numbers with K, M, B suffix
     * Examples: "1.2K", "1.5M", "2.3B"
     */
    fun formatCompactNumber(number: Double): String {
        return when {
            number >= 1_000_000_000 -> "${(number / 1_000_000_000).format(1)}B"
            number >= 1_000_000 -> "${(number / 1_000_000).format(1)}M"
            number >= 1_000 -> "${(number / 1_000).format(1)}K"
            else -> number.format(2)
        }
    }

    // Helper function for decimal formatting
    private fun Double.format(digits: Int): String {
        return try {
            val format = DecimalFormat("#,##0.${"0".repeat(digits)}")
            format.format(this)
        } catch (e: Exception) {
            "—"
        }
    }

    // Trade Statistics Formatters
    /**
     * Format win rate percentage
     * Examples: "40%", "75.5%"
     */
    fun formatWinRate(percentage: Double): String {
        return formatPercentageAbs(percentage)
    }

    /**
     * Format profit factor (risk/reward ratio)
     * Examples: "2.5", "1.0"
     */
    fun formatProfitFactor(factor: Double): String {
        return try {
            DecimalFormat("0.##").format(factor)
        } catch (e: Exception) {
            "—"
        }
    }

    /**
     * Format average trade P&L
     */
    fun formatAverageTrade(amount: Double, currency: Currency = Currency.USD): String {
        return formatCurrency(amount, currency)
    }

    // Duration Formatters
    /**
     * Format duration in milliseconds to human-readable string
     * Examples: "2d 3h 15m"
     */
    fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60)) % 24
        val days = durationMs / (1000 * 60 * 60 * 24)

        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    // Trade Symbol Formatters
    /**
     * Format trade symbol to uppercase with proper spacing
     * Examples: "BTC/USDT", "ETH/USDT"
     */
    fun formatSymbol(symbol: String): String {
        return symbol.uppercase().trim()
    }

    /**
     * Get base asset from symbol
     * Examples: "BTC/USDT" -> "BTC"
     */
    fun getBaseAsset(symbol: String): String {
        return symbol.split("/")[0].uppercase()
    }

    /**
     * Get quote asset from symbol
     * Examples: "BTC/USDT" -> "USDT"
     */
    fun getQuoteAsset(symbol: String): String {
        return symbol.split("/").getOrElse(1) { "USD" }.uppercase()
    }
}
