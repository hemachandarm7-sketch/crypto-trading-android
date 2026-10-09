package com.tradingdashboard.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.presentation.components.*
import com.tradingdashboard.presentation.theme.CardDarkBg
import com.tradingdashboard.presentation.theme.PrimaryGreen
import com.tradingdashboard.presentation.theme.RedLoss
import com.tradingdashboard.presentation.theme.TextMuted
import com.tradingdashboard.presentation.viewmodel.TradeDetailViewModel
import com.tradingdashboard.util.Constants
import com.tradingdashboard.util.Formatters

/**
 * Trade Detail Screen
 * Displays comprehensive information about a single trade
 */
@Composable
fun TradeDetailScreen(
    tradeId: String,
    onNavigateBack: () -> Unit,
    viewModel: TradeDetailViewModel = hiltViewModel()
) {
    val trade by viewModel.trade.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val roi by viewModel.roi.collectAsState()
    val roe by viewModel.roe.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showEditSheet by viewModel.showEditSheet.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Content
        if (isLoading) {
            LoadingIndicator()
        } else if (error != null) {
            ErrorState(
                message = error ?: Constants.ErrorMessages.UNKNOWN_ERROR,
                onRetry = { viewModel.retry() }
            )
        } else if (trade != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Header
                TradeDetailHeader(
                    trade = trade!!,
                    onNavigateBack = onNavigateBack,
                    onEdit = { viewModel.showEditSheet() },
                    onDelete = { viewModel.showDeleteDialog() }
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Key Metrics
                    KeyMetricsCard(
                        trade = trade!!,
                        roi = roi,
                        roe = roe,
                        duration = duration
                    )

                    // Position Details
                    PositionDetailsCard(trade = trade!!)

                    // Prices Card
                    PricesCard(trade = trade!!)

                    // P&L Breakdown
                    PnLBreakdownCard(trade = trade!!)

                    // Notes
                    if (!trade!!.notes.isNullOrEmpty()) {
                        NotesCard(trade = trade!!)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            EmptyState(
                title = "Trade Not Found",
                message = "The trade you're looking for could not be found.",
                action = {
                    PrimaryButton(
                        text = "Go Back",
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }

        // Delete Confirmation Dialog
        DeleteConfirmDialog(
            isVisible = showDeleteDialog,
            itemName = "trade",
            onConfirm = {
                viewModel.deleteTrade()
                onNavigateBack()
            },
            onCancel = { viewModel.hideDeleteDialog() }
        )

        // Edit Bottom Sheet
        EditTradeBottomSheet(
            isVisible = showEditSheet,
            trade = trade,
            viewModel = viewModel,
            onDismiss = { viewModel.hideEditSheet() }
        )

        // Loading Overlay
        FullScreenLoadingOverlay(isLoading && isDeleting)
    }
}

/**
 * Trade Detail Header with back button and actions
 */
@Composable
private fun TradeDetailHeader(
    trade: Trade,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = trade.symbol,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${trade.getDirectionBadge()} • ${Formatters.formatPrice(trade.entryPrice)}",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedLoss)
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = CardDarkBg
        )
    )
}

/**
 * Key Metrics Card showing ROI, ROE, duration
 */
@Composable
private fun KeyMetricsCard(
    trade: Trade,
    roi: Double,
    roe: Double,
    duration: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance Metrics",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricBox(
                    label = "ROI",
                    value = Formatters.formatPercentage(roi),
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "ROE",
                    value = Formatters.formatPercentage(roe),
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "Duration",
                    value = duration,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual metric box
 */
@Composable
private fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Position Details Card
 */
@Composable
private fun PositionDetailsCard(trade: Trade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Position Details",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            DetailRow(
                label = "Position",
                value = trade.getDirectionBadge(),
                valueColor = if (trade.position == Constants.Trade.POSITION_LONG) PrimaryGreen else RedLoss
            )
            DetailRow(
                label = "Leverage",
                value = "${trade.leverage}x"
            )
            DetailRow(
                label = "Quantity",
                value = Formatters.formatPriceSimple(trade.quantity)
            )
            DetailRow(
                label = "Margin",
                value = Formatters.formatCurrency(trade.margin)
            )
            DetailRow(
                label = "Status",
                value = trade.status,
                valueColor = if (trade.isOpen()) PrimaryGreen else TextMuted
            )
        }
    }
}

/**
 * Prices Card showing entry and exit prices
 */
@Composable
private fun PricesCard(trade: Trade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Price Information",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            DetailRow(
                label = "Entry Price",
                value = Formatters.formatPrice(trade.entryPrice)
            )

            if (trade.exitPrice != null) {
                DetailRow(
                    label = "Exit Price",
                    value = Formatters.formatPrice(trade.exitPrice!!)
                )

                val priceChange = trade.exitPrice!! - trade.entryPrice
                DetailRow(
                    label = "Price Change",
                    value = Formatters.formatPnL(priceChange),
                    valueColor = priceChange.getValueColor()
                )
            } else {
                DetailRow(
                    label = "Current Price",
                    value = "—"
                )
            }
        }
    }
}

/**
 * P&L Breakdown Card
 */
@Composable
private fun PnLBreakdownCard(trade: Trade) {
    val pnlAmount = trade.pnlAmount ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pnlAmount >= 0) {
                PrimaryGreen.copy(alpha = 0.1f)
            } else {
                RedLoss.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "P&L Analysis",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            DetailRow(
                label = "P&L Amount",
                value = Formatters.formatCurrency(pnlAmount),
                valueColor = pnlAmount.getValueColor()
            )

            if (trade.margin > 0) {
                val pnlPercent = (pnlAmount / trade.margin) * 100
                DetailRow(
                    label = "P&L %",
                    value = Formatters.formatPercentage(pnlPercent),
                    valueColor = pnlPercent.getValueColor()
                )
            }

            DetailRow(
                label = "Open Time",
                value = Formatters.formatDateTime(trade.openTime)
            )

            if (trade.closeTime != null) {
                DetailRow(
                    label = "Close Time",
                    value = Formatters.formatDateTime(trade.closeTime!!)
                )
            }
        }
    }
}

/**
 * Notes Card
 */
@Composable
private fun NotesCard(trade: Trade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Notes",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = trade.notes ?: "No notes",
                fontSize = 13.sp,
                color = Color.White,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Detail row showing label and value
 */
@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * Edit Trade Bottom Sheet
 */
@Composable
private fun EditTradeBottomSheet(
    isVisible: Boolean,
    trade: Trade?,
    viewModel: TradeDetailViewModel,
    onDismiss: () -> Unit
) {
    if (trade != null && isVisible) {
        BottomSheet(
            isVisible = isVisible,
            title = "Edit Trade",
            onDismiss = onDismiss
        ) {
            // Entry Price
            EditableField(
                label = "Entry Price",
                value = Formatters.formatPrice(trade.entryPrice),
                onChange = { viewModel.updateTradeField("entryPrice", it.toDoubleOrNull() ?: 0.0) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Exit Price
            if (trade.exitPrice != null) {
                EditableField(
                    label = "Exit Price",
                    value = Formatters.formatPrice(trade.exitPrice!!),
                    onChange = { viewModel.updateTradeField("exitPrice", it.toDoubleOrNull()) }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Notes
            EditableField(
                label = "Notes",
                value = trade.notes ?: "",
                onChange = { viewModel.updateTradeField("notes", it) },
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Save",
                    onClick = {
                        viewModel.saveEditedTrade()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Editable Field Component for bottom sheet
 */
@Composable
private fun EditableField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    maxLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            maxLines = maxLines
        )
    }
}

// Extension functions for color
private fun Double.getValueColor(): Color {
    return if (this >= 0.0) PrimaryGreen else RedLoss
}

private fun Trade.getDirectionBadge(): String {
    return if (position == Constants.Trade.POSITION_LONG) "LONG" else "SHORT"
}

private fun Trade.isOpen(): Boolean {
    return status == Constants.Trade.STATUS_OPEN
}
