package com.tradingdashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingdashboard.data.repository.TradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimePeriod {
    WEEK, MONTH, ALL_TIME
}

data class AnalyticsData(
    val totalTrades: Int = 0,
    val winRate: Double = 0.0,
    val lossRate: Double = 0.0,
    val totalPnL: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalLoss: Double = 0.0,
    val profitPercent: Double = 0.0,
    val lossPercent: Double = 0.0,
    val avgPnL: Double = 0.0,
    val winCount: Int = 0,
    val lossCount: Int = 0,
    val maxWin: Double = 0.0,
    val maxLoss: Double = 0.0,
    val avgWin: Double = 0.0,
    val avgLoss: Double = 0.0,
    val profitFactor: Double = 0.0,
    val largestWinner: String = "",
    val largestLoser: String = ""
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: TradeRepository
) : ViewModel() {

    private val _analyticsData = MutableStateFlow<AnalyticsData?>(null)
    val analyticsData: StateFlow<AnalyticsData?> = _analyticsData.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(TimePeriod.ALL_TIME)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _chartData = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val chartData: StateFlow<List<Pair<String, Double>>> = _chartData.asStateFlow()

    private val _symbolBreakdown = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val symbolBreakdown: StateFlow<List<Pair<String, Int>>> = _symbolBreakdown.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Get all trades
                val trades = repository.getUserTrades("user_1")

                // Calculate analytics
                val closedTrades = trades.filter { it.status == "CLOSED" }

                if (closedTrades.isEmpty()) {
                    _analyticsData.value = AnalyticsData(totalTrades = trades.size)
                    _isLoading.value = false
                    return@launch
                }

                val winningTrades = closedTrades.filter { (it.pnlAmount ?: 0.0) > 0 }
                val losingTrades = closedTrades.filter { (it.pnlAmount ?: 0.0) < 0 }

                val totalPnL = closedTrades.sumOf { it.pnlAmount ?: 0.0 }
                val winRate = if (closedTrades.isNotEmpty())
                    (winningTrades.size * 100.0) / closedTrades.size
                else 0.0

                val maxWin = winningTrades.maxOfOrNull { it.pnlAmount ?: 0.0 } ?: 0.0
                val maxLoss = losingTrades.minOfOrNull { it.pnlAmount ?: 0.0 } ?: 0.0
                val avgWin = if (winningTrades.isNotEmpty())
                    winningTrades.sumOf { it.pnlAmount ?: 0.0 } / winningTrades.size
                else 0.0
                val avgLoss = if (losingTrades.isNotEmpty())
                    losingTrades.sumOf { it.pnlAmount ?: 0.0 } / losingTrades.size
                else 0.0

                val profitFactor = if (avgLoss != 0.0)
                    avgWin / kotlin.math.abs(avgLoss)
                else 0.0

                val largestWinner = winningTrades.maxByOrNull { it.pnlAmount ?: 0.0 }?.symbol ?: "—"
                val largestLoser = losingTrades.minByOrNull { it.pnlAmount ?: 0.0 }?.symbol ?: "—"

                // Calculate total profit and loss amounts
                val totalProfit = winningTrades.sumOf { it.pnlAmount ?: 0.0 }
                val totalLoss = kotlin.math.abs(losingTrades.sumOf { it.pnlAmount ?: 0.0 })
                
                // Calculate profit and loss percentages (of total P&L composition)
                val totalPnLAmount = totalProfit + totalLoss
                val profitPercent = if (totalPnLAmount > 0.0)
                    (totalProfit / totalPnLAmount) * 100.0
                else 0.0
                val lossPercent = if (totalPnLAmount > 0.0)
                    (totalLoss / totalPnLAmount) * 100.0
                else 0.0
                
                // Calculate loss rate percentage (by trade count)
                val lossRate = if (closedTrades.isNotEmpty())
                    (losingTrades.size * 100.0) / closedTrades.size
                else 0.0

                val analytics = AnalyticsData(
                    totalTrades = trades.size,
                    winRate = winRate,
                    lossRate = lossRate,
                    totalPnL = totalPnL,
                    totalProfit = totalProfit,
                    totalLoss = totalLoss,
                    profitPercent = profitPercent,
                    lossPercent = lossPercent,
                    avgPnL = if (closedTrades.isNotEmpty()) totalPnL / closedTrades.size else 0.0,
                    winCount = winningTrades.size,
                    lossCount = losingTrades.size,
                    maxWin = maxWin,
                    maxLoss = maxLoss,
                    avgWin = avgWin,
                    avgLoss = avgLoss,
                    profitFactor = profitFactor,
                    largestWinner = largestWinner,
                    largestLoser = largestLoser
                )

                _analyticsData.value = analytics

                // Generate chart data (cumulative P&L over time)
                generateChartData(closedTrades)

                // Generate symbol breakdown
                generateSymbolBreakdown(trades)

                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun generateChartData(trades: List<com.tradingdashboard.data.model.Trade>) {
        val sortedTrades = trades.sortedBy { it.closeTime ?: it.openTime }
        var cumulativePnL = 0.0
        val chartPoints = mutableListOf<Pair<String, Double>>()

        sortedTrades.forEachIndexed { index, trade ->
            cumulativePnL += (trade.pnlAmount ?: 0.0)
            chartPoints.add(Pair("T${index + 1}", cumulativePnL))
        }

        _chartData.value = chartPoints
    }

    private fun generateSymbolBreakdown(trades: List<com.tradingdashboard.data.model.Trade>) {
        val breakdown = trades
            .groupingBy { it.symbol }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }

        _symbolBreakdown.value = breakdown
    }

    fun setPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
        loadAnalytics() // Reload with new period filter
    }

    fun refresh() {
        loadAnalytics()
    }
}
