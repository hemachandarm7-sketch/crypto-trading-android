package com.tradingdashboard.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.data.repository.TradeRepository
import com.tradingdashboard.util.Constants
import com.tradingdashboard.util.Logger
import com.tradingdashboard.util.Formatters
import javax.inject.Inject
import kotlin.math.absoluteValue

/**
 * ViewModel for Trade Detail Screen
 * Handles fetching and managing a single trade's data
 */
@HiltViewModel
class TradeDetailViewModel @Inject constructor(
    private val tradeRepository: TradeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get trade ID from navigation arguments
    private val tradeId: String = savedStateHandle.get<String>("tradeId") ?: ""

    // ============ State Flows ============

    private val _trade = MutableStateFlow<Trade?>(null)
    val trade: StateFlow<Trade?> = _trade.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showEditSheet = MutableStateFlow(false)
    val showEditSheet: StateFlow<Boolean> = _showEditSheet.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _editedTrade = MutableStateFlow<Trade?>(null)
    val editedTrade: StateFlow<Trade?> = _editedTrade.asStateFlow()

    // Statistics derived from current trade
    private val _roi = MutableStateFlow(0.0)
    val roi: StateFlow<Double> = _roi.asStateFlow()

    private val _roe = MutableStateFlow(0.0)
    val roe: StateFlow<Double> = _roe.asStateFlow()

    private val _pnlPercent = MutableStateFlow(0.0)
    val pnlPercent: StateFlow<Double> = _pnlPercent.asStateFlow()

    private val _duration = MutableStateFlow("")
    val duration: StateFlow<String> = _duration.asStateFlow()

    init {
        Logger.d("TradeDetailViewModel initialized with tradeId: $tradeId")
        loadTradeDetails()
    }

    // ============ Public Functions ============

    /**
     * Load trade details from repository
     */
    private fun loadTradeDetails() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                Logger.d("Loading trade details for ID: $tradeId")

                // TODO: Replace with actual repository call when API is implemented
                // For now, create sample trade
                val sampleTrade = Trade(
                    id = tradeId,
                    userId = Constants.User.DEFAULT_USER_ID,
                    symbol = "BTC/USDT",
                    position = Constants.Trade.POSITION_LONG,
                    leverage = 10.0,
                    entryPrice = 45250.50,
                    exitPrice = 46800.00,
                    quantity = 0.22,
                    margin = 100.0,
                    pnlAmount = 342.10,
                    status = Constants.Trade.STATUS_CLOSED,
                    openTime = System.currentTimeMillis() - (48 * 60 * 60 * 1000),
                    closeTime = System.currentTimeMillis(),
                    notes = "Strong breakout on daily chart. Closed at resistance."
                )

                _trade.value = sampleTrade
                calculateStatistics(sampleTrade)

            } catch (e: Exception) {
                Logger.e("Error loading trade details", tag = "TradeDetailVM", throwable = e)
                _error.value = e.message ?: Constants.ErrorMessages.DATABASE_ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Calculate derived statistics for the trade
     */
    private fun calculateStatistics(trade: Trade) {
        try {
            // Calculate ROI (Return on Investment)
            val roi = if (trade.entryPrice > 0) {
                ((trade.exitPrice ?: trade.entryPrice) - trade.entryPrice) / trade.entryPrice * 100
            } else {
                0.0
            }
            _roi.value = roi

            // Calculate ROE (Return on Equity)
            val roe = if (trade.margin > 0) {
                (trade.pnlAmount ?: 0.0) / trade.margin * 100
            } else {
                0.0
            }
            _roe.value = roe

            // Calculate P&L percentage
            val pnlPercent = if (trade.margin > 0) {
                ((trade.pnlAmount ?: 0.0) / trade.margin) * 100
            } else {
                0.0
            }
            _pnlPercent.value = pnlPercent

            // Calculate duration
            val closeTime = trade.closeTime ?: System.currentTimeMillis()
            val duration = closeTime - trade.openTime
            _duration.value = Formatters.formatDuration(duration)

            Logger.d(
                "Trade statistics calculated - ROI: ${String.format("%.2f", roi)}%, " +
                "ROE: ${String.format("%.2f", roe)}%, " +
                "P&L%: ${String.format("%.2f", pnlPercent)}%"
            )
        } catch (e: Exception) {
            Logger.e("Error calculating statistics", tag = "TradeDetailVM", throwable = e)
        }
    }

    /**
     * Show delete confirmation dialog
     */
    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    /**
     * Hide delete confirmation dialog
     */
    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }

    /**
     * Delete the trade
     */
    fun deleteTrade() {
        val currentTrade = _trade.value ?: return

        viewModelScope.launch {
            try {
                _isDeleting.value = true
                Logger.d("Deleting trade: ${currentTrade.id}")

                // TODO: Replace with actual repository call
                tradeRepository.deleteTrade(currentTrade.id)

                Logger.d("Trade deleted successfully")
                _showDeleteDialog.value = false

                // TODO: Navigate back after deletion
                // Navigation should be handled by screen using this ViewModel

            } catch (e: Exception) {
                Logger.e("Error deleting trade", tag = "TradeDetailVM", throwable = e)
                _error.value = Constants.ErrorMessages.TRADE_NOT_FOUND
            } finally {
                _isDeleting.value = false
            }
        }
    }

    /**
     * Show edit bottom sheet
     */
    fun showEditSheet() {
        _trade.value?.let {
            _editedTrade.value = it.copy()
            _showEditSheet.value = true
        }
    }

    /**
     * Hide edit bottom sheet
     */
    fun hideEditSheet() {
        _showEditSheet.value = false
        _editedTrade.value = null
    }

    /**
     * Update trade field
     */
    fun updateTradeField(field: String, value: Any) {
        _editedTrade.value = _editedTrade.value?.copy(
            symbol = if (field == "symbol") value as? String ?: _editedTrade.value?.symbol ?: "" else _editedTrade.value?.symbol ?: "",
            entryPrice = if (field == "entryPrice") value as? Double ?: _editedTrade.value?.entryPrice ?: 0.0 else _editedTrade.value?.entryPrice ?: 0.0,
            exitPrice = if (field == "exitPrice") value as? Double else _editedTrade.value?.exitPrice,
            quantity = if (field == "quantity") value as? Double ?: _editedTrade.value?.quantity ?: 0.0 else _editedTrade.value?.quantity ?: 0.0,
            pnlAmount = if (field == "pnlAmount") value as? Double else _editedTrade.value?.pnlAmount,
            notes = if (field == "notes") value as? String ?: _editedTrade.value?.notes ?: "" else _editedTrade.value?.notes ?: ""
        )
    }

    /**
     * Save edited trade
     */
    fun saveEditedTrade() {
        val edittedTrade = _editedTrade.value ?: return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                Logger.d("Saving edited trade: ${edittedTrade.id}")

                // TODO: Replace with actual repository call
                tradeRepository.updateTrade(edittedTrade)

                _trade.value = edittedTrade
                calculateStatistics(edittedTrade)
                _showEditSheet.value = false
                _editedTrade.value = null

                Logger.d("Trade updated successfully")

            } catch (e: Exception) {
                Logger.e("Error updating trade", tag = "TradeDetailVM", throwable = e)
                _error.value = Constants.ErrorMessages.SYNC_FAILED
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Retry loading trade
     */
    fun retry() {
        clearError()
        loadTradeDetails()
    }
}
