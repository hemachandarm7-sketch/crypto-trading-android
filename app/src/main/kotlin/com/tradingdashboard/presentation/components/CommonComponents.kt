package com.tradingdashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradingdashboard.presentation.theme.CardDarkBg
import com.tradingdashboard.presentation.theme.PrimaryGreen
import com.tradingdashboard.presentation.theme.TextMuted
import com.tradingdashboard.util.Constants

// ============ Button Components ============

/**
 * Primary button - main action button with green background
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (leadingIcon != null && !isLoading) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

/**
 * Secondary button - alternative action button with transparent background
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .height(44.dp)
            .border(
                width = 1.dp,
                color = TextMuted.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (leadingIcon != null && !isLoading) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = TextMuted
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted
        )
    }
}

/**
 * Danger button - destructive action button with red background
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = com.tradingdashboard.presentation.theme.RedLoss,
            disabledContainerColor = com.tradingdashboard.presentation.theme.RedLoss.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

/**
 * Small icon button
 */
@Composable
fun IconButtonSmall(
    icon: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = PrimaryGreen
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ============ Input Components ============

/**
 * Text input field with custom styling
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    isPassword: Boolean = false,
    maxLines: Int = 1,
    singleLine: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextMuted.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                null
            },
            trailingIcon = if (trailingIcon != null) {
                {
                    IconButton(onClick = onTrailingIconClick ?: {}) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardDarkBg,
                unfocusedContainerColor = CardDarkBg,
                focusedIndicatorColor = PrimaryGreen,
                unfocusedIndicatorColor = CardDarkBg,
                cursorColor = PrimaryGreen,
                errorIndicatorColor = com.tradingdashboard.presentation.theme.RedLoss
            ),
            maxLines = maxLines,
            singleLine = singleLine,
            isError = isError,
            enabled = enabled,
            shape = RoundedCornerShape(8.dp)
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = com.tradingdashboard.presentation.theme.RedLoss,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Search input field
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onClear: (() -> Unit)? = null
) {
    CustomTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = placeholder,
        leadingIcon = Icons.Filled.Search,
        trailingIcon = if (value.isNotEmpty()) Icons.Filled.Close else null,
        onTrailingIconClick = {
            onValueChange("")
            onClear?.invoke()
        },
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Search
    )
}

/**
 * Email input field
 */
@Composable
fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Email",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    CustomTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = "your@email.com",
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Next,
        isError = isError,
        errorMessage = errorMessage
    )
}

/**
 * Password input field
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    CustomTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = "••••••••",
        keyboardType = KeyboardType.Password,
        isPassword = !isPasswordVisible,
        trailingIcon = if (isPasswordVisible) {
            Icons.Filled.Visibility
        } else {
            Icons.Filled.VisibilityOff
        },
        onTrailingIconClick = { isPasswordVisible = !isPasswordVisible },
        isError = isError,
        errorMessage = errorMessage
    )
}

/**
 * Number input field
 */
@Composable
fun NumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "0.00",
    isError: Boolean = false,
    errorMessage: String? = null,
    decimals: Int = 2
) {
    CustomTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                onValueChange(newValue)
            }
        },
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        keyboardType = KeyboardType.Decimal,
        imeAction = ImeAction.Done,
        isError = isError,
        errorMessage = errorMessage
    )
}

/**
 * Multiline text area
 */
@Composable
fun TextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    minLines: Int = 3,
    maxLines: Int = 5
) {
    CustomTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        label = label,
        maxLines = maxLines,
        singleLine = false,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Default
    )
}

// ============ Chip Components ============

/**
 * Filter chip
 */
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(32.dp)
            .padding(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PrimaryGreen else CardDarkBg,
            contentColor = if (isSelected) Color.White else TextMuted
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Status chip
 */
@Composable
fun StatusChip(
    label: String,
    backgroundColor: Color = PrimaryGreen,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

// ============ Divider Components ============

/**
 * Custom divider
 */
@Composable
fun DividerLine(
    modifier: Modifier = Modifier,
    color: Color = TextMuted.copy(alpha = 0.1f),
    thickness: Int = 1
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness.dp)
            .background(color)
    )
}

/**
 * Divider with text
 */
@Composable
fun DividerWithText(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DividerLine(modifier = Modifier.weight(1f))
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextMuted
        )
        DividerLine(modifier = Modifier.weight(1f))
    }
}

// ============ Additional Imports ============

// Import Icons for Visibility
@Composable
private fun Icons.Companion.Filled.Visibility(): ImageVector {
    return com.tradingdashboard.presentation.theme.Icons.Filled.Visibility
}

@Composable
private fun Icons.Companion.Filled.VisibilityOff(): ImageVector {
    return com.tradingdashboard.presentation.theme.Icons.Filled.VisibilityOff
}

@Composable
private fun Icons.Companion.Filled.Close(): ImageVector {
    return com.tradingdashboard.presentation.theme.Icons.Filled.Close
}

// Fallback for visibility icons if not in theme
private object Icons {
    object Filled {
        val Visibility: ImageVector
            @Composable get() = androidx.compose.material.icons.filled.Visibility
        
        val VisibilityOff: ImageVector
            @Composable get() = androidx.compose.material.icons.filled.VisibilityOff
        
        val Close: ImageVector
            @Composable get() = androidx.compose.material.icons.filled.Close
    }
}
