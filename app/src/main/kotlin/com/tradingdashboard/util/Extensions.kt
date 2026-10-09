package com.tradingdashboard.util

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.presentation.theme.GreenWin
import com.tradingdashboard.presentation.theme.RedLoss
import com.tradingdashboard.presentation.theme.TextMuted
import kotlin.math.absoluteValue

// ============ String Extensions ============

/**
 * Check if string is a valid email
 */
fun String.isValidEmail(): Boolean {
    return this.matches(Regex(Constants.Validation.REGEX_EMAIL))
}

/**
 * Check if string is a valid trading symbol
 */
fun String.isValidSymbol(): Boolean {
    return this.isNotEmpty() &&
            this.length in Constants.Validation.MIN_SYMBOL_LENGTH..Constants.Validation.MAX_SYMBOL_LENGTH &&
            this.matches(Regex(Constants.Validation.REGEX_SYMBOL))
}

/**
 * Capitalize first letter
 */
fun String.capitalizeFirst(): String {
    return if (this.isNotEmpty()) this[0].uppercase() + this.substring(1) else this
}

/**
 * Safe string to double conversion
 */
fun String.toDoubleOrDefault(default: Double = 0.0): Double {
    return this.toDoubleOrNull() ?: default
}

/**
 * Safe string to long conversion
 */
fun String.toLongOrDefault(default: Long = 0L): Long {
    return this.toLongOrNull() ?: default
}

// ============ Double Extensions ============

/**
 * Check if number is positive
 */
fun Double.isPositive(): Boolean = this > 0.0

/**
 * Check if number is negative
 */
fun Double.isNegative(): Boolean = this < 0.0

/**
 * Check if number is zero
 */
fun Double.isZero(): Boolean = this == 0.0

/**
 * Get color based on value (green for positive, red for negative)
 */
fun Double.getValueColor(): Color = if (this >= 0.0) GreenWin else RedLoss

/**
 * Get sign as string ("+", "-", or empty)
 */
fun Double.getSign(): String = when {
    this > 0.0 -> "+"
    this < 0.0 -> "-"
    else -> ""
}

/**
 * Round to specific decimal places
 */
fun Double.roundTo(decimalPlaces: Int): Double {
    val multiplier = Math.pow(10.0, decimalPlaces.toDouble())
    return Math.round(this * multiplier) / multiplier
}

/**
 * Safe division (returns 0 if divisor is 0)
 */
fun Double.safeDivide(divisor: Double): Double {
    return if (divisor == 0.0) 0.0 else this / divisor
}

// ============ List Extensions ============

/**
 * Calculate average
 */
fun <T> List<T>.averageOf(selector: (T) -> Double): Double {
    return if (this.isEmpty()) 0.0 else this.sumOf(selector) / this.size
}

/**
 * Calculate sum
 */
fun <T> List<T>.sumOf(selector: (T) -> Double): Double {
    return this.fold(0.0) { acc, item -> acc + selector(item) }
}

/**
 * Find by predicate or return default
 */
fun <T> List<T>.findOrDefault(predicate: (T) -> Boolean, default: T?): T? {
    return this.find(predicate) ?: default
}

/**
 * Group and count
 */
fun <T> List<T>.groupAndCount(): Map<T, Int> {
    return this.groupingBy { it }.eachCount()
}

// ============ Trade Extensions ============

/**
 * Check if trade is profitable
 */
fun Trade.isWinner(): Boolean {
    return (pnlAmount ?: 0.0) > 0.0
}

/**
 * Check if trade is a loss
 */
fun Trade.isLoser(): Boolean {
    return (pnlAmount ?: 0.0) < 0.0
}

/**
 * Get trade duration in milliseconds
 */
fun Trade.getDurationMs(): Long {
    val closeTime = closeTime ?: System.currentTimeMillis()
    return closeTime - openTime
}

/**
 * Get trade status color
 */
fun Trade.getStatusColor(): Color {
    return if (status == Constants.Trade.STATUS_OPEN) {
        GreenWin
    } else {
        TextMuted
    }
}

/**
 * Get P&L color
 */
fun Trade.getPnLColor(): Color {
    return (pnlAmount ?: 0.0).getValueColor()
}

/**
 * Calculate ROE (Return on Equity)
 */
fun Trade.calculateROE(): Double {
    if (margin == 0.0) return 0.0
    return ((pnlAmount ?: 0.0) / margin) * 100.0
}

/**
 * Check if trade is open
 */
fun Trade.isOpen(): Boolean = status == Constants.Trade.STATUS_OPEN

/**
 * Check if trade is closed
 */
fun Trade.isClosed(): Boolean = status == Constants.Trade.STATUS_CLOSED

/**
 * Get trade direction badge text
 */
fun Trade.getDirectionBadge(): String {
    return if (position == Constants.Trade.POSITION_LONG) "LONG" else "SHORT"
}

/**
 * Calculate risk/reward ratio
 */
fun Trade.calculateRiskReward(entryPrice: Double = this.entryPrice): Double {
    if (entryPrice == 0.0) return 0.0
    
    val takeProfit = exitPrice ?: entryPrice
    val reward = (takeProfit - entryPrice).absoluteValue
    
    return if (position == Constants.Trade.POSITION_LONG) {
        reward / entryPrice * 100
    } else {
        reward / entryPrice * 100
    }
}

// ============ Context Extensions ============

/**
 * Get DP value in pixels
 */
fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

/**
 * Get pixels value in DP
 */
fun Context.pxToDp(px: Int): Int {
    return (px / resources.displayMetrics.density).toInt()
}

// ============ Collection Extensions ============

/**
 * Safe get with default
 */
fun <T> List<T>.getOrDefault(index: Int, default: T): T {
    return if (index in this.indices) this[index] else default
}

/**
 * Check if list is not empty with condition
 */
fun <T> List<T>.hasAny(predicate: (T) -> Boolean): Boolean {
    return this.any(predicate)
}

/**
 * Get first or null
 */
fun <T> List<T>.firstOrNull(): T? {
    return if (this.isNotEmpty()) this[0] else null
}

/**
 * Get last or null
 */
fun <T> List<T>.lastOrNull(): T? {
    return if (this.isNotEmpty()) this[this.size - 1] else null
}

// ============ Boolean Extensions ============

/**
 * Convert boolean to readable string
 */
fun Boolean.toReadableString(): String {
    return if (this) "Enabled" else "Disabled"
}

/**
 * Toggle boolean
 */
fun Boolean.toggle(): Boolean = !this

// ============ Pair Extensions ============

/**
 * Get first or default
 */
fun <T, U> Pair<T, U>?.firstOrDefault(default: T): T {
    return this?.first ?: default
}

/**
 * Get second or default
 */
fun <T, U> Pair<T, U>?.secondOrDefault(default: U): U {
    return this?.second ?: default
}

// ============ Time Extensions ============

/**
 * Add hours to timestamp
 */
fun Long.addHours(hours: Int): Long {
    return this + (hours * 60 * 60 * 1000)
}

/**
 * Add days to timestamp
 */
fun Long.addDays(days: Int): Long {
    return this + (days * 24 * 60 * 60 * 1000)
}

/**
 * Check if timestamp is today
 */
fun Long.isToday(): Boolean {
    val calendar = java.util.Calendar.getInstance()
    val today = calendar.timeInMillis
    
    calendar.timeInMillis = this
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    
    calendar.timeInMillis = today
    return year == calendar.get(java.util.Calendar.YEAR) &&
            month == calendar.get(java.util.Calendar.MONTH) &&
            day == calendar.get(java.util.Calendar.DAY_OF_MONTH)
}

/**
 * Check if timestamp is in current week
 */
fun Long.isThisWeek(): Boolean {
    val calendar = java.util.Calendar.getInstance()
    val today = calendar.timeInMillis
    
    calendar.timeInMillis = this
    val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
    
    calendar.timeInMillis = today
    return week == calendar.get(java.util.Calendar.WEEK_OF_YEAR)
}

/**
 * Check if timestamp is in current month
 */
fun Long.isThisMonth(): Boolean {
    val calendar = java.util.Calendar.getInstance()
    val today = calendar.timeInMillis
    
    calendar.timeInMillis = this
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH)
    
    calendar.timeInMillis = today
    return year == calendar.get(java.util.Calendar.YEAR) &&
            month == calendar.get(java.util.Calendar.MONTH)
}
