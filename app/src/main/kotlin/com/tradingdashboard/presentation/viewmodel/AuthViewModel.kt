package com.tradingdashboard.presentation.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingdashboard.data.repository.AuthRepository
import com.tradingdashboard.data.repository.BiometricAuthManager
import com.tradingdashboard.data.repository.BiometricPreferenceManager
import com.tradingdashboard.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication operations including biometric
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val biometricAuthManager: BiometricAuthManager,
    private val biometricPreferenceManager: BiometricPreferenceManager
) : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkAuthStatus()
    }

    /**
     * Update email field
     */
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        _errorMessage.value = null
    }

    /**
     * Update password field
     */
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        _errorMessage.value = null
    }

    /**
     * Update confirm password field
     */
    fun updateConfirmPassword(newPassword: String) {
        _confirmPassword.value = newPassword
        _errorMessage.value = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Attempt to sign in with email and password
     */
    fun signIn() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Validate inputs
                if (!validateInputs()) {
                    _isLoading.value = false
                    return@launch
                }

                // Call repository
                val result = authRepository.signIn(_email.value, _password.value)

                result.onSuccess { userId ->
                    Logger.d(TAG, "Sign in successful: $userId")
                    _authState.value = AuthState.Authenticated(userId, _email.value)
                    clearForm()
                }.onFailure { exception ->
                    Logger.e(TAG, "Sign in failed: ${exception.message}", exception)
                    _errorMessage.value = exception.message ?: "Sign in failed"
                    _authState.value = AuthState.Error(exception.message ?: "Sign in failed")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Unexpected error during signin: ${e.message}", e)
                _errorMessage.value = "An unexpected error occurred"
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Attempt to sign up with email and password
     */
    fun signUp() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Validate inputs
                if (!validateSignUpInputs()) {
                    _isLoading.value = false
                    return@launch
                }

                // Call repository
                val result = authRepository.signUp(_email.value, _password.value)

                result.onSuccess { userId ->
                    Logger.d(TAG, "Sign up successful: $userId")
                    _authState.value = AuthState.Authenticated(userId, _email.value)
                    clearForm()
                }.onFailure { exception ->
                    Logger.e(TAG, "Sign up failed: ${exception.message}", exception)
                    _errorMessage.value = exception.message ?: "Sign up failed"
                    _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Unexpected error during signup: ${e.message}", e)
                _errorMessage.value = "An unexpected error occurred"
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sign out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.signOut()
                result.onSuccess {
                    Logger.d(TAG, "Sign out successful")
                    _authState.value = AuthState.Unauthenticated
                    clearForm()
                }.onFailure { exception ->
                    Logger.e(TAG, "Sign out failed: ${exception.message}", exception)
                    _errorMessage.value = exception.message ?: "Sign out failed"
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Unexpected error during signout: ${e.message}", e)
                _errorMessage.value = "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Request password reset
     */
    fun requestPasswordReset() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                if (_email.value.isBlank()) {
                    _errorMessage.value = "Please enter your email address"
                    _isLoading.value = false
                    return@launch
                }

                val result = authRepository.requestPasswordReset(_email.value)
                result.onSuccess {
                    Logger.d(TAG, "Password reset email sent")
                    _errorMessage.value = null
                    _authState.value = AuthState.PasswordResetSent(_email.value)
                }.onFailure { exception ->
                    Logger.e(TAG, "Password reset failed: ${exception.message}", exception)
                    _errorMessage.value = exception.message ?: "Failed to send reset email"
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Unexpected error during password reset: ${e.message}", e)
                _errorMessage.value = "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Check current authentication status
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                if (authRepository.isUserAuthenticated()) {
                    val userId = authRepository.getCurrentUserId()
                    val email = authRepository.getCurrentUserEmail()
                    if (userId != null && email != null) {
                        Logger.d(TAG, "User already authenticated: $userId")
                        _authState.value = AuthState.Authenticated(userId, email)
                    }
                } else {
                    Logger.d(TAG, "User not authenticated")
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error checking auth status: ${e.message}", e)
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Attempt biometric authentication
     */
    fun attemptBiometricAuth(activity: FragmentActivity) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Check if biometric is available
                if (!biometricAuthManager.isBiometricAvailable()) {
                    Logger.d(TAG, "Biometric not available on device")
                    _errorMessage.value = "Biometric not available on this device"
                    _isLoading.value = false
                    return@launch
                }

                // Attempt biometric authentication
                val result = biometricAuthManager.authenticateWithBiometric(
                    activity,
                    title = "Unlock Trading Dashboard",
                    subtitle = "Use your biometric to authenticate",
                    description = "Place your finger on the sensor or look at the camera"
                )

                result.onSuccess {
                    Logger.d(TAG, "Biometric authentication successful")
                    // Biometric successful - proceed to dashboard
                    _authState.value = AuthState.BiometricSuccess
                }.onFailure { exception ->
                    Logger.e(TAG, "Biometric authentication failed: ${exception.message}", exception)
                    _errorMessage.value = when (exception) {
                        is BiometricAuthManager.BiometricException -> {
                            when {
                                exception.message.contains("user canceled") -> "Authentication cancelled"
                                exception.message.contains("negative button") -> "Authentication cancelled"
                                else -> exception.message
                            }
                        }
                        else -> exception.message ?: "Biometric authentication failed"
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Unexpected error during biometric auth: ${e.message}", e)
                _errorMessage.value = "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Check if biometric is available and enabled
     */
    fun isBiometricAvailable(): Boolean {
        return biometricAuthManager.isBiometricAvailable()
    }

    /**
     * Enable biometric for user
     */
    fun enableBiometric() {
        viewModelScope.launch {
            try {
                biometricPreferenceManager.enableBiometric()
                Logger.d(TAG, "Biometric enabled")
            } catch (e: Exception) {
                Logger.e(TAG, "Error enabling biometric: ${e.message}", e)
                _errorMessage.value = "Failed to enable biometric"
            }
        }
    }

    /**
     * Disable biometric for user
     */
    fun disableBiometric() {
        viewModelScope.launch {
            try {
                biometricPreferenceManager.disableBiometric()
                Logger.d(TAG, "Biometric disabled")
            } catch (e: Exception) {
                Logger.e(TAG, "Error disabling biometric: ${e.message}", e)
                _errorMessage.value = "Failed to disable biometric"
            }
        }
    }

    /**
     * Validate sign in inputs
     */
    private fun validateInputs(): Boolean {
        return when {
            _email.value.isBlank() -> {
                _errorMessage.value = "Please enter your email"
                false
            }
            !isValidEmail(_email.value) -> {
                _errorMessage.value = "Please enter a valid email"
                false
            }
            _password.value.isBlank() -> {
                _errorMessage.value = "Please enter your password"
                false
            }
            _password.value.length < 6 -> {
                _errorMessage.value = "Password must be at least 6 characters"
                false
            }
            else -> true
        }
    }

    /**
     * Validate sign up inputs
     */
    private fun validateSignUpInputs(): Boolean {
        return when {
            _email.value.isBlank() -> {
                _errorMessage.value = "Please enter your email"
                false
            }
            !isValidEmail(_email.value) -> {
                _errorMessage.value = "Please enter a valid email"
                false
            }
            _password.value.isBlank() -> {
                _errorMessage.value = "Please enter a password"
                false
            }
            _password.value.length < 6 -> {
                _errorMessage.value = "Password must be at least 6 characters"
                false
            }
            _confirmPassword.value != _password.value -> {
                _errorMessage.value = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    /**
     * Clear form fields
     */
    private fun clearForm() {
        _email.value = ""
        _password.value = ""
        _confirmPassword.value = ""
        _errorMessage.value = null
    }
}

/**
 * Authentication state
 */
sealed class AuthState {
    data object Initial : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val userId: String, val email: String) : AuthState()
    data object BiometricSuccess : AuthState()
    data class Error(val message: String) : AuthState()
    data class PasswordResetSent(val email: String) : AuthState()
}
