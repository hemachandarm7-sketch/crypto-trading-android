package com.tradingdashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tradingdashboard.presentation.theme.CardDarkBg
import com.tradingdashboard.presentation.theme.PrimaryGreen
import com.tradingdashboard.presentation.theme.RedLoss
import com.tradingdashboard.presentation.theme.TextMuted

/**
 * Confirmation dialog for critical actions
 */
@Composable
fun ConfirmDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onCancel,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth(0.9f)
                    .background(CardDarkBg, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon
                    Text(
                        text = if (isDestructive) "⚠️" else "❓",
                        fontSize = 40.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Title
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Message
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        Button(
                            onClick = onCancel,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardDarkBg.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = cancelText,
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }

                        // Confirm Button
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDestructive) RedLoss else PrimaryGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = confirmText,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple yes/no confirmation dialog
 */
@Composable
fun YesNoDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    onYes: () -> Unit,
    onNo: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConfirmDialog(
        isVisible = isVisible,
        title = title,
        message = message,
        confirmText = "Yes",
        cancelText = "No",
        onConfirm = onYes,
        onCancel = onNo,
        modifier = modifier
    )
}

/**
 * Delete confirmation dialog with special styling
 */
@Composable
fun DeleteConfirmDialog(
    isVisible: Boolean,
    itemName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConfirmDialog(
        isVisible = isVisible,
        title = "Delete $itemName?",
        message = "This action cannot be undone. Are you sure you want to delete this $itemName?",
        confirmText = "Delete",
        cancelText = "Cancel",
        isDestructive = true,
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier
    )
}

/**
 * Information dialog (read-only)
 */
@Composable
fun InfoDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth(0.9f)
                    .background(CardDarkBg, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon
                    Text(
                        text = "ℹ️",
                        fontSize = 40.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Title
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Message
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // OK Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "OK",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Input dialog for text input
 */
@Composable
fun InputDialog(
    isVisible: Boolean,
    title: String,
    placeholder: String,
    initialValue: String = "",
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        var value by androidx.compose.runtime.remember { 
            androidx.compose.runtime.mutableStateOf(initialValue) 
        }

        Dialog(
            onDismissRequest = onCancel,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth(0.9f)
                    .background(CardDarkBg, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Input field
                    androidx.compose.material3.OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        placeholder = {
                            Text(
                                text = placeholder,
                                color = TextMuted.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onCancel,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardDarkBg.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = { onConfirm(value) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Confirm",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
