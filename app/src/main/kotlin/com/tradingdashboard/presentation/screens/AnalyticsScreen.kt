package com.tradingdashboard.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tradingdashboard.presentation.theme.GreenWin
import com.tradingdashboard.presentation.theme.PrimaryGreen
import com.tradingdashboard.presentation.theme.RedLoss
import com.tradingdashboard.presentation.theme.TextMuted
import com.tradingdashboard.presentation.viewmodel.AnalyticsViewModel
import com.tradingdashboard.presentation.viewmodel.TimePeriod

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val analytics by viewModel.analyticsData.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val symbolBreakdown by viewModel.symbolBreakdown.collectAsState()

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your trading performance & statistics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }

            // Time Period Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimePeriod.values().forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { viewModel.setPeriod(period) },
                            label = {
                                Text(
                                    period.name.replace("_", " "),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.W500
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (analytics != null) {
                val data = analytics!!

                // Key Metrics Row 1
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Win Rate",
                            value = "${String.format("%.1f", data.winRate)}%",
                            color = PrimaryGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Loss Rate",
                            value = "${String.format("%.1f", data.lossRate)}%",
                            color = RedLoss,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Key Metrics Row 1.5 - Total Profit & Loss
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Total Profit",
                            value = "$${String.format("%.2f", data.totalProfit)}",
                            color = GreenWin,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Total Loss",
                            value = "-$${String.format("%.2f", data.totalLoss)}",
                            color = RedLoss,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Key Metrics Row 2
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Profit %",
                            value = "${String.format("%.1f", data.profitPercent)}%",
                            color = GreenWin,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Loss %",
                            value = "${String.format("%.1f", data.lossPercent)}%",
                            color = RedLoss,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Key Metrics Row 3
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Total P&L",
                            value = "$${String.format("%.2f", data.totalPnL)}",
                            color = if (data.totalPnL >= 0) GreenWin else RedLoss,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Profit Factor",
                            value = String.format("%.2f", data.profitFactor),
                            color = if (data.profitFactor > 1.0) GreenWin else TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Win/Loss Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Win/Loss Distribution",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            // Pie Chart Visualization (Simple)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left side - chart representation
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(40.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${data.winCount + data.lossCount}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )
                                        Text(
                                            text = "trades",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Right side - legend
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(GreenWin, RoundedCornerShape(2.dp))
                                        )
                                        Column {
                                            Text(
                                                text = "Wins",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = "${data.winCount} trades",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(RedLoss, RoundedCornerShape(2.dp))
                                        )
                                        Column {
                                            Text(
                                                text = "Losses",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = "${data.lossCount} trades",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Max Win/Loss
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WinLossCard(
                            label = "Max Win",
                            value = "$${String.format("%.2f", data.maxWin)}",
                            symbol = data.largestWinner,
                            icon = Icons.Filled.TrendingUp,
                            color = GreenWin,
                            modifier = Modifier.weight(1f)
                        )
                        WinLossCard(
                            label = "Max Loss",
                            value = "$${String.format("%.2f", data.maxLoss)}",
                            symbol = data.largestLoser,
                            icon = Icons.Filled.TrendingDown,
                            color = RedLoss,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Average Win/Loss
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Avg Win",
                            value = "$${String.format("%.2f", data.avgWin)}",
                            color = GreenWin,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Avg Loss",
                            value = "$${String.format("%.2f", data.avgLoss)}",
                            color = RedLoss,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Symbol Breakdown
                if (symbolBreakdown.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Most Traded Symbols",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                symbolBreakdown.take(5).forEach { (symbol, count) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.background,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = symbol,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "$count trades",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrimaryGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No analytics data available",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    color: Color = TextMuted,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WinLossCard(
    label: String,
    value: String,
    symbol: String,
    icon: androidx.compose.material.icons.Icons,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = symbol,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
