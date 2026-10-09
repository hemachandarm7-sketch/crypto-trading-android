package com.tradingdashboard.data.repository

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.tradingdashboard.util.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Manager for biometric authentication (fingerprint/face recognition)
 */
class BiometricAuthManager(
    private val context: Context
) {
    companion object {
        private const val TAG = "BiometricAuthManager"
    }

    private val biometricManager = BiometricManager.from(context)

    /**
     * Check if device supports biometric authentication
     */
    fun isBiometricAvailable(): Boolean {
        return try {
            val canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking biometric availability: ${e.message}", e)
            false
        }
    }

    /**
     * Get biometric authenticator type available on device
     */
    fun getBiometricType(): BiometricType {
        return try {
            when {
                canAuthenticateWithStrongBiometric() -> BiometricType.STRONG
                canAuthenticateWithWeakBiometric() -> BiometricType.WEAK
                else -> BiometricType.NONE
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting biometric type: ${e.message}", e)
            BiometricType.NONE
        }
    }

    /**
     * Get user-friendly biometric type name
     */
    fun getBiometricTypeName(): String {
        return when (getBiometricType()) {
            BiometricType.STRONG -> "Fingerprint/Face"
            BiometricType.WEAK -> "Biometric"
            BiometricType.NONE -> "Not available"
        }
    }

    /**
     * Authenticate user with biometric
     * @param activity - FragmentActivity for biometric prompt
     * @param title - Title for biometric prompt
     * @param subtitle - Subtitle for biometric prompt
     * @param description - Description for biometric prompt
     * @return Result<Unit> - Success or failure
     */
    suspend fun authenticateWithBiometric(
        activity: FragmentActivity,
        title: String = "Unlock Trading Dashboard",
        subtitle: String = "Use your biometric to authenticate",
        description: String = "Place your finger on the sensor or look at the camera"
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            if (!isBiometricAvailable()) {
                Logger.w(TAG, "Biometric not available")
                continuation.resume(Result.failure(Exception("Biometric not available on this device")))
                return@suspendCancellableCoroutine
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText("Cancel")
                .build()

            val authCallback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Logger.d(TAG, "Biometric authentication succeeded")
                    continuation.resume(Result.success(Unit))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Logger.e(TAG, "Biometric authentication error: $errString")
                    continuation.resume(
                        Result.failure(
                            BiometricException(
                                errorCode,
                                errString.toString()
                            )
                        )
                    )
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Logger.w(TAG, "Biometric authentication failed")
                    continuation.resume(Result.failure(Exception("Biometric authentication failed")))
                }
            }

            val biometricPrompt = BiometricPrompt(
                activity,
                MainThreadExecutor(),
                authCallback
            )

            biometricPrompt.authenticate(promptInfo)

        } catch (e: Exception) {
            Logger.e(TAG, "Error during biometric authentication: ${e.message}", e)
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Check if strong biometric is available
     */
    private fun canAuthenticateWithStrongBiometric(): Boolean {
        return try {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            ) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if weak biometric is available
     */
    private fun canAuthenticateWithWeakBiometric(): Boolean {
        return try {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            ) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Biometric exception with error code
     */
    data class BiometricException(
        val errorCode: Int,
        override val message: String
    ) : Exception(message)

    /**
     * Biometric authenticator type
     */
    enum class BiometricType {
        STRONG,  // Fingerprint or Face (strong security)
        WEAK,    // Other biometric methods
        NONE     // No biometric available
    }
}

/**
 * Executor for running callbacks on main thread
 */
private class MainThreadExecutor : java.util.concurrent.Executor {
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    override fun execute(command: Runnable) {
        handler.post(command)
    }
}
