package com.tradingdashboard.di

import android.content.Context
import com.tradingdashboard.data.repository.AuthRepository
import com.tradingdashboard.data.repository.BiometricAuthManager
import com.tradingdashboard.data.repository.BiometricPreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import javax.inject.Singleton

/**
 * Hilt module for authentication and biometric dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    /**
     * Provide Supabase Auth instance
     */
    @Singleton
    @Provides
    fun provideSupabaseAuth(): Auth {
        val client = createSupabaseClient(
            supabaseUrl = "https://hqjdcqofotscnhuppqgd.supabase.co",
            supabaseKey = "sb_publishable_L1EgmZf6JURBj9Uggoisrg_tHNYAib"
        )
        return client.auth
    }

    /**
     * Provide AuthRepository instance
     */
    @Singleton
    @Provides
    fun provideAuthRepository(
        auth: Auth,
        @ApplicationContext context: Context
    ): AuthRepository {
        return AuthRepository(auth, context)
    }

    /**
     * Provide BiometricAuthManager instance
     */
    @Singleton
    @Provides
    fun provideBiometricAuthManager(
        @ApplicationContext context: Context
    ): BiometricAuthManager {
        return BiometricAuthManager(context)
    }

    /**
     * Provide BiometricPreferenceManager instance
     */
    @Singleton
    @Provides
    fun provideBiometricPreferenceManager(
        @ApplicationContext context: Context
    ): BiometricPreferenceManager {
        return BiometricPreferenceManager(context)
    }
}
