package com.tradingdashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tradingdashboard.presentation.theme.CardDarkBg
import com.tradingdashboard.presentation.theme.TextMuted

/**
 * Bottom sheet modal component
 */
@Composable
fun BottomSheet(
    isVisible: Boolean,
    title: String? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = CardDarkBg,
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp
                            )
                        )
                        .padding(bottom = 32.dp)
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(
                                color = TextMuted.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )

                    // Title
                    if (title != null) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 20.dp,
                                bottom = 16.dp
                            )
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        content = content
                    )
                }
            }
        }
    }
}

/**
 * Bottom sheet option item
 */
@Composable
fun BottomSheetOption(
    icon: String,
    label: String,
    description: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDestructive) {
                        com.tradingdashboard.presentation.theme.RedLoss
                    } else {
                        Color.White
                    }
                )

                if (description != null) {
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Text(
                text = "›",
                fontSize = 24.sp,
                color = TextMuted,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

/**
 * Bottom sheet with options list
 */
@Composable
fun OptionsBottomSheet(
    isVisible: Boolean,
    title: String,
    options: List<BottomSheetOptionItem>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomSheet(
        isVisible = isVisible,
        title = title,
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        options.forEachIndexed { index, option ->
            androidx.compose.foundation.clickable(
                onClick = {
                    option.onClick()
                    onDismiss()
                }
            ) {
                BottomSheetOption(
                    icon = option.icon,
                    label = option.label,
                    description = option.description,
                    onClick = {
                        option.onClick()
                        onDismiss()
                    },
                    isDestructive = option.isDestructive
                )
            }

            if (index < options.size - 1) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TextMuted.copy(alpha = 0.1f))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SecondaryButton(
            text = "Close",
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Data class for bottom sheet options
 */
data class BottomSheetOptionItem(
    val icon: String,
    val label: String,
    val description: String? = null,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

/**
 * Bottom sheet with filters/sorting options
 */
@Composable
fun FilterBottomSheet(
    isVisible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    BottomSheet(
        isVisible = isVisible,
        title = title,
        onDismiss = onDismiss,
        modifier = modifier,
        content = content
    )
}

/**
 * Import clickable modifier
 */
@Composable
private fun androidx.compose.foundation.Modifier.clickable(
    onClick: () -> Unit
): androidx.compose.foundation.Modifier {
    return androidx.compose.foundation.clickable(
        indication = androidx.compose.material.ripple.rememberRipple(),
        interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
        onClick = onClick
    )
}
