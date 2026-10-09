package com.tradingdashboard.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.data.repository.TradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExtractedTradeData(
    val symbol: String = "",
    val entryPrice: Double = 0.0,
    val exitPrice: Double? = null,
    val position: String = "LONG",
    val leverage: Double = 1.0,
    val quantity: Double = 0.0,
    val margin: Double = 0.0,
    val pnlAmount: Double? = null,
    val pnlPercentage: Double? = null,
    val rawText: String = ""
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val repository: TradeRepository
) : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _extractedData = MutableStateFlow<ExtractedTradeData?>(null)
    val extractedData: StateFlow<ExtractedTradeData?> = _extractedData.asStateFlow()

    private val _isOCRRunning = MutableStateFlow(false)
    val isOCRRunning: StateFlow<Boolean> = _isOCRRunning.asStateFlow()

    private val _ocrError = MutableStateFlow<String?>(null)
    val ocrError: StateFlow<String?> = _ocrError.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow<Boolean?>(null)
    val saveSuccess: StateFlow<Boolean?> = _saveSuccess.asStateFlow()

    fun setSelectedImage(uri: Uri) {
        _selectedImageUri.value = uri
        _extractedData.value = null
        _ocrError.value = null
    }

    fun clearSelectedImage() {
        _selectedImageUri.value = null
        _extractedData.value = null
        _ocrError.value = null
    }

    fun startOCRExtraction(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _isOCRRunning.value = true
                _ocrError.value = null

                // TODO: Implement actual OCR using Tesseract.js or MLKit
                // For now, we'll simulate OCR extraction with placeholder data
                simulateOCRExtraction()

                _isOCRRunning.value = false
            } catch (e: Exception) {
                _ocrError.value = e.message ?: "Error extracting trade data"
                _isOCRRunning.value = false
            }
        }
    }

    private fun simulateOCRExtraction() {
        // This will be replaced with actual OCR implementation
        val sampleData = ExtractedTradeData(
            symbol = "BTC/USDT",
            entryPrice = 45250.50,
            exitPrice = 46800.00,
            position = "LONG",
            leverage = 10.0,
            quantity = 0.1,
            margin = 452.50,
            pnlAmount = 155.00,
            pnlPercentage = 34.3,
            rawText = "Sample extracted text from screenshot"
        )
        _extractedData.value = sampleData
    }

    fun updateExtractedData(data: ExtractedTradeData) {
        _extractedData.value = data
    }

    fun updateExtractedField(field: String, value: String) {
        val current = _extractedData.value ?: return
        val updated = when (field) {
            "symbol" -> current.copy(symbol = value)
            "entryPrice" -> current.copy(entryPrice = value.toDoubleOrNull() ?: 0.0)
            "exitPrice" -> current.copy(exitPrice = value.toDoubleOrNull())
            "position" -> current.copy(position = value)
            "leverage" -> current.copy(leverage = value.toDoubleOrNull() ?: 1.0)
            "quantity" -> current.copy(quantity = value.toDoubleOrNull() ?: 0.0)
            "margin" -> current.copy(margin = value.toDoubleOrNull() ?: 0.0)
            else -> current
        }
        _extractedData.value = updated
    }

    fun saveTrade() {
        val data = _extractedData.value ?: return

        viewModelScope.launch {
            try {
                _isSaving.value = true

                // Create trade object from extracted data
                val trade = Trade(
                    id = System.currentTimeMillis().toString(),
                    userId = "user_1", // TODO: Get from auth
                    symbol = data.symbol,
                    position = data.position,
                    leverage = data.leverage,
                    quantity = data.quantity,
                    entryPrice = data.entryPrice,
                    exitPrice = data.exitPrice,
                    margin = data.margin,
                    pnlAmount = data.pnlAmount,
                    pnlPercentage = data.pnlPercentage,
                    status = if (data.exitPrice != null) "CLOSED" else "OPEN",
                    openTime = System.currentTimeMillis(),
                    closeTime = if (data.exitPrice != null) System.currentTimeMillis() else null
                )

                // Save to repository
                repository.addTrade(trade)

                _saveSuccess.value = true
                _isSaving.value = false

                // Clear after successful save
                clearSelectedImage()

            } catch (e: Exception) {
                _ocrError.value = e.message ?: "Error saving trade"
                _saveSuccess.value = false
                _isSaving.value = false
            }
        }
    }

    fun resetSaveStatus() {
        _saveSuccess.value = null
    }

    fun dismissError() {
        _ocrError.value = null
    }
}
