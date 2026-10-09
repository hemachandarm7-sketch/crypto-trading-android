package com.tradingdashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.data.repository.TradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TradeFilter {
    ALL, OPEN, CLOSED
}

enum class TradeSort {
    DATE_DESC, DATE_ASC, PNL_DESC, PNL_ASC, SYMBOL
}

@HiltViewModel
class TradesViewModel @Inject constructor(
    private val repository: TradeRepository
) : ViewModel() {

    // State flows
    private val _trades = MutableStateFlow<List<Trade>>(emptyList())
    val trades: StateFlow<List<Trade>> = _trades.asStateFlow()

    private val _filteredTrades = MutableStateFlow<List<Trade>>(emptyList())
    val filteredTrades: StateFlow<List<Trade>> = _filteredTrades.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TradeFilter.ALL)
    val selectedFilter: StateFlow<TradeFilter> = _selectedFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(TradeSort.DATE_DESC)
    val selectedSort: StateFlow<TradeSort> = _selectedSort.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    private val _openCount = MutableStateFlow(0)
    val openCount: StateFlow<Int> = _openCount.asStateFlow()

    private val _closedCount = MutableStateFlow(0)
    val closedCount: StateFlow<Int> = _closedCount.asStateFlow()

    init {
        loadTrades()
        setupFilterAndSort()
    }

    private fun loadTrades() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Get trades from repository
                // For now using hardcoded data, will integrate with Supabase later
                val tradesList = repository.getUserTrades("user_1")
                _trades.value = tradesList

                // Update counts
                _totalCount.value = tradesList.size
                _openCount.value = tradesList.count { it.status == "OPEN" }
                _closedCount.value = tradesList.count { it.status == "CLOSED" }

                applyFilterAndSort(tradesList)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading trades"
                _isLoading.value = false
            }
        }
    }

    private fun setupFilterAndSort() {
        viewModelScope.launch {
            combine(
                _trades,
                _selectedFilter,
                _selectedSort,
                _searchQuery
            ) { trades, filter, sort, query ->
                var filtered = trades

                // Apply filter
                filtered = when (filter) {
                    TradeFilter.ALL -> filtered
                    TradeFilter.OPEN -> filtered.filter { it.status == "OPEN" }
                    TradeFilter.CLOSED -> filtered.filter { it.status == "CLOSED" }
                }

                // Apply search
                if (query.isNotEmpty()) {
                    filtered = filtered.filter {
                        it.symbol.contains(query, ignoreCase = true)
                    }
                }

                // Apply sort
                filtered = when (sort) {
                    TradeSort.DATE_DESC -> filtered.sortedByDescending { it.openTime }
                    TradeSort.DATE_ASC -> filtered.sortedBy { it.openTime }
                    TradeSort.PNL_DESC -> filtered.sortedByDescending { it.pnlAmount ?: 0.0 }
                    TradeSort.PNL_ASC -> filtered.sortedBy { it.pnlAmount ?: 0.0 }
                    TradeSort.SYMBOL -> filtered.sortedBy { it.symbol }
                }

                filtered
            }.collect { filtered ->
                _filteredTrades.value = filtered
            }
        }
    }

    private fun applyFilterAndSort(tradesList: List<Trade>) {
        var filtered = tradesList

        // Apply filter
        filtered = when (_selectedFilter.value) {
            TradeFilter.ALL -> filtered
            TradeFilter.OPEN -> filtered.filter { it.status == "OPEN" }
            TradeFilter.CLOSED -> filtered.filter { it.status == "CLOSED" }
        }

        // Apply search
        if (_searchQuery.value.isNotEmpty()) {
            filtered = filtered.filter {
                it.symbol.contains(_searchQuery.value, ignoreCase = true)
            }
        }

        // Apply sort
        filtered = when (_selectedSort.value) {
            TradeSort.DATE_DESC -> filtered.sortedByDescending { it.openTime }
            TradeSort.DATE_ASC -> filtered.sortedBy { it.openTime }
            TradeSort.PNL_DESC -> filtered.sortedByDescending { it.pnlAmount ?: 0.0 }
            TradeSort.PNL_ASC -> filtered.sortedBy { it.pnlAmount ?: 0.0 }
            TradeSort.SYMBOL -> filtered.sortedBy { it.symbol }
        }

        _filteredTrades.value = filtered
    }

    fun setFilter(filter: TradeFilter) {
        _selectedFilter.value = filter
    }

    fun setSort(sort: TradeSort) {
        _selectedSort.value = sort
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        loadTrades()
    }

    fun deleteTrade(tradeId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteTrade(tradeId)
                loadTrades()
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting trade"
                _isLoading.value = false
            }
        }
    }

    fun dismissError() {
        _error.value = null
    }
}
