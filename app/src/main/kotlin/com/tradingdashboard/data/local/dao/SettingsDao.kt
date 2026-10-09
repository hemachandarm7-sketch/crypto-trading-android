package com.tradingdashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tradingdashboard.data.model.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    
    @Query("SELECT * FROM settings WHERE userId = :userId")
    fun getSettingsFlow(userId: String): Flow<SettingsEntity?>
    
    @Query("SELECT * FROM settings WHERE userId = :userId")
    suspend fun getSettings(userId: String): SettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: SettingsEntity)
    
    @Query("UPDATE settings SET theme = :theme WHERE userId = :userId")
    suspend fun updateTheme(userId: String, theme: String)
    
    @Query("UPDATE settings SET currency = :currency WHERE userId = :userId")
    suspend fun updateCurrency(userId: String, currency: String)
    
    @Query("UPDATE settings SET biometricEnabled = :enabled WHERE userId = :userId")
    suspend fun updateBiometric(userId: String, enabled: Boolean)
}
