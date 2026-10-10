package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate850
import com.example.ui.viewmodel.BarComparisonItem
import com.example.ui.viewmodel.formatCurrency
import kotlin.math.max

@Composable
fun DualBarChart(
    items: List<BarComparisonItem>,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No financial data available for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
            }
        }
        return
    }

    var selectedIndex by remember(items) { mutableStateOf(items.lastIndex) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(items) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val maxVal = remember(items) {
        val peak = items.maxOfOrNull { max(it.income, it.expense) } ?: 1000.0
        if (peak <= 0.0) 1000.0 else peak * 1.15
    }

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Legend & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cash Flow Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendItem(color = EmeraldPrimary, label = "Income")
                    Spacer(modifier = Modifier.width(12.dp))
                    LegendItem(color = ExpenseRose, label = "Expense")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .pointerInput(items) {
                            detectTapGestures { offset ->
                                val slotWidth = size.width / items.size
                                val clickedIdx = (offset.x / slotWidth).toInt().coerceIn(0, items.lastIndex)
                                selectedIndex = clickedIdx
                            }
                        }
                ) {
                    drawChart(
                        items = items,
                        maxVal = maxVal,
                        anim = animProgress.value,
                        selectedIndex = selectedIndex,
                        textMeasurer = textMeasurer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Item Detail Card
            if (selectedIndex in items.indices) {
                val selected = items[selectedIndex]
                val net = selected.income - selected.expense
                val isPositive = net >= 0

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selected.label} (${selected.subLabel.ifEmpty { "Selected" }})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Net: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = (if (isPositive) "+" else "") + formatCurrency(net, currencySymbol),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) EmeraldPrimary else ExpenseRose
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatCurrency(selected.income, currencySymbol),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "Inflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatCurrency(selected.expense, currencySymbol),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRose
                                )
                                Text(
                                    text = "Outflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawChart(
    items: List<BarComparisonItem>,
    maxVal: Double,
    anim: Float,
    selectedIndex: Int,
    textMeasurer: TextMeasurer
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val bottomLabelHeight = 28f
    val chartPlotHeight = canvasHeight - bottomLabelHeight

    // Draw grid lines
    val gridLines = 3
    for (g in 0..gridLines) {
        val y = chartPlotHeight * (g.toFloat() / gridLines)
        drawLine(
            color = Color(0xFF334155).copy(alpha = 0.45f),
            start = Offset(0f, y),
            end = Offset(canvasWidth, y),
            strokeWidth = 1f
        )
    }

    val slotWidth = canvasWidth / items.size
    val barWidth = (slotWidth * 0.28f).coerceAtMost(24f)
    val barGap = 3f

    items.forEachIndexed { index, item ->
        val slotCenterX = slotWidth * index + (slotWidth / 2f)

        // Draw highlight background behind selected slot
        if (index == selectedIndex) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.07f),
                topLeft = Offset(slotWidth * index + 2f, 0f),
                size = Size(slotWidth - 4f, chartPlotHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }

        // Heights
        val incomeHeight = ((item.income / maxVal) * chartPlotHeight * anim).toFloat().coerceAtLeast(3f)
        val expenseHeight = ((item.expense / maxVal) * chartPlotHeight * anim).toFloat().coerceAtLeast(3f)

        val incomeLeft = slotCenterX - barWidth - barGap
        val incomeTop = chartPlotHeight - incomeHeight

        val expenseLeft = slotCenterX + barGap
        val expenseTop = chartPlotHeight - expenseHeight

        // Draw Income Bar (Emerald)
        drawRoundRect(
            color = if (item.income > 0) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.25f),
            topLeft = Offset(incomeLeft, incomeTop),
            size = Size(barWidth, incomeHeight),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Draw Expense Bar (Rose)
        drawRoundRect(
            color = if (item.expense > 0) ExpenseRose else ExpenseRose.copy(alpha = 0.25f),
            topLeft = Offset(expenseLeft, expenseTop),
            size = Size(barWidth, expenseHeight),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Draw Bottom Label
        val labelLayout = textMeasurer.measure(
            text = item.label,
            style = TextStyle(
                color = if (index == selectedIndex) Color.White else Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal
            )
        )
        val labelX = slotCenterX - (labelLayout.size.width / 2f)
        val labelY = chartPlotHeight + 6f
        drawText(
            textLayoutResult = labelLayout,
            topLeft = Offset(labelX, labelY)
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFCBD5E1)
        )
    }
}
