package com.tradingdashboard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val userId: String,
    val currency: String = "USD",
    val theme: String = "DARK",
    val defaultLeverage: Double = 1.0,
    val defaultExchange: String = "BINANCE",
    val notificationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val autoSyncEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

// Domain model
data class Settings(
    val userId: String,
    val currency: String,
    val theme: String,
    val defaultLeverage: Double,
    val defaultExchange: String,
    val notificationsEnabled: Boolean,
    val biometricEnabled: Boolean,
    val autoSyncEnabled: Boolean
)

fun SettingsEntity.toDomain(): Settings = Settings(
    userId = userId,
    currency = currency,
    theme = theme,
    defaultLeverage = defaultLeverage,
    defaultExchange = defaultExchange,
    notificationsEnabled = notificationsEnabled,
    biometricEnabled = biometricEnabled,
    autoSyncEnabled = autoSyncEnabled
)

fun Settings.toEntity(): SettingsEntity = SettingsEntity(
    userId = userId,
    currency = currency,
    theme = theme,
    defaultLeverage = defaultLeverage,
    defaultExchange = defaultExchange,
    notificationsEnabled = notificationsEnabled,
    biometricEnabled = biometricEnabled,
    autoSyncEnabled = autoSyncEnabled
)
