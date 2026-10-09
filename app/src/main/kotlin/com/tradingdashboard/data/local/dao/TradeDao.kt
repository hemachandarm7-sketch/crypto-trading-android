package com.tradingdashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tradingdashboard.data.model.TradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    
    @Query("SELECT * FROM trades WHERE userId = :userId ORDER BY openTime DESC")
    fun getUserTradesFlow(userId: String): Flow<List<TradeEntity>>
    
    @Query("SELECT * FROM trades WHERE userId = :userId ORDER BY openTime DESC")
    suspend fun getUserTrades(userId: String): List<TradeEntity>
    
    @Query("SELECT * FROM trades WHERE userId = :userId AND status = 'OPEN'")
    fun getOpenTradesFlow(userId: String): Flow<List<TradeEntity>>
    
    @Query("SELECT * FROM trades WHERE userId = :userId AND status = 'CLOSED'")
    suspend fun getClosedTrades(userId: String): List<TradeEntity>
    
    @Query("SELECT * FROM trades WHERE id = :tradeId")
    suspend fun getTradeById(tradeId: String): TradeEntity?
    
    @Query("SELECT * FROM trades WHERE userId = :userId AND syncedWithServer = 0")
    suspend fun getUnsyncedTrades(userId: String): List<TradeEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<TradeEntity>)
    
    @Update
    suspend fun updateTrade(trade: TradeEntity)
    
    @Delete
    suspend fun deleteTrade(trade: TradeEntity)
    
    @Query("DELETE FROM trades WHERE id = :tradeId")
    suspend fun deleteTradeById(tradeId: String)
    
    @Query("UPDATE trades SET syncedWithServer = 1 WHERE id = :tradeId")
    suspend fun markSynced(tradeId: String)
    
    @Query("SELECT COUNT(*) FROM trades WHERE userId = :userId")
    suspend fun getTradeCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM trades WHERE userId = :userId AND status = 'OPEN'")
    suspend fun getOpenTradeCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM trades WHERE userId = :userId AND status = 'CLOSED'")
    suspend fun getClosedTradeCount(userId: String): Int
    
    @Query("SELECT SUM(pnlAmount) FROM trades WHERE userId = :userId AND status = 'CLOSED'")
    suspend fun getRealizedPnl(userId: String): Double?
}
