package com.tradingdashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.data.repository.TradeRepository
import com.tradingdashboard.data.repository.TradeStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val tradeRepository: TradeRepository
) : ViewModel() {
    
    private val _trades = MutableStateFlow<List<Trade>>(emptyList())
    val trades: StateFlow<List<Trade>> = _trades.asStateFlow()
    
    private val _stats = MutableStateFlow<TradeStats?>(null)
    val stats: StateFlow<TradeStats?> = _stats.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _userId = MutableStateFlow("")
    
    init {
        // Get user ID from your auth system
        initializeUser("user-123") // Replace with actual user ID
    }
    
    fun initializeUser(userId: String) {
        _userId.value = userId
        loadDashboard()
    }
    
    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Load trades
                tradeRepository.getUserTradesFlow(_userId.value)
                    .collectLatest { trades ->
                        _trades.value = trades
                    }
                
                // Load stats
                val stats = tradeRepository.getTradeStats(_userId.value)
                _stats.value = stats
                
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        loadDashboard()
    }
    
    fun syncWithServer() {
        viewModelScope.launch {
            try {
                tradeRepository.syncWithServer(_userId.value).onSuccess {
                    loadDashboard()
                }.onFailure { e ->
                    _error.value = e.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}

// UI State
data class DashboardState(
    val trades: List<Trade> = emptyList(),
    val stats: TradeStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
