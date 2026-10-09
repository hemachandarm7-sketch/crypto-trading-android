package com.tradingdashboard.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradingdashboard.presentation.viewmodel.AuthViewModel
import com.tradingdashboard.ui.theme.GreenWin
import com.tradingdashboard.ui.theme.RedLoss

@Composable
fun BiometricAuthScreen(
    activity: FragmentActivity?,
    onAuthSuccess: () -> Unit,
    onSkipBiometric: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        if (activity != null) {
            // Attempt biometric authentication on screen load
            viewModel.attemptBiometricAuth(activity)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Icon
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Biometric",
                modifier = Modifier.size(80.dp),
                tint = GreenWin
            )

            // Title
            Text(
                text = "Quick Unlock",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Subtitle
            Text(
                text = "Use your fingerprint or face to unlock the app",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Loading state
            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Waiting for biometric...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = RedLoss,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip button
            if (!isLoading) {
                TextButton(
                    onClick = onSkipBiometric
                ) {
                    Text("Skip for Now")
                }

                Button(
                    onClick = {
                        if (activity != null) {
                            viewModel.attemptBiometricAuth(activity)
                        }
                    }
                ) {
                    Text("Try Again")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info text
            Text(
                text = "You can set up biometric unlock in Settings later",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
