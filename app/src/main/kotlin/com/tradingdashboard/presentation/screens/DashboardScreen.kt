package com.tradingdashboard.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.data.repository.TradeStats
import com.tradingdashboard.presentation.theme.PrimaryGreen
import com.tradingdashboard.presentation.theme.TextMuted
import com.tradingdashboard.presentation.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onAddTradeClick: () -> Unit = {},
    onTradeClick: (String) -> Unit = {}
) {
    val trades by viewModel.trades.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    
    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val today = dateFormat.format(Date())
    
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Header
            item {
                Text(
                    text = today.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
            }
            
            // Greeting
            item {
                Column {
                    Text(
                        text = "Good morning, trader",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                    Text(
                        text = "Your database-backed trading journal at a glance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
            
            // Add Trade Button
            item {
                Button(
                    onClick = onAddTradeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Trade",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Trade", fontSize = 16.sp, fontWeight = FontWeight.W600)
                }
            }
            
            // Metrics Grid
            stats?.let { stat ->
                // Row 1: Total Trades & Open Positions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Total trades",
                            value = stat.totalTrades.toString(),
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Open positions",
                            value = stat.openPositions.toString(),
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Row 2: Closed Trades & Total Investment
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Closed trades",
                            value = stat.closedTrades.toString(),
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Total investment",
                            value = "$${String.format("%.2f", stat.totalInvestment)}",
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Row 3: Realized P&L & Unrealized P&L
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Realized P&L",
                            value = "$${String.format("%.2f", stat.realizedPnl)}",
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Unrealized P&L",
                            value = "—",
                            subtitle = "Connect a verified market price feed",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Row 4: Win Rate & Loss Rate
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Win rate",
                            value = "${String.format("%.1f", stat.winRate)}%",
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Loss rate",
                            value = "${String.format("%.1f", stat.lossRate)}%",
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Row 5: Total Profit & Total Loss
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Total profit",
                            value = "$${String.format("%.2f", stat.totalProfit)}",
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Total loss",
                            value = "$${String.format("%.2f", stat.totalLoss)}",
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Row 6: Profit % & Loss %
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Profit %",
                            value = "${String.format("%.1f", stat.profitPercent)}%",
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Loss %",
                            value = "${String.format("%.1f", stat.lossPercent)}%",
                            subtitle = "From saved trade records",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Trades List
            item {
                Text(
                    text = "Recent Trades",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W600
                )
            }
            
            items(trades, key = { it.id }) { trade ->
                TradeItem(
                    trade = trade,
                    onClick = { onTradeClick(trade.id) }
                )
            }
            
            // Error Message
            if (error != null) {
                item {
                    Text(
                        text = "Error: ${error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TradeItem(
    trade: Trade,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = trade.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W600
                )
                Text(
                    text = "${trade.position} • ${trade.leverage}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", trade.pnlAmount ?: 0.0)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W600,
                    color = if ((trade.pnlAmount ?: 0.0) >= 0) PrimaryGreen else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "${String.format("%.2f", trade.pnlPercentage ?: 0.0)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if ((trade.pnlPercentage ?: 0.0) >= 0) PrimaryGreen else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun clickable(action: () -> Unit): Modifier = Modifier
