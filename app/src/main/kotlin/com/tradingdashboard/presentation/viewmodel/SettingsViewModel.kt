package com.tradingdashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingdashboard.data.model.Settings
import com.tradingdashboard.data.repository.TradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Currency {
    USD, EUR, GBP, INR, JPY
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: TradeRepository
) : ViewModel() {

    private val _settings = MutableStateFlow<Settings?>(null)
    val settings: StateFlow<Settings?> = _settings.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currency = MutableStateFlow(Currency.USD)
    val currency: StateFlow<Currency> = _currency.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(true)
    val autoSyncEnabled: StateFlow<Boolean> = _autoSyncEnabled.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow<Boolean?>(null)
    val saveSuccess: StateFlow<Boolean?> = _saveSuccess.asStateFlow()

    private val _appVersion = MutableStateFlow("1.0.0")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    private val _buildNumber = MutableStateFlow("001")
    val buildNumber: StateFlow<String> = _buildNumber.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // TODO: Load from repository
                // For now, using defaults
                val defaultSettings = Settings(
                    userId = "user_1",
                    currency = "USD",
                    theme = "DARK",
                    biometricEnabled = false,
                    autoSyncEnabled = true
                )

                _settings.value = defaultSettings
                _isDarkMode.value = defaultSettings.theme == "DARK"
                _currency.value = try {
                    Currency.valueOf(defaultSettings.currency)
                } catch (e: Exception) {
                    Currency.USD
                }
                _biometricEnabled.value = defaultSettings.biometricEnabled
                _autoSyncEnabled.value = defaultSettings.autoSyncEnabled

                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun setCurrency(currency: Currency) {
        _currency.value = currency
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _biometricEnabled.value = enabled
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        _autoSyncEnabled.value = enabled
    }

    fun saveSettings() {
        viewModelScope.launch {
            try {
                _isSaving.value = true

                val updatedSettings = Settings(
                    userId = "user_1",
                    currency = _currency.value.name,
                    theme = if (_isDarkMode.value) "DARK" else "LIGHT",
                    biometricEnabled = _biometricEnabled.value,
                    autoSyncEnabled = _autoSyncEnabled.value
                )

                // TODO: Save to repository
                // repository.saveSettings(updatedSettings)

                _settings.value = updatedSettings
                _saveSuccess.value = true
                _isSaving.value = false

                // Reset success after 2 seconds
                kotlinx.coroutines.delay(2000)
                _saveSuccess.value = null

            } catch (e: Exception) {
                _saveSuccess.value = false
                _isSaving.value = false
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                // TODO: Implement actual sync
                // repository.syncWithServer("user_1")
                kotlinx.coroutines.delay(1500)
                _saveSuccess.value = true
                _isSaving.value = false

                // Reset success after 2 seconds
                kotlinx.coroutines.delay(2000)
                _saveSuccess.value = null

            } catch (e: Exception) {
                _saveSuccess.value = false
                _isSaving.value = false
            }
        }
    }

    fun resetToDefaults() {
        _isDarkMode.value = true
        _currency.value = Currency.USD
        _biometricEnabled.value = false
        _autoSyncEnabled.value = true
    }
}
