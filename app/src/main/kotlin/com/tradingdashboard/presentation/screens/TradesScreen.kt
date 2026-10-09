package com.tradingdashboard.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.presentation.theme.GreenWin
import com.tradingdashboard.presentation.theme.PrimaryGreen
import com.tradingdashboard.presentation.theme.RedLoss
import com.tradingdashboard.presentation.theme.TextMuted
import com.tradingdashboard.presentation.viewmodel.TradeFilter
import com.tradingdashboard.presentation.viewmodel.TradeSort
import com.tradingdashboard.presentation.viewmodel.TradesViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TradesScreen(
    viewModel: TradesViewModel = hiltViewModel(),
    onTradeClick: (String) -> Unit = {},
    onEditClick: (Trade) -> Unit = {},
    onDeleteClick: (String) -> Unit = {}
) {
    val filteredTrades by viewModel.filteredTrades.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val openCount by viewModel.openCount.collectAsState()
    val closedCount by viewModel.closedCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }
    var expandedSortMenu by remember { mutableStateOf(false) }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refresh() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                Text(
                    text = "Your Trades",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(
                        label = "Total",
                        value = totalCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        label = "Open",
                        value = openCount.toString(),
                        color = PrimaryGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        label = "Closed",
                        value = closedCount.toString(),
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Search Bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Filter Chips
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { viewModel.setFilter(it) }
                )

                // Sort Menu
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { expandedSortMenu = true },
                        modifier = Modifier.align(Alignment.CenterEnd),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text("Sort: ${selectedSort.name}", fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = expandedSortMenu,
                        onDismissRequest = { expandedSortMenu = false },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        TradeSort.values().forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.name) },
                                onClick = {
                                    viewModel.setSort(sort)
                                    expandedSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)

            // Error Display
            if (error != null) {
                ErrorBanner(
                    message = error ?: "",
                    onDismiss = { viewModel.dismissError() }
                )
            }

            // Trades List
            if (filteredTrades.isEmpty()) {
                EmptyStateDisplay(
                    message = if (searchQuery.isNotEmpty())
                        "No trades found for \"$searchQuery\""
                    else
                        "No trades yet. Start trading!",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(filteredTrades) { trade ->
                        TradeCard(
                            trade = trade,
                            onCardClick = { onTradeClick(trade.id) },
                            onEditClick = { onEditClick(trade) },
                            onDeleteClick = { showDeleteConfirm = trade.id }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Trade?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm?.let { tradeId ->
                            viewModel.deleteTrade(tradeId)
                            onDeleteClick(tradeId)
                        }
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = RedLoss
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    color: Color = TextMuted,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.height(48.dp),
        placeholder = { Text("Search by symbol...", color = TextMuted) },
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = "Search",
                tint = TextMuted
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = PrimaryGreen,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedTextColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun FilterChipsRow(
    selectedFilter: TradeFilter,
    onFilterSelected: (TradeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TradeFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        filter.name,
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

@Composable
fun TradeCard(
    trade: Trade,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(trade.openTime))

    val pnlColor = if ((trade.pnlAmount ?: 0.0) >= 0) GreenWin else RedLoss
    val statusColor = if (trade.status == "OPEN") PrimaryGreen else TextMuted
    val statusBg = if (trade.status == "OPEN")
        PrimaryGreen.copy(alpha = 0.1f)
    else
        TextMuted.copy(alpha = 0.1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Symbol and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trade.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = trade.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Position and Leverage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TradeDetailChip(
                    label = "Position",
                    value = trade.position,
                    modifier = Modifier.weight(1f)
                )
                TradeDetailChip(
                    label = "Leverage",
                    value = "${trade.leverage}x",
                    modifier = Modifier.weight(1f)
                )
                TradeDetailChip(
                    label = "Margin",
                    value = "$${String.format("%.2f", trade.margin)}",
                    modifier = Modifier.weight(1f)
                )
            }

            // Entry and Exit Prices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Entry",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$${String.format("%.4f", trade.entryPrice)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Exit",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (trade.exitPrice != null)
                            "$${String.format("%.4f", trade.exitPrice)}"
                        else
                            "—",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // P&L Display
            if (trade.pnlAmount != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            pnlColor.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "P&L",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "$${String.format("%.2f", trade.pnlAmount)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = pnlColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${String.format("%.1f", trade.pnlPercentage ?: 0.0)}%",
                        style = MaterialTheme.typography.titleSmall,
                        color = pnlColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Edit, "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }
                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedLoss.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Delete, "Delete", modifier = Modifier.size(16.dp), tint = RedLoss)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp, color = RedLoss)
                }
            }
        }
    }
}

@Composable
fun TradeDetailChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.background,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = RedLoss.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = RedLoss,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Text("×", fontSize = 20.sp, color = RedLoss)
            }
        }
    }
}

@Composable
fun EmptyStateDisplay(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📊",
            fontSize = 48.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
