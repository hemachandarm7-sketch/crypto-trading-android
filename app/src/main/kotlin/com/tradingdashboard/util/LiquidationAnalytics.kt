package com.tradingdashboard.util

import com.tradingdashboard.data.model.Trade

/**
 * Analytics specifically for liquidation trades
 */
object LiquidationAnalytics {

    /**
     * Calculate liquidation metrics
     */
    data class LiquidationMetrics(
        val totalLiquidations: Int = 0,
        val totalLiquidationLoss: Double = 0.0,
        val averageLiquidationLoss: Double = 0.0,
        val largestLiquidationLoss: Double = 0.0,
        val liquidationRate: Double = 0.0, // % of closed trades that were liquidated
        val liquidationLossRate: Double = 0.0, // % of total losses that came from liquidations
        val cascadeLiquidations: Int = 0 // Multiple liquidations close together
    )

    /**
     * Calculate all liquidation metrics
     * @param trades - All trades (open and closed)
     * @return LiquidationMetrics containing all calculations
     */
    fun calculateLiquidationMetrics(trades: List<Trade>): LiquidationMetrics {
        val closedTrades = trades.filter { it.status.uppercase() == "CLOSED" }
        val liquidatedTrades = closedTrades.filter { it.isLiquidated }
        val allLosses = closedTrades.filter { it.pnlAmount != null && it.pnlAmount < 0 }

        if (liquidatedTrades.isEmpty()) {
            return LiquidationMetrics()
        }

        // Calculate liquidation losses
        val liquidationLosses = liquidatedTrades
            .filter { it.pnlAmount != null && it.pnlAmount < 0 }
            .map { kotlin.math.abs(it.pnlAmount!!) }

        if (liquidationLosses.isEmpty()) {
            return LiquidationMetrics(
                totalLiquidations = liquidatedTrades.size,
                liquidationRate = if (closedTrades.isNotEmpty())
                    (liquidatedTrades.size * 100.0) / closedTrades.size else 0.0
            )
        }

        val totalLiquidationLoss = liquidationLosses.sum()
        val averageLiquidationLoss = totalLiquidationLoss / liquidationLosses.size
        val largestLiquidationLoss = liquidationLosses.maxOrNull() ?: 0.0

        // Calculate liquidation rate (% of closed trades)
        val liquidationRate = if (closedTrades.isNotEmpty())
            (liquidatedTrades.size * 100.0) / closedTrades.size else 0.0

        // Calculate liquidation loss rate (% of total losses)
        val totalLosses = allLosses.sumOf { kotlin.math.abs(it.pnlAmount!!) }
        val liquidationLossRate = if (totalLosses > 0)
            (totalLiquidationLoss / totalLosses) * 100.0 else 0.0

        // Detect cascade liquidations (multiple liquidations within 5 minutes)
        val cascadeLiquidations = detectCascades(liquidatedTrades)

        return LiquidationMetrics(
            totalLiquidations = liquidatedTrades.size,
            totalLiquidationLoss = totalLiquidationLoss,
            averageLiquidationLoss = averageLiquidationLoss,
            largestLiquidationLoss = largestLiquidationLoss,
            liquidationRate = liquidationRate,
            liquidationLossRate = liquidationLossRate,
            cascadeLiquidations = cascadeLiquidations
        )
    }

    /**
     * Get liquidation summary as string
     */
    fun getLiquidationSummary(metrics: LiquidationMetrics): String {
        return """
            Liquidations: ${metrics.totalLiquidations}
            Total Loss: $${String.format("%.2f", metrics.totalLiquidationLoss)}
            Average Loss: $${String.format("%.2f", metrics.averageLiquidationLoss)}
            Largest Loss: $${String.format("%.2f", metrics.largestLiquidationLoss)}
            Rate: ${String.format("%.1f", metrics.liquidationRate)}%
            Loss Contribution: ${String.format("%.1f", metrics.liquidationLossRate)}%
            Cascades: ${metrics.cascadeLiquidations}
        """.trimIndent()
    }

    /**
     * Detect cascade liquidations (multiple within 5 minutes)
     */
    private fun detectCascades(liquidatedTrades: List<Trade>): Int {
        if (liquidatedTrades.size < 2) return 0

        val sortedByTime = liquidatedTrades
            .filter { it.closeTime != null }
            .sortedBy { it.closeTime }

        if (sortedByTime.isEmpty()) return 0

        var cascadeCount = 0
        val timeWindow = 5 * 60 * 1000 // 5 minutes in milliseconds

        for (i in 1 until sortedByTime.size) {
            val current = sortedByTime[i]
            val previous = sortedByTime[i - 1]

            if (current.closeTime != null && previous.closeTime != null) {
                val timeDiff = current.closeTime - previous.closeTime
                if (timeDiff < timeWindow) {
                    cascadeCount++
                }
            }
        }

        return cascadeCount
    }

    /**
     * Categorize trades by close reason
     */
    fun categorizeTrades(trades: List<Trade>): Map<String, Int> {
        return trades
            .filter { it.status.uppercase() == "CLOSED" }
            .groupingBy { 
                when {
                    it.isLiquidated -> "Liquidated"
                    it.closeReason?.lowercase()?.contains("stop") == true -> "Stop Loss"
                    it.closeReason?.lowercase()?.contains("take") == true -> "Take Profit"
                    else -> "Manual"
                }
            }
            .eachCount()
    }

    /**
     * Get risk assessment based on liquidation history
     */
    fun getRiskAssessment(metrics: LiquidationMetrics): RiskLevel {
        return when {
            metrics.totalLiquidations == 0 -> RiskLevel.LOW
            metrics.liquidationRate < 10.0 -> RiskLevel.LOW
            metrics.liquidationRate < 20.0 -> RiskLevel.MEDIUM
            metrics.liquidationRate < 30.0 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }
    }

    /**
     * Risk assessment levels
     */
    enum class RiskLevel {
        LOW,        // 0% liquidation rate
        MEDIUM,     // 10-20% liquidation rate
        HIGH,       // 20-30% liquidation rate
        CRITICAL    // 30%+ liquidation rate
    }

    /**
     * Recommendations based on liquidation metrics
     */
    fun getRecommendations(metrics: LiquidationMetrics): List<String> {
        val recommendations = mutableListOf<String>()

        if (metrics.totalLiquidations == 0) {
            recommendations.add("✓ No liquidations detected - good risk management")
            return recommendations
        }

        if (metrics.liquidationRate > 50.0) {
            recommendations.add("⚠️ Liquidation rate is very high (${String.format("%.1f", metrics.liquidationRate)}%)")
            recommendations.add("Consider reducing position size or leverage")
        } else if (metrics.liquidationRate > 20.0) {
            recommendations.add("⚠️ High liquidation rate (${String.format("%.1f", metrics.liquidationRate)}%)")
            recommendations.add("Review your stop-loss placement")
        }

        if (metrics.cascadeLiquidations > 0) {
            recommendations.add("⚠️ Cascade liquidations detected (${metrics.cascadeLiquidations})")
            recommendations.add("Multiple liquidations may indicate margin issues")
        }

        if (metrics.liquidationLossRate > 50.0) {
            recommendations.add("⚠️ Liquidations account for ${String.format("%.1f", metrics.liquidationLossRate)}% of losses")
            recommendations.add("Focus on reducing forced closures")
        }

        return recommendations
    }
}
