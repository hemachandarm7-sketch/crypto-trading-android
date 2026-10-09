package com.tradingdashboard.presentation.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradingdashboard.presentation.theme.CardDarkBg
import com.tradingdashboard.presentation.theme.PrimaryGreen
import com.tradingdashboard.presentation.theme.TextMuted
import com.tradingdashboard.util.Constants

/**
 * Loading indicator with message
 */
@Composable
fun LoadingIndicator(
    message: String = Constants.UI.LOADING_MESSAGE,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = PrimaryGreen,
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = TextMuted,
            fontSize = 14.sp
        )
    }
}

/**
 * Shimmer effect for skeleton loading
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = 800)
        ),
        label = "shimmerAlpha"
    )

    Spacer(
        modifier = modifier
            .clip(shape)
            .background(
                color = CardDarkBg.copy(alpha = alpha)
            )
    )
}

/**
 * Skeleton for trade card loading
 */
@Composable
fun TradeCardSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardDarkBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerEffect(modifier = Modifier.size(80.dp, 20.dp))
                ShimmerEffect(modifier = Modifier.size(60.dp, 20.dp))
            }
            ShimmerEffect(modifier = Modifier.fillMaxWidth().height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerEffect(modifier = Modifier.size(70.dp, 16.dp))
                ShimmerEffect(modifier = Modifier.size(70.dp, 16.dp))
            }
        }
    }
}

/**
 * Skeleton for analytics stats loading
 */
@Composable
fun StatsCardSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardDarkBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerEffect(modifier = Modifier.size(100.dp, 16.dp))
            ShimmerEffect(modifier = Modifier.fillMaxWidth().height(24.dp))
        }
    }
}

/**
 * Skeleton list of trades
 */
@Composable
fun TradeListSkeleton(modifier: Modifier = Modifier, count: Int = 5) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            item {
                TradeCardSkeleton()
            }
        }
    }
}

/**
 * Empty state with message
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: String = Constants.UI.EMPTY_STATE_ICON,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = message,
            fontSize = 14.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        action?.invoke()
    }
}

/**
 * Error state with retry option
 */
@Composable
fun ErrorState(
    title: String = "Error",
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = message,
            fontSize = 14.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        if (onRetry != null) {
            PrimaryButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Loading full screen overlay
 */
@Composable
fun FullScreenLoadingOverlay(
    isVisible: Boolean,
    message: String = Constants.UI.LOADING_MESSAGE,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator(message = message)
        }
    }
}

// LazyColumn import for skeleton list
@Composable
private fun LazyColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        content()
    }
}
