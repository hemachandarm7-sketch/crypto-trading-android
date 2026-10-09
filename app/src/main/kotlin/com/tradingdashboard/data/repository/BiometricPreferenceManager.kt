package com.tradingdashboard.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.tradingdashboard.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manager for biometric preferences using DataStore
 */
class BiometricPreferenceManager(
    private val context: Context
) {
    companion object {
        private const val TAG = "BiometricPrefManager"
        private val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        private val BIOMETRIC_SETUP_COMPLETED = booleanPreferencesKey("biometric_setup_completed")
    }

    private val Context.dataStore by preferencesDataStore(name = "biometric_prefs")
    private val dataStore = context.dataStore

    /**
     * Enable biometric authentication
     */
    suspend fun enableBiometric(): Result<Unit> = try {
        Logger.d(TAG, "Enabling biometric")
        dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] = true
            preferences[BIOMETRIC_SETUP_COMPLETED] = true
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Logger.e(TAG, "Error enabling biometric: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Disable biometric authentication
     */
    suspend fun disableBiometric(): Result<Unit> = try {
        Logger.d(TAG, "Disabling biometric")
        dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] = false
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Logger.e(TAG, "Error disabling biometric: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Check if biometric is enabled
     */
    suspend fun isBiometricEnabled(): Boolean {
        return try {
            dataStore.data.map { preferences ->
                preferences[IS_BIOMETRIC_ENABLED] ?: false
            }.collect { enabled ->
                return@suspend enabled
            }
            false
        } catch (e: Exception) {
            Logger.e(TAG, "Error reading biometric preference: ${e.message}", e)
            false
        }
    }

    /**
     * Get biometric enabled status as Flow
     */
    fun isBiometricEnabledFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] ?: false
        }
    }

    /**
     * Check if biometric setup is completed
     */
    suspend fun isBiometricSetupCompleted(): Boolean {
        return try {
            dataStore.data.map { preferences ->
                preferences[BIOMETRIC_SETUP_COMPLETED] ?: false
            }.collect { completed ->
                return@suspend completed
            }
            false
        } catch (e: Exception) {
            Logger.e(TAG, "Error reading biometric setup status: ${e.message}", e)
            false
        }
    }

    /**
     * Mark biometric setup as completed
     */
    suspend fun markBiometricSetupCompleted(): Result<Unit> = try {
        Logger.d(TAG, "Marking biometric setup as completed")
        dataStore.edit { preferences ->
            preferences[BIOMETRIC_SETUP_COMPLETED] = true
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Logger.e(TAG, "Error marking setup completed: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Reset biometric preferences
     */
    suspend fun resetBiometricPreferences(): Result<Unit> = try {
        Logger.d(TAG, "Resetting biometric preferences")
        dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] = false
            preferences[BIOMETRIC_SETUP_COMPLETED] = false
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Logger.e(TAG, "Error resetting preferences: ${e.message}", e)
        Result.failure(e)
    }
}

/**
 * Extension function to collect first value from Flow
 */
private suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (T) -> Unit) {
    kotlinx.coroutines.flow.collect { value ->
        action(value)
    }
}
