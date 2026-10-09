package com.tradingdashboard.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tradingdashboard.data.local.dao.TradeDao
import com.tradingdashboard.data.local.dao.SettingsDao
import com.tradingdashboard.data.model.TradeEntity
import com.tradingdashboard.data.model.SettingsEntity

@Database(
    entities = [
        TradeEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class TradingDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao
    abstract fun settingsDao(): SettingsDao
}
