package com.tradingdashboard.data.repository

import com.tradingdashboard.data.local.dao.TradeDao
import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.data.model.TradeEntity
import com.tradingdashboard.data.model.toDomain
import com.tradingdashboard.data.model.toEntity
import com.tradingdashboard.data.remote.SupabaseClient
import com.tradingdashboard.util.Constants
import com.tradingdashboard.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Trade Repository - handles all trade data operations
 * Implements offline-first pattern with local Room DB and remote Supabase sync
 */
class TradeRepository @Inject constructor(
    private val tradeDao: TradeDao,
    private val supabaseClient: SupabaseClient
) {
    
    // ============ Local Database Operations ============

    /**
     * Get all trades for user as Flow (reactive updates)
     */
    fun getUserTradesFlow(userId: String): Flow<List<Trade>> {
        return tradeDao.getUserTradesFlow(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Get all trades for user (one-time fetch)
     */
    suspend fun getUserTrades(userId: String): List<Trade> {
        return try {
            Logger.d("Fetching all user trades from local DB")
            tradeDao.getUserTrades(userId).map { it.toDomain() }
        } catch (e: Exception) {
            Logger.e("Error fetching user trades", throwable = e)
            emptyList()
        }
    }
    
    /**
     * Get open trades as Flow
     */
    fun getOpenTradesFlow(userId: String): Flow<List<Trade>> {
        return tradeDao.getOpenTradesFlow(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Get closed trades
     */
    suspend fun getClosedTrades(userId: String): List<Trade> {
        return try {
            Logger.d("Fetching closed trades from local DB")
            tradeDao.getClosedTrades(userId).map { it.toDomain() }
        } catch (e: Exception) {
            Logger.e("Error fetching closed trades", throwable = e)
            emptyList()
        }
    }
    
    /**
     * Get single trade by ID
     */
    suspend fun getTradeById(tradeId: String): Trade? {
        return try {
            Logger.d("Fetching trade $tradeId from local DB")
            tradeDao.getTradeById(tradeId)?.toDomain()
        } catch (e: Exception) {
            Logger.e("Error fetching trade $tradeId", throwable = e)
            null
        }
    }
    
    // ============ Create/Update/Delete Operations ============

    /**
     * Add new trade to local DB and mark for sync
     */
    suspend fun addTrade(trade: Trade): Result<Unit> = try {
        Logger.d("Adding new trade: ${trade.symbol}")
        
        // Save to local DB
        val entity = trade.toEntity().copy(isSynced = false)
        tradeDao.insertTrade(entity)
        
        Logger.d("Trade added successfully, queued for sync")
        Result.success(Unit)
    } catch (e: Exception) {
        Logger.e("Error adding trade", throwable = e)
        Result.failure(e)
    }
    
    /**
     * Update existing trade and mark for sync
     */
    suspend fun updateTrade(trade: Trade): Result<Unit> = try {
        Logger.d("Updating trade: ${trade.id}")
        
        // Update local DB
        val entity = trade.toEntity().copy(isSynced = false)
        tradeDao.updateTrade(entity)
        
        Logger.d("Trade updated successfully, queued for sync")
        Result.success(Unit)
    } catch (e: Exception) {
        Logger.e("Error updating trade", throwable = e)
        Result.failure(e)
    }
    
    /**
     * Delete trade from local DB and mark for sync
     */
    suspend fun deleteTrade(tradeId: String): Result<Unit> = try {
        Logger.d("Deleting trade: $tradeId")
        
        tradeDao.deleteTradeById(tradeId)
        
        Logger.d("Trade deleted successfully")
        Result.success(Unit)
    } catch (e: Exception) {
        Logger.e("Error deleting trade", throwable = e)
        Result.failure(e)
    }
    
    // ============ Statistics ============

    /**
     * Get comprehensive trade statistics
     */
    suspend fun getTradeStats(userId: String): TradeStats {
        return try {
            Logger.d("Calculating trade statistics for user: $userId")
            
            val totalTrades = tradeDao.getTradeCount(userId)
            val openTrades = tradeDao.getOpenTradeCount(userId)
            val closedTrades = tradeDao.getClosedTradeCount(userId)
            val realizedPnl = tradeDao.getRealizedPnl(userId) ?: 0.0
            
            val allTrades = tradeDao.getUserTrades(userId)
            val closedTradesList = allTrades.filter { it.status == Constants.Trade.STATUS_CLOSED }
            
            // Calculate win/loss metrics
            val winningTrades = closedTradesList.filter { (it.pnlAmount ?: 0.0) > 0 }
            val losingTrades = closedTradesList.filter { (it.pnlAmount ?: 0.0) < 0 }
            
            val winRate = if (closedTrades > 0) {
                (winningTrades.size.toDouble() / closedTrades.toDouble()) * 100
            } else {
                0.0
            }
            
            val lossRate = if (closedTrades > 0) {
                (losingTrades.size.toDouble() / closedTrades.toDouble()) * 100
            } else {
                0.0
            }
            
            // Calculate profit and loss amounts
            val totalProfit = winningTrades.sumOf { it.pnlAmount ?: 0.0 }
            val totalLoss = kotlin.math.abs(losingTrades.sumOf { it.pnlAmount ?: 0.0 })
            
            // Calculate profit and loss percentages (of total P&L composition)
            val totalPnLAmount = totalProfit + totalLoss
            val profitPercent = if (totalPnLAmount > 0.0) {
                (totalProfit / totalPnLAmount) * 100.0
            } else {
                0.0
            }
            val lossPercent = if (totalPnLAmount > 0.0) {
                (totalLoss / totalPnLAmount) * 100.0
            } else {
                0.0
            }
            
            // Calculate liquidation metrics
            val liquidatedTrades = closedTradesList.filter { it.isLiquidated }
            val liquidationLosses = liquidatedTrades
                .filter { it.pnlAmount != null && it.pnlAmount < 0 }
                .map { kotlin.math.abs(it.pnlAmount!!) }
            
            val totalLiquidationLoss = liquidationLosses.sumOf { it }
            val liquidationRate = if (closedTrades > 0) {
                (liquidatedTrades.size.toDouble() / closedTrades.toDouble()) * 100
            } else {
                0.0
            }
            val liquidationLossRate = if (totalLoss > 0) {
                (totalLiquidationLoss / totalLoss) * 100
            } else {
                0.0
            }
            
            val totalMargin = allTrades.sumOf { it.margin ?: 0.0 }
            val avgPnl = if (closedTrades > 0) realizedPnl / closedTrades else 0.0
            
            TradeStats(
                totalTrades = totalTrades,
                openPositions = openTrades,
                closedTrades = closedTrades,
                realizedPnl = realizedPnl,
                totalInvestment = totalMargin,
                winRate = winRate,
                lossRate = lossRate,
                totalProfit = totalProfit,
                totalLoss = totalLoss,
                profitPercent = profitPercent,
                lossPercent = lossPercent,
                averagePnl = avgPnl,
                totalLiquidations = liquidatedTrades.size,
                liquidationLoss = totalLiquidationLoss,
                liquidationRate = liquidationRate,
                liquidationLossRate = liquidationLossRate,
                cascadeLiquidations = 0 // TODO: Implement cascade detection
            )
        } catch (e: Exception) {
            Logger.e("Error calculating stats", throwable = e)
            TradeStats()
        }
    }
    
    // ============ Server Sync Operations ============

    /**
     * Pull latest trades from Supabase and update local cache
     */
    suspend fun pullFromServer(userId: String): Result<List<Trade>> = try {
        Logger.methodEntry("pullFromServer")
        Logger.d("Pulling trades from Supabase for user: $userId")
        
        val serverTrades = supabaseClient.getUserTrades(userId)
        
        if (serverTrades.isNotEmpty()) {
            // Update local cache with server data
            val entities = serverTrades.map { it.copy(isSynced = true) }
            tradeDao.insertTrades(entities)
            
            Logger.d("Pulled ${serverTrades.size} trades from server")
        }
        
        Logger.methodExit("pullFromServer", result = serverTrades.size)
        Result.success(serverTrades)
    } catch (e: Exception) {
        Logger.e("Error pulling from server", throwable = e)
        Result.failure(e)
    }
    
    /**
     * Push unsynced trades to Supabase
     */
    suspend fun pushToServer(userId: String): Result<Int> = try {
        Logger.methodEntry("pushToServer")
        Logger.d("Pushing unsynced trades to Supabase")
        
        val unsyncedTrades = tradeDao.getUnsyncedTrades(userId)
        var successCount = 0
        var failureCount = 0
        
        for (tradeEntity in unsyncedTrades) {
            try {
                val trade = tradeEntity.toDomain()
                supabaseClient.upsertTrade(trade)
                tradeDao.markSynced(tradeEntity.id)
                successCount++
                Logger.d("Synced trade: ${trade.symbol}")
            } catch (e: Exception) {
                failureCount++
                Logger.e("Failed to sync trade: ${tradeEntity.symbol}", throwable = e)
            }
        }
        
        Logger.d("Push complete: $successCount synced, $failureCount failed")
        Logger.methodExit("pushToServer", result = successCount)
        
        // Return success if at least some trades synced
        if (successCount > 0 || failureCount == 0) {
            Result.success(successCount)
        } else {
            Result.failure(Exception("All trades failed to sync"))
        }
    } catch (e: Exception) {
        Logger.e("Error pushing to server", throwable = e)
        Result.failure(e)
    }
    
    /**
     * Full sync: pull from server, push local changes
     * Implements bi-directional sync with conflict resolution
     */
    suspend fun fullSync(userId: String): Result<SyncResult> = try {
        Logger.section("Starting Full Sync")
        
        val syncStart = System.currentTimeMillis()
        
        // Step 1: Pull latest from server
        val pullResult = pullFromServer(userId)
        val pulledCount = pullResult.getOrNull()?.size ?: 0
        
        // Step 2: Push local changes
        val pushResult = pushToServer(userId)
        val pushedCount = pushResult.getOrNull() ?: 0
        
        // Step 3: Handle conflicts
        val conflictCount = resolveConflicts(userId)
        
        val duration = System.currentTimeMillis() - syncStart
        
        val result = SyncResult(
            pulledCount = pulledCount,
            pushedCount = pushedCount,
            conflictCount = conflictCount,
            durationMs = duration,
            isSuccessful = pullResult.isSuccess && pushResult.isSuccess
        )
        
        Logger.d("Sync complete: $result")
        Logger.section("Sync Completed")
        
        Result.success(result)
    } catch (e: Exception) {
        Logger.e("Error during full sync", throwable = e)
        Result.failure(e)
    }
    
    /**
     * Resolve conflicts between local and server versions
     * Strategy: Local changes take precedence if newer
     */
    private suspend fun resolveConflicts(userId: String): Int {
        // Placeholder for conflict resolution
        // In production, implement proper merge logic
        return 0
    }
    
    /**
     * Get count of unsynced trades
     */
    suspend fun getUnsyncedCount(userId: String): Int {
        return try {
            tradeDao.getUnsyncedTrades(userId).size
        } catch (e: Exception) {
            Logger.e("Error getting unsynced count", throwable = e)
            0
        }
    }
}

/**
 * Trade statistics data class
 */
data class TradeStats(
    val totalTrades: Int = 0,
    val openPositions: Int = 0,
    val closedTrades: Int = 0,
    val realizedPnl: Double = 0.0,
    val totalInvestment: Double = 0.0,
    val winRate: Double = 0.0,
    val lossRate: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalLoss: Double = 0.0,
    val profitPercent: Double = 0.0,
    val lossPercent: Double = 0.0,
    val averagePnl: Double = 0.0,
    
    // Liquidation metrics
    val totalLiquidations: Int = 0,
    val liquidationLoss: Double = 0.0,
    val liquidationRate: Double = 0.0, // % of closed trades liquidated
    val liquidationLossRate: Double = 0.0, // % of total losses from liquidations
    val cascadeLiquidations: Int = 0 // Multiple liquidations close together
)

/**
 * Sync operation result
 */
data class SyncResult(
    val pulledCount: Int = 0,
    val pushedCount: Int = 0,
    val conflictCount: Int = 0,
    val durationMs: Long = 0L,
    val isSuccessful: Boolean = false
) {
    override fun toString(): String {
        return "Pulled: $pulledCount | Pushed: $pushedCount | Conflicts: $conflictCount | ${durationMs}ms"
    }
}
