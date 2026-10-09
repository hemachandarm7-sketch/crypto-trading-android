package com.tradingdashboard.util

/**
 * Detects and handles liquidation trades from OCR text and data
 */
object LiquidationDetector {
    
    companion object {
        private const val TAG = "LiquidationDetector"
        
        // Keywords that indicate liquidation
        private val LIQUIDATION_KEYWORDS = listOf(
            "liquidated",
            "liquidation",
            "liq",
            "force closed",
            "force close",
            "forced close",
            "margin call",
            "stop loss",
            "sl hit",
            "stopped out",
            "cascade",
            "cascaded",
            "account liquidated",
            "position liquidated"
        )
        
        // Status indicators
        private val LIQUIDATION_STATUS = listOf(
            "LIQ",
            "LIQUIDATED",
            "CLOSED_LIQUIDATION",
            "FORCED_CLOSE"
        )
    }

    /**
     * Detect if a trade was liquidated based on OCR text
     * @param ocrText - Extracted text from screenshot
     * @return Pair of (isLiquidated, closeReason)
     */
    fun detectLiquidationFromText(ocrText: String): Pair<Boolean, String?> {
        if (ocrText.isBlank()) {
            return Pair(false, null)
        }

        val lowerText = ocrText.lowercase()

        // Check for liquidation keywords
        val foundKeyword = LIQUIDATION_KEYWORDS.find { keyword ->
            lowerText.contains(keyword)
        }

        if (foundKeyword != null) {
            Logger.d(TAG, "Detected liquidation keyword: $foundKeyword")
            return Pair(true, "liquidated")
        }

        return Pair(false, null)
    }

    /**
     * Detect liquidation from trade status text
     * @param statusText - Trade status or close reason text
     * @return Pair of (isLiquidated, closeReason)
     */
    fun detectLiquidationFromStatus(statusText: String?): Pair<Boolean, String?> {
        if (statusText == null || statusText.isBlank()) {
            return Pair(false, null)
        }

        val upperText = statusText.uppercase()

        // Check for liquidation status
        if (LIQUIDATION_STATUS.any { status ->
                upperText.contains(status)
            }) {
            Logger.d(TAG, "Detected liquidation status: $statusText")
            return Pair(true, "liquidated")
        }

        return Pair(false, null)
    }

    /**
     * Detect if P&L suggests liquidation
     * A liquidation typically results in significant loss
     * @param pnlAmount - P&L amount in dollars
     * @param pnlPercentage - P&L percentage
     * @param margin - Margin used
     * @return Pair of (isLiquidated, closeReason) - only returns liquidation if confidence is high
     */
    fun detectLiquidationFromPnL(
        pnlAmount: Double?,
        pnlPercentage: Double?,
        margin: Double?
    ): Pair<Boolean, String?> {
        // Can't determine from P&L alone - too many false positives
        // Always require explicit text confirmation
        return Pair(false, null)
    }

    /**
     * Comprehensive liquidation detection combining multiple signals
     * @param ocrText - Extracted text from screenshot
     * @param statusText - Trade status field
     * @param pnlAmount - P&L amount
     * @param pnlPercentage - P&L percentage
     * @param margin - Margin used
     * @return Pair of (isLiquidated, closeReason)
     */
    fun detectLiquidation(
        ocrText: String? = null,
        statusText: String? = null,
        pnlAmount: Double? = null,
        pnlPercentage: Double? = null,
        margin: Double? = null
    ): Pair<Boolean, String?> {
        // Check status first (most reliable)
        val statusResult = detectLiquidationFromStatus(statusText)
        if (statusResult.first) {
            return statusResult
        }

        // Check OCR text
        if (!ocrText.isNullOrBlank()) {
            val ocrResult = detectLiquidationFromText(ocrText)
            if (ocrResult.first) {
                return ocrResult
            }
        }

        // No liquidation detected
        return Pair(false, null)
    }

    /**
     * Get close reason text based on liquidation status
     * @param isLiquidated - Whether trade was liquidated
     * @param manualCloseReason - Manual close reason if provided
     * @return Close reason text
     */
    fun getCloseReason(
        isLiquidated: Boolean,
        manualCloseReason: String? = null
    ): String {
        return when {
            isLiquidated -> "liquidated"
            !manualCloseReason.isNullOrBlank() -> manualCloseReason
            else -> "manual_exit"
        }
    }

    /**
     * Validate liquidation trade data
     * Ensures a liquidation trade has all required fields
     * @param symbol - Trade symbol
     * @param pnlAmount - P&L amount (should be negative for liquidation)
     * @param status - Trade status (should be CLOSED)
     * @param isLiquidated - Liquidation flag
     * @return Pair of (isValid, errorMessage)
     */
    fun validateLiquidationTrade(
        symbol: String,
        pnlAmount: Double?,
        status: String?,
        isLiquidated: Boolean
    ): Pair<Boolean, String?> {
        if (!isLiquidated) {
            return Pair(true, null)
        }

        // Liquidation validation
        if (symbol.isBlank()) {
            return Pair(false, "Liquidation: Symbol is required")
        }

        if (status?.uppercase() != "CLOSED") {
            return Pair(false, "Liquidation: Status must be CLOSED")
        }

        if (pnlAmount == null) {
            return Pair(false, "Liquidation: P&L amount is required")
        }

        // Liquidations typically result in losses
        // But allow small gains in edge cases (partial liquidation recovery)
        if (pnlAmount > 100.0) {
            Logger.w(TAG, "Unusual liquidation with large P&L gain: $pnlAmount")
        }

        return Pair(true, null)
    }

    /**
     * Check if multiple liquidations occurred in short timeframe
     * Useful for detecting cascade liquidations
     * @param trades - List of recent trades
     * @param timeWindowSeconds - Time window to check (default 300 = 5 minutes)
     * @return Number of liquidations in time window
     */
    fun detectCascadeLiquidations(
        trades: List<Pair<Long, Boolean>>, // Pair of (timestamp, isLiquidated)
        timeWindowSeconds: Long = 300
    ): Int {
        if (trades.isEmpty()) return 0

        val mostRecent = trades.maxByOrNull { it.first }?.first ?: return 0
        val cutoffTime = mostRecent - (timeWindowSeconds * 1000)

        return trades.count { (timestamp, isLiquidated) ->
            isLiquidated && timestamp >= cutoffTime
        }
    }
}
