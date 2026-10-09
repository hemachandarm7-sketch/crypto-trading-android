package com.tradingdashboard.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
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
import com.tradingdashboard.presentation.theme.PrimaryGreen
import com.tradingdashboard.presentation.theme.RedLoss
import com.tradingdashboard.presentation.theme.TextMuted
import com.tradingdashboard.presentation.viewmodel.ExtractedTradeData
import com.tradingdashboard.presentation.viewmodel.UploadViewModel

@Composable
fun UploadScreen(
    viewModel: UploadViewModel = hiltViewModel()
) {
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val extractedData by viewModel.extractedData.collectAsState()
    val isOCRRunning by viewModel.isOCRRunning.collectAsState()
    val ocrError by viewModel.ocrError.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

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
                    text = "Extract Trade from Screenshot",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Upload a screenshot of your trade to auto-extract details",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }

        // Success Message
        if (saveSuccess == true) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryGreen.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✓ Trade Saved Successfully!",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Your trade has been added to the database.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = { viewModel.resetSaveStatus() },
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Text("New Trade", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Error Message
        if (ocrError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = RedLoss.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Error",
                                color = RedLoss,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = ocrError ?: "",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = RedLoss
                            )
                        }
                    }
                }
            }
        }

        // Image Upload Section
        if (selectedImageUri == null) {
            item {
                UploadPlaceholder(
                    onCameraClick = { /* TODO: Launch camera */ },
                    onGalleryClick = { /* TODO: Launch gallery picker */ }
                )
            }
        } else {
            // Image Preview
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.Image,
                                contentDescription = "Image",
                                modifier = Modifier.size(64.dp),
                                tint = TextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Image Selected",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Ready for OCR processing",
                                color = TextMuted.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // OCR Processing
            if (isOCRRunning) {
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
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = PrimaryGreen
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Extracting trade data...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.W500
                            )
                            Text(
                                text = "Using OCR to parse screenshot",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else if (extractedData != null) {
                // Extracted Data Display
                item {
                    ExtractedDataForm(
                        data = extractedData!!,
                        onFieldChange = { field, value ->
                            viewModel.updateExtractedField(field, value)
                        },
                        isSaving = isSaving,
                        onSave = { viewModel.saveTrade() }
                    )
                }
            } else {
                // Start OCR Button
                item {
                    Button(
                        onClick = {
                            selectedImageUri?.let { viewModel.startOCRExtraction(it) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Extract Trade Data (OCR)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W600
                        )
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.clearSelectedImage() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Choose Different Image",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UploadPlaceholder(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCameraClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = "Camera",
                    modifier = Modifier.size(40.dp),
                    tint = PrimaryGreen
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Take a Screenshot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Capture a screenshot of your trade",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Arrow",
                    modifier = Modifier.size(24.dp),
                    tint = TextMuted
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onGalleryClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = "Gallery",
                    modifier = Modifier.size(40.dp),
                    tint = PrimaryGreen
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Choose from Gallery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Select from your photos",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Arrow",
                    modifier = Modifier.size(24.dp),
                    tint = TextMuted
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡 Tip: Best Results",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    fontSize = 12.sp
                )
                Text(
                    text = "• Clear, well-lit screenshot\n• Include all trade details\n• Avoid cropping important info",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ExtractedDataForm(
    data: ExtractedTradeData,
    onFieldChange: (String, String) -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Extracted Trade Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Symbol
        TradeFormField(
            label = "Symbol",
            value = data.symbol,
            onValueChange = { onFieldChange("symbol", it) }
        )

        // Position & Leverage Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TradeFormField(
                label = "Position",
                value = data.position,
                onValueChange = { onFieldChange("position", it) },
                modifier = Modifier.weight(1f)
            )
            TradeFormField(
                label = "Leverage",
                value = data.leverage.toString(),
                onValueChange = { onFieldChange("leverage", it) },
                modifier = Modifier.weight(1f)
            )
        }

        // Entry & Exit Price Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TradeFormField(
                label = "Entry Price",
                value = data.entryPrice.toString(),
                onValueChange = { onFieldChange("entryPrice", it) },
                modifier = Modifier.weight(1f)
            )
            TradeFormField(
                label = "Exit Price",
                value = data.exitPrice?.toString() ?: "",
                onValueChange = { onFieldChange("exitPrice", it) },
                modifier = Modifier.weight(1f)
            )
        }

        // Quantity & Margin Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TradeFormField(
                label = "Quantity",
                value = data.quantity.toString(),
                onValueChange = { onFieldChange("quantity", it) },
                modifier = Modifier.weight(1f)
            )
            TradeFormField(
                label = "Margin",
                value = data.margin.toString(),
                onValueChange = { onFieldChange("margin", it) },
                modifier = Modifier.weight(1f)
            )
        }

        // P&L Display
        if (data.pnlAmount != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (data.pnlAmount >= 0)
                        PrimaryGreen.copy(alpha = 0.1f)
                    else
                        RedLoss.copy(alpha = 0.1f)
                )
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
                            text = "Profit/Loss",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$${String.format("%.2f", data.pnlAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (data.pnlAmount >= 0) PrimaryGreen else RedLoss
                        )
                    }
                    Text(
                        text = "${String.format("%.1f", data.pnlPercentage ?: 0.0)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (data.pnlAmount >= 0) PrimaryGreen else RedLoss
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isSaving && data.symbol.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                disabledContainerColor = TextMuted.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving...", fontSize = 16.sp, fontWeight = FontWeight.W600)
            } else {
                Text(
                    text = "Save Trade to Database",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }
}

@Composable
fun TradeFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = PrimaryGreen,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}
