package com.tradingdashboard.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tradingdashboard.util.Logger
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for handling user authentication via Supabase GoTrue
 */
class AuthRepository(
    private val auth: Auth,
    private val context: Context
) {
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val SESSION_TOKEN_KEY = stringPreferencesKey("session_token")
        private const val TAG = "AuthRepository"
    }

    private val Context.dataStore by preferencesDataStore(name = "auth_prefs")
    private val dataStore = context.dataStore

    /**
     * Sign up with email and password
     * @return Result containing userId on success or error message on failure
     */
    suspend fun signUp(email: String, password: String): Result<String> = try {
        Logger.d(TAG, "Attempting signup for $email")
        
        // Validate inputs
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        // Call Supabase auth signup
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        // Get the new user's ID
        val userId = auth.currentUserOrNull()?.id
        if (userId != null) {
            // Save to local storage
            dataStore.edit { preferences ->
                preferences[USER_ID_KEY] = userId
                preferences[USER_EMAIL_KEY] = email
            }
            Logger.d(TAG, "Signup successful for userId: $userId")
            Result.success(userId)
        } else {
            Result.failure(Exception("Failed to retrieve user ID after signup"))
        }
    } catch (e: AuthRestException) {
        Logger.e(TAG, "Signup failed: ${e.message}", e)
        Result.failure(Exception(e.message ?: "Signup failed"))
    } catch (e: Exception) {
        Logger.e(TAG, "Signup error: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Sign in with email and password
     * @return Result containing userId on success or error message on failure
     */
    suspend fun signIn(email: String, password: String): Result<String> = try {
        Logger.d(TAG, "Attempting signin for $email")
        
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }

        // Call Supabase auth signin
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        // Get the user's ID
        val userId = auth.currentUserOrNull()?.id
        if (userId != null) {
            // Save to local storage
            dataStore.edit { preferences ->
                preferences[USER_ID_KEY] = userId
                preferences[USER_EMAIL_KEY] = email
            }
            Logger.d(TAG, "Signin successful for userId: $userId")
            Result.success(userId)
        } else {
            Result.failure(Exception("Failed to retrieve user ID after signin"))
        }
    } catch (e: AuthRestException) {
        Logger.e(TAG, "Signin failed: ${e.message}", e)
        // Provide user-friendly error messages
        val errorMsg = when {
            e.message?.contains("Invalid login credentials") == true -> "Invalid email or password"
            e.message?.contains("Email not confirmed") == true -> "Please verify your email"
            else -> e.message ?: "Sign in failed"
        }
        Result.failure(Exception(errorMsg))
    } catch (e: Exception) {
        Logger.e(TAG, "Signin error: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Sign out the current user
     */
    suspend fun signOut(): Result<Unit> = try {
        Logger.d(TAG, "Signing out user")
        auth.signOut()
        
        // Clear local storage
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(SESSION_TOKEN_KEY)
        }
        Logger.d(TAG, "Signout successful")
        Result.success(Unit)
    } catch (e: Exception) {
        Logger.e(TAG, "Signout error: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Get current logged-in user ID
     */
    suspend fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id ?: getSavedUserId()
    }

    /**
     * Get current logged-in user email
     */
    suspend fun getCurrentUserEmail(): String? {
        return auth.currentUserOrNull()?.email ?: getSavedUserEmail()
    }

    /**
     * Check if user is authenticated
     */
    suspend fun isUserAuthenticated(): Boolean {
        return auth.currentUserOrNull() != null
    }

    /**
     * Get saved user ID from local storage
     */
    private suspend fun getSavedUserId(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }.firstValue()
    }

    /**
     * Get saved user email from local storage
     */
    private suspend fun getSavedUserEmail(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }.firstValue()
    }

    /**
     * Get user ID as Flow for reactive updates
     */
    fun getUserIdFlow(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    /**
     * Get user email as Flow for reactive updates
     */
    fun getUserEmailFlow(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    /**
     * Request password reset email
     */
    suspend fun requestPasswordReset(email: String): Result<Unit> = try {
        Logger.d(TAG, "Requesting password reset for $email")
        auth.resetPasswordForEmail(email)
        Logger.d(TAG, "Password reset email sent")
        Result.success(Unit)
    } catch (e: Exception) {
        Logger.e(TAG, "Password reset error: ${e.message}", e)
        Result.failure(e)
    }
}

/**
 * Extension to get first value from Flow
 */
private suspend fun <T> Flow<T>.firstValue(): T? {
    var result: T? = null
    this.collect { value ->
        result = value
    }
    return result
}
