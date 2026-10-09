package com.tradingdashboard.data.remote

import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.data.model.TradeEntity
import com.tradingdashboard.util.Constants
import com.tradingdashboard.util.Logger
import io.github.supabase.SupabaseClient
import io.github.supabase.postgrest.query.Columns
import io.github.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase API Client - handles all remote API calls for trades
 * Provides type-safe wrappers around Supabase Postgrest API
 */
@Singleton
class SupabaseClient @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    
    companion object {
        private const val TABLE_TRADES = "trades"
        private const val NETWORK_TIMEOUT_MS = 30_000L
    }

    // ============ Read Operations ============

    /**
     * Get all trades for a specific user
     */
    suspend fun getUserTrades(userId: String): List<TradeEntity> {
        return try {
            Logger.logRequest("GET /${TABLE_TRADES}?user_id=eq.$userId")
            
            val response = supabaseClient
                .from(TABLE_TRADES)
                .select {
                    filter("user_id", "eq", userId)
                    order("open_time", Order.DESCENDING)
                }
                .decodeList<SupabaseTradeDto>()
            
            Logger.logResponse(
                "GET /${TABLE_TRADES}",
                statusCode = 200,
                duration = 0
            )
            
            response.map { it.toTradeEntity() }
        } catch (e: Exception) {
            Logger.e("Error fetching user trades from Supabase", throwable = e)
            throw SupabaseException("Failed to fetch trades", e)
        }
    }

    /**
     * Get single trade by ID
     */
    suspend fun getTradeById(tradeId: String): TradeEntity? {
        return try {
            Logger.logRequest("GET /${TABLE_TRADES}/$tradeId")
            
            val response = supabaseClient
                .from(TABLE_TRADES)
                .select {
                    filter("id", "eq", tradeId)
                }
                .decodeSingleOrNull<SupabaseTradeDto>()
            
            Logger.logResponse(
                "GET /${TABLE_TRADES}/$tradeId",
                statusCode = if (response != null) 200 else 404,
                duration = 0
            )
            
            response?.toTradeEntity()
        } catch (e: Exception) {
            Logger.e("Error fetching trade $tradeId from Supabase", throwable = e)
            null
        }
    }

    /**
     * Get open trades for user
     */
    suspend fun getOpenTrades(userId: String): List<TradeEntity> {
        return try {
            Logger.logRequest("GET /${TABLE_TRADES}?user_id=eq.$userId&status=eq.OPEN")
            
            val response = supabaseClient
                .from(TABLE_TRADES)
                .select {
                    filter("user_id", "eq", userId)
                    filter("status", "eq", Constants.Trade.STATUS_OPEN)
                    order("open_time", Order.DESCENDING)
                }
                .decodeList<SupabaseTradeDto>()
            
            response.map { it.toTradeEntity() }
        } catch (e: Exception) {
            Logger.e("Error fetching open trades", throwable = e)
            emptyList()
        }
    }

    /**
     * Get closed trades for user
     */
    suspend fun getClosedTrades(userId: String): List<TradeEntity> {
        return try {
            Logger.logRequest("GET /${TABLE_TRADES}?user_id=eq.$userId&status=eq.CLOSED")
            
            val response = supabaseClient
                .from(TABLE_TRADES)
                .select {
                    filter("user_id", "eq", userId)
                    filter("status", "eq", Constants.Trade.STATUS_CLOSED)
                    order("close_time", Order.DESCENDING)
                }
                .decodeList<SupabaseTradeDto>()
            
            response.map { it.toTradeEntity() }
        } catch (e: Exception) {
            Logger.e("Error fetching closed trades", throwable = e)
            emptyList()
        }
    }

    /**
     * Get trades within date range
     */
    suspend fun getTradesInRange(
        userId: String,
        startTime: Long,
        endTime: Long
    ): List<TradeEntity> {
        return try {
            Logger.logRequest("GET /${TABLE_TRADES}?user_id=eq.$userId&open_time=gte.$startTime&open_time=lte.$endTime")
            
            val response = supabaseClient
                .from(TABLE_TRADES)
                .select {
                    filter("user_id", "eq", userId)
                    filter("open_time", "gte", startTime)
                    filter("open_time", "lte", endTime)
                    order("open_time", Order.DESCENDING)
                }
                .decodeList<SupabaseTradeDto>()
            
            response.map { it.toTradeEntity() }
        } catch (e: Exception) {
            Logger.e("Error fetching trades in range", throwable = e)
            emptyList()
        }
    }

    // ============ Write Operations ============

    /**
     * Create new trade on server
     */
    suspend fun createTrade(trade: Trade): TradeEntity {
        return try {
            Logger.logRequest("POST /${TABLE_TRADES}")
            
            val dto = trade.toSupabaseDto()
            val response = supabaseClient
                .from(TABLE_TRADES)
                .insert(dto)
                .decodeSingle<SupabaseTradeDto>()
            
            Logger.logResponse(
                "POST /${TABLE_TRADES}",
                statusCode = 201,
                duration = 0
            )
            
            response.toTradeEntity()
        } catch (e: Exception) {
            Logger.e("Error creating trade on Supabase", throwable = e)
            throw SupabaseException("Failed to create trade", e)
        }
    }

    /**
     * Update existing trade on server
     */
    suspend fun updateTrade(trade: Trade): TradeEntity {
        return try {
            Logger.logRequest("PATCH /${TABLE_TRADES}/${trade.id}")
            
            val dto = trade.toSupabaseDto()
            val response = supabaseClient
                .from(TABLE_TRADES)
                .update(dto) {
                    filter("id", "eq", trade.id)
                }
                .decodeSingle<SupabaseTradeDto>()
            
            Logger.logResponse(
                "PATCH /${TABLE_TRADES}/${trade.id}",
                statusCode = 200,
                duration = 0
            )
            
            response.toTradeEntity()
        } catch (e: Exception) {
            Logger.e("Error updating trade on Supabase", throwable = e)
            throw SupabaseException("Failed to update trade", e)
        }
    }

    /**
     * Upsert trade (insert or update)
     */
    suspend fun upsertTrade(trade: Trade): TradeEntity {
        return try {
            Logger.logRequest("UPSERT /${TABLE_TRADES}/${trade.id}")
            
            val dto = trade.toSupabaseDto()
            val response = supabaseClient
                .from(TABLE_TRADES)
                .upsert(dto)
                .decodeSingle<SupabaseTradeDto>()
            
            Logger.logResponse(
                "UPSERT /${TABLE_TRADES}",
                statusCode = 200,
                duration = 0
            )
            
            response.toTradeEntity()
        } catch (e: Exception) {
            Logger.e("Error upserting trade on Supabase", throwable = e)
            throw SupabaseException("Failed to upsert trade", e)
        }
    }

    /**
     * Delete trade from server
     */
    suspend fun deleteTrade(tradeId: String): Boolean {
        return try {
            Logger.logRequest("DELETE /${TABLE_TRADES}/$tradeId")
            
            supabaseClient
                .from(TABLE_TRADES)
                .delete {
                    filter("id", "eq", tradeId)
                }
            
            Logger.logResponse(
                "DELETE /${TABLE_TRADES}/$tradeId",
                statusCode = 204,
                duration = 0
            )
            
            true
        } catch (e: Exception) {
            Logger.e("Error deleting trade on Supabase", throwable = e)
            false
        }
    }

    /**
     * Batch upsert trades
     */
    suspend fun batchUpsertTrades(trades: List<Trade>): Int {
        return try {
            Logger.logRequest("BATCH UPSERT /${TABLE_TRADES}")
            
            var successCount = 0
            for (trade in trades) {
                try {
                    upsertTrade(trade)
                    successCount++
                } catch (e: Exception) {
                    Logger.e("Failed to upsert trade ${trade.id}", throwable = e)
                }
            }
            
            Logger.d("Batch upsert: $successCount/${trades.size} successful")
            successCount
        } catch (e: Exception) {
            Logger.e("Error in batch upsert", throwable = e)
            0
        }
    }

    // ============ Helper Functions ============

    /**
     * Check if connection to Supabase is available
     */
    suspend fun checkConnection(): Boolean {
        return try {
            Logger.d("Checking Supabase connection...")
            // Simple query to test connection
            supabaseClient
                .from(TABLE_TRADES)
                .select(columns = Columns.list("id"))
                .limit(1)
                .decodeList<Map<String, Any>>()
            
            Logger.d("Supabase connection OK")
            true
        } catch (e: Exception) {
            Logger.e("Supabase connection failed", throwable = e)
            false
        }
    }
}

// ============ DTO Classes ============

/**
 * Supabase Trade Data Transfer Object
 * Maps between API response and domain model
 */
@Serializable
data class SupabaseTradeDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val symbol: String,
    val direction: String,  // LONG or SHORT
    val leverage: Double,
    @SerialName("entry_price")
    val entryPrice: Double,
    @SerialName("exit_price")
    val exitPrice: Double? = null,
    val quantity: Double,
    val margin: Double,
    val notional: Double? = null,
    @SerialName("pnl_amount")
    val pnlAmount: Double? = null,
    @SerialName("pnl_percentage")
    val pnlPercentage: Double? = null,
    val status: String,  // OPEN or CLOSED
    @SerialName("close_reason")
    val closeReason: String? = null,
    @SerialName("open_time")
    val openTime: Long,
    @SerialName("close_time")
    val closeTime: Long? = null,
    @SerialName("holding_seconds")
    val holdingSeconds: Long? = null,
    val exchange: String? = null,
    @SerialName("market_type")
    val marketType: String? = null,
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    fun toTradeEntity(): TradeEntity {
        return TradeEntity(
            id = id,
            userId = userId,
            symbol = symbol,
            position = direction,
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
            openTime = openTime,
            closeTime = closeTime,
            holdingSeconds = holdingSeconds,
            exchange = exchange ?: "BINANCE",
            marketType = marketType ?: "FUTURES",
            notes = notes,
            isSynced = true
        )
    }
}

// ============ Conversion Extensions ============

/**
 * Convert Trade domain model to Supabase DTO
 */
fun Trade.toSupabaseDto(): SupabaseTradeDto {
    return SupabaseTradeDto(
        id = id,
        userId = userId,
        symbol = symbol,
        direction = position,
        leverage = leverage,
        entryPrice = entryPrice,
        exitPrice = exitPrice,
        quantity = quantity,
        margin = margin,
        notional = (quantity * entryPrice),
        pnlAmount = pnlAmount,
        pnlPercentage = if (margin > 0) ((pnlAmount ?: 0.0) / margin) * 100 else null,
        status = status,
        closeReason = null,
        openTime = openTime,
        closeTime = closeTime,
        holdingSeconds = if (closeTime != null) (closeTime - openTime) / 1000 else null,
        exchange = "BINANCE",
        marketType = "FUTURES",
        notes = notes
    )
}

// ============ Exception Classes ============

/**
 * Custom exception for Supabase API errors
 */
class SupabaseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
