package com.tradingdashboard.di

import android.content.Context
import androidx.room.Room
import com.tradingdashboard.data.local.database.TradingDatabase
import com.tradingdashboard.data.local.dao.TradeDao
import com.tradingdashboard.data.local.dao.SettingsDao
import com.tradingdashboard.data.remote.SupabaseProvider
import com.tradingdashboard.data.repository.TradeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Singleton
    @Provides
    fun provideTradingDatabase(
        @ApplicationContext context: Context
    ): TradingDatabase {
        return Room.databaseBuilder(
            context,
            TradingDatabase::class.java,
            "trading_database"
        ).fallbackToDestructiveMigration().build()
    }
    
    @Singleton
    @Provides
    fun provideTradeDao(database: TradingDatabase): TradeDao {
        return database.tradeDao()
    }
    
    @Singleton
    @Provides
    fun provideSettingsDao(database: TradingDatabase): SettingsDao {
        return database.settingsDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    
    @Singleton
    @Provides
    fun provideSupabaseClient(
        @ApplicationContext context: Context
    ): com.tradingdashboard.data.remote.SupabaseClient {
        SupabaseProvider.initialize(context)
        return SupabaseProvider.getClient()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Singleton
    @Provides
    fun provideTradeRepository(
        tradeDao: TradeDao
    ): TradeRepository {
        return TradeRepository(tradeDao)
    }
}
