package com.tradingdashboard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val symbol: String,
    val position: String, // LONG/SHORT
    val leverage: Double?,
    val entryPrice: Double?,
    val exitPrice: Double?,
    val quantity: Double?,
    val margin: Double?,
    val notional: Double?,
    val pnlAmount: Double?,
    val pnlPercentage: Double?,
    val status: String, // OPEN/CLOSED
    val closeReason: String? = null, // "liquidated", "manual_exit", etc.
    val isLiquidated: Boolean = false, // True if trade was liquidated
    val openTime: Long?,
    val closeTime: Long?,
    val holdingSeconds: Long?,
    val exchange: String? = null,
    val marketType: String? = null,
    val notes: String? = null,
    val syncedWithServer: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)

// Domain model
data class Trade(
    val id: String,
    val userId: String,
    val symbol: String,
    val position: String,
    val leverage: Double?,
    val entryPrice: Double?,
    val exitPrice: Double?,
    val quantity: Double?,
    val margin: Double?,
    val notional: Double?,
    val pnlAmount: Double?,
    val pnlPercentage: Double?,
    val status: String,
    val closeReason: String?,
    val isLiquidated: Boolean = false,
    val openTime: Long?,
    val closeTime: Long?,
    val holdingSeconds: Long?,
    val exchange: String?,
    val marketType: String?,
    val notes: String?
) {
    /**
     * Check if this is a closed trade (including liquidations)
     */
    fun isClosed(): Boolean = status == "CLOSED" || status == "closed"

    /**
     * Check if this is a losing trade
     */
    fun isLosingTrade(): Boolean = pnlAmount != null && pnlAmount < 0

    /**
     * Check if this is a winning trade
     */
    fun isWinningTrade(): Boolean = pnlAmount != null && pnlAmount > 0
}

// Extension functions
fun TradeEntity.toDomain(): Trade = Trade(
    id = id,
    userId = userId,
    symbol = symbol,
    position = position,
    leverage = leverage,
    entryPrice = entryPrice,
    exitPrice = exitPrice,
    quantity = quantity,
    margin = margin,
    notional = notional,
    pnlAmount = pnlAmount,
    pnlPercentage = pnlPercentage,
    status = status,
    closeReason = closeReason,
    isLiquidated = isLiquidated,
    openTime = openTime,
    closeTime = closeTime,
    holdingSeconds = holdingSeconds,
    exchange = exchange,
    marketType = marketType,
    notes = notes
)

fun Trade.toEntity(): TradeEntity = TradeEntity(
    id = id,
    userId = userId,
    symbol = symbol,
    position = position,
    leverage = leverage,
    entryPrice = entryPrice,
    exitPrice = exitPrice,
    quantity = quantity,
    margin = margin,
    notional = notional,
    pnlAmount = pnlAmount,
    pnlPercentage = pnlPercentage,
    status = status,
    closeReason = closeReason,
    isLiquidated = isLiquidated,
    openTime = openTime,
    closeTime = closeTime,
    holdingSeconds = holdingSeconds,
    exchange = exchange,
    marketType = marketType,
    notes = notes
)
