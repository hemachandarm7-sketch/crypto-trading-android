package com.tradingdashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradingdashboard.data.model.Trade
import com.tradingdashboard.presentation.theme.CardDarkBg
import com.tradingdashboard.presentation.theme.GreenWin
import com.tradingdashboard.presentation.theme.PrimaryGreen
import com.tradingdashboard.presentation.theme.RedLoss
import com.tradingdashboard.presentation.theme.TextMuted
import com.tradingdashboard.util.Formatters

/**
 * Pie chart for win/loss ratio
 */
@Composable
fun WinLossChart(
    wins: Int,
    losses: Int,
    modifier: Modifier = Modifier
) {
    val total = wins + losses
    val winPercentage = if (total > 0) (wins * 100) / total else 0
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Win/Loss Ratio",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie chart visualization
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Win/Loss indicator
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = androidx.compose.foundation.background(
                                    if (winPercentage > 50) GreenWin else RedLoss
                                ).brush ?: androidx.compose.foundation.brush.SolidColor(CardDarkBg),
                                shape = RoundedCornerShape(40.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$winPercentage%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(GreenWin, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = "Wins: $wins",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(RedLoss, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = "Losses: $losses",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bar chart for P&L distribution
 */
@Composable
fun PnLDistributionChart(
    trades: List<Trade>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "P&L Distribution",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (trades.isEmpty()) {
                Text(
                    text = "No trades to display",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                return@Column
            }

            // Histogram of P&L ranges
            val pnlValues = trades.mapNotNull { it.pnlAmount }
            val minPnl = pnlValues.minOrNull() ?: 0.0
            val maxPnl = pnlValues.maxOrNull() ?: 0.0

            // Divide into bins
            val binCount = 5
            val binSize = if (maxPnl > minPnl) (maxPnl - minPnl) / binCount else 1.0
            val bins = MutableList(binCount) { 0 }

            for (pnl in pnlValues) {
                val binIndex = ((pnl - minPnl) / binSize).toInt().coerceIn(0, binCount - 1)
                bins[binIndex]++
            }

            val maxBinCount = bins.maxOrNull() ?: 1

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(binCount) { i ->
                    val percentage = if (maxBinCount > 0) {
                        (bins[i] * 100) / maxBinCount
                    } else {
                        0
                    }

                    val binStart = minPnl + (i * binSize)
                    val binEnd = binStart + binSize

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${Formatters.formatCurrency(binStart)} - ${Formatters.formatCurrency(binEnd)}",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(percentage / 100f)
                                    .background(
                                        color = if (binStart + binEnd / 2 >= 0) GreenWin else RedLoss,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )

                            Text(
                                text = "${bins[i]}",
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Line chart for cumulative P&L over time
 */
@Composable
fun CumulativePnLChart(
    trades: List<Trade>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cumulative P&L",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (trades.isEmpty()) {
                Text(
                    text = "No trades to display",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                return@Column
            }

            val sortedTrades = trades.sortedBy { it.openTime }
            var cumulativePnl = 0.0
            val dataPoints = mutableListOf<Pair<Long, Double>>()

            for (trade in sortedTrades) {
                cumulativePnl += (trade.pnlAmount ?: 0.0)
                dataPoints.add(Pair(trade.openTime, cumulativePnl))
            }

            // Simplified line chart visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp))
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val minX = dataPoints.minOf { it.first }
                    val maxX = dataPoints.maxOf { it.first }
                    val minY = dataPoints.minOf { it.second }
                    val maxY = dataPoints.maxOf { it.second }

                    val xRange = maxX - minX
                    val yRange = if (maxY > minY) maxY - minY else 1.0

                    for (i in 0 until dataPoints.size - 1) {
                        val (x1, y1) = dataPoints[i]
                        val (x2, y2) = dataPoints[i + 1]

                        val x1Pos = ((x1 - minX) / xRange) * size.width
                        val y1Pos = size.height - ((y1 - minY) / yRange) * size.height
                        val x2Pos = ((x2 - minX) / xRange) * size.width
                        val y2Pos = size.height - ((y2 - minY) / yRange) * size.height

                        drawLine(
                            color = if (y2 >= y1) GreenWin else RedLoss,
                            start = androidx.compose.ui.geometry.Offset(x1Pos.toFloat(), y1Pos.toFloat()),
                            end = androidx.compose.ui.geometry.Offset(x2Pos.toFloat(), y2Pos.toFloat()),
                            strokeWidth = 2f
                        )
                    }
                }

                // Show final value
                Text(
                    text = Formatters.formatCurrency(cumulativePnl),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (cumulativePnl >= 0) GreenWin else RedLoss,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }

            // Time range info
            if (dataPoints.isNotEmpty()) {
                Text(
                    text = "${Formatters.formatDate(dataPoints.first().first)} - ${Formatters.formatDate(dataPoints.last().first)}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * Canvas import helper
 */
@Composable
private fun Canvas(
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.Canvas
) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = content)
}
