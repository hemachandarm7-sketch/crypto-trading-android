package com.tradingdashboard.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradingdashboard.presentation.theme.PrimaryGreen
import com.tradingdashboard.presentation.theme.TextMuted
import com.tradingdashboard.presentation.viewmodel.Currency
import com.tradingdashboard.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val autoSyncEnabled by viewModel.autoSyncEnabled.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val appVersion by viewModel.appVersion.collectAsState()
    val buildNumber by viewModel.buildNumber.collectAsState()

    var showCurrencyMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

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
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Customize your app preferences",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Success",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Settings saved successfully",
                            color = PrimaryGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            }
        }

        // Appearance Section
        item {
            SettingsSection(
                title = "Appearance",
                icon = Icons.Filled.Palette
            ) {
                // Dark Mode Toggle
                SettingItem(
                    label = "Dark Mode",
                    subtitle = "Use dark theme throughout the app",
                    trailing = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) },
                            modifier = Modifier.scale(0.9f)
                        )
                    }
                )
            }
        }

        // General Section
        item {
            SettingsSection(
                title = "General",
                icon = Icons.Filled.Palette
            ) {
                // Currency Selection
                Box {
                    SettingItem(
                        label = "Currency",
                        subtitle = currency.name,
                        trailing = { Text("${currency.name}", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier.clickable { showCurrencyMenu = true }
                    )

                    DropdownMenu(
                        expanded = showCurrencyMenu,
                        onDismissRequest = { showCurrencyMenu = false },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Currency.values().forEach { curr ->
                            DropdownMenuItem(
                                text = { Text(curr.name) },
                                onClick = {
                                    viewModel.setCurrency(curr)
                                    showCurrencyMenu = false
                                },
                                leadingIcon = if (curr == currency) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = PrimaryGreen) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // Sync Section
        item {
            SettingsSection(
                title = "Sync & Data",
                icon = Icons.Filled.CloudSync
            ) {
                // Auto Sync Toggle
                SettingItem(
                    label = "Auto Sync",
                    subtitle = "Automatically sync trades with server",
                    trailing = {
                        Switch(
                            checked = autoSyncEnabled,
                            onCheckedChange = { viewModel.setAutoSyncEnabled(it) },
                            modifier = Modifier.scale(0.9f)
                        )
                    }
                )

                Divider(
                    color = MaterialTheme.colorScheme.background,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Sync Now Button
                Button(
                    onClick = { viewModel.syncNow() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        disabledContainerColor = TextMuted.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.background,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing...", fontSize = 12.sp)
                    } else {
                        Icon(
                            Icons.Filled.CloudSync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync Now", fontSize = 12.sp)
                    }
                }
            }
        }

        // Security Section
        item {
            SettingsSection(
                title = "Security",
                icon = Icons.Filled.DarkMode
            ) {
                // Biometric Auth Toggle
                SettingItem(
                    label = "Biometric Authentication",
                    subtitle = "Use fingerprint or face ID to unlock",
                    trailing = {
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) },
                            modifier = Modifier.scale(0.9f)
                        )
                    }
                )
            }
        }

        // Actions Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Save Button
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        disabledContainerColor = TextMuted.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.background,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...", fontSize = 14.sp, fontWeight = FontWeight.W600)
                    } else {
                        Text(
                            text = "Save Settings",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600
                        )
                    }
                }

                // Reset Button
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextMuted
                    )
                ) {
                    Text(
                        text = "Reset to Defaults",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                }
            }
        }

        // About Section
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Info",
                            modifier = Modifier.size(20.dp),
                            tint = PrimaryGreen
                        )
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.background, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "App Version",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = appVersion,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Build Number",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = buildNumber,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "ORBIT Trading Dashboard © 2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = 10.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Defaults?") },
            text = { Text("This will reset all settings to their default values.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
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
            // Section Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    tint = PrimaryGreen
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Divider(color = MaterialTheme.colorScheme.background, thickness = 1.dp)

            // Section Content
            content()
        }
    }
}

@Composable
fun SettingItem(
    label: String,
    subtitle: String = "",
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W500,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        }
    }
}
