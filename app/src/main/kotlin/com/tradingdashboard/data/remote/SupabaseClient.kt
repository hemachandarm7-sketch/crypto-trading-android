package com.tradingdashboard.data.remote

import android.content.Context
import com.tradingdashboard.BuildConfig
import io.github.supabase.SupabaseClient
import io.github.supabase.createSupabaseClient
import io.github.supabase.gotrue.GoTrue
import io.github.supabase.postgrest.Postgrest
import io.github.supabase.storage.Storage
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging

object SupabaseProvider {
    
    private var supabaseClient: SupabaseClient? = null
    
    fun initialize(context: Context) {
        if (supabaseClient == null) {
            supabaseClient = createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(GoTrue)
                install(Storage)
                install(Postgrest)
                
                // Custom HTTP client with logging
                httpClient = HttpClient(Android) {
                    install(Logging) {
                        level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
                    }
                }
            }
        }
    }
    
    fun getClient(): SupabaseClient {
        return supabaseClient ?: throw IllegalStateException("SupabaseClient not initialized. Call initialize() first.")
    }
}

// Singleton accessor
val supabase: SupabaseClient
    get() = SupabaseProvider.getClient()
