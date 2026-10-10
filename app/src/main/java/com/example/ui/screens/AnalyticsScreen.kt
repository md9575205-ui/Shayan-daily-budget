package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DualBarChart
import com.example.ui.components.EXPENSE_CATEGORIES
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseDark
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.AnalyticsPeriod
import com.example.ui.viewmodel.BudgetUiState
import com.example.ui.viewmodel.formatCurrency
import kotlin.math.max

@Composable
fun AnalyticsScreen(
    state: BudgetUiState,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val isWeekly = state.analyticsPeriod == AnalyticsPeriod.WEEKLY
    val currentBars = if (isWeekly) state.weeklyChartBars else state.monthlyChartBars
    val periodTotalIncome = if (isWeekly) state.weeklyTotalIncome else state.monthlyTotalIncome
    val periodTotalExpense = if (isWeekly) state.weeklyTotalExpense else state.monthlyTotalExpense
    val periodNet = periodTotalIncome - periodTotalExpense
    val savingsRate = if (periodTotalIncome > 0) {
        max(0.0, (periodNet / periodTotalIncome) * 100).toInt()
    } else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16)),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header & Period Toggle
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Visual Comparative Analytics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Side-by-side income vs expense comparisons",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Timeframe Selector Pill (Weekly vs Monthly)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate850)
                        .padding(4.dp)
                ) {
                    // Weekly Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isWeekly) EmeraldPrimary else Color.Transparent)
                            .clickable { onPeriodSelected(AnalyticsPeriod.WEEKLY) }
                            .padding(vertical = 10.dp)
                            .testTag("weekly_period_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Weekly (7 Days)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isWeekly) Color.Black else Color(0xFFCBD5E1)
                        )
                    }

                    // Monthly Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isWeekly) EmeraldPrimary else Color.Transparent)
                            .clickable { onPeriodSelected(AnalyticsPeriod.MONTHLY) }
                            .padding(vertical = 10.dp)
                            .testTag("monthly_period_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Monthly (4 Weeks)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (!isWeekly) Color.Black else Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        // Full Total Summary & Comparison Chart Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isWeekly) "Full 1-Week Total Summary" else "Full 1-Month Total Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (periodNet >= 0) EmeraldPrimary.copy(alpha = 0.2f) else ExpenseRose.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (periodNet >= 0) "Surplus" else "Deficit",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (periodNet >= 0) EmeraldPrimary else ExpenseRose,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dual Totals Side-by-Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Income
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF064E3B).copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldDark.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Total Income",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFA7F3D0)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = formatCurrency(periodTotalIncome, state.currencySymbol),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldPrimary
                                )
                            }
                        }

                        // Total Expense
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF4C0519).copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseDark.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = ExpenseRose,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Total Expenses",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFECDD3)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = formatCurrency(periodTotalExpense, state.currencySymbol),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = ExpenseRose
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Total Ratio Bar (Income vs Expense)
                    val totalSum = (periodTotalIncome + periodTotalExpense).coerceAtLeast(1.0)
                    val incomeRatio = (periodTotalIncome / totalSum).toFloat()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cash Flow Ratio",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "Savings Rate: $savingsRate%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (savingsRate > 20) EmeraldPrimary else AmberGold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            if (periodTotalIncome == 0.0 && periodTotalExpense == 0.0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Slate800)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(incomeRatio.coerceIn(0.01f, 0.99f))
                                        .fillMaxSize()
                                        .background(EmeraldPrimary)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight((1f - incomeRatio).coerceIn(0.01f, 0.99f))
                                        .fillMaxSize()
                                        .background(ExpenseRose)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Visual Side-by-Side Chart
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isWeekly) "Daily Breakdown (7-Day Comparison)" else "Weekly Breakdown (4-Week Comparison)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                DualBarChart(
                    items = currentBars,
                    currencySymbol = state.currencySymbol
                )
            }
        }

        // Category-Based Expenditure Distribution
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Spending Tagging",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "${state.categorySpendings.size} Categories",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                if (state.categorySpendings.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate850)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No category expense data recorded yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else {
                    state.categorySpendings.forEach { catItem ->
                        val catDef = EXPENSE_CATEGORIES.find { it.name == catItem.category }
                            ?: EXPENSE_CATEGORIES.last()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate850),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = catDef.color.copy(alpha = 0.2f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = catDef.icon,
                                                    contentDescription = catItem.category,
                                                    tint = catDef.color,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = catItem.category,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${(catItem.percentageOfTotal * 100).toInt()}% of total expenses",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    Text(
                                        text = formatCurrency(catItem.totalSpent, state.currencySymbol),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRose
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Progress bar for category share
                                LinearProgressIndicator(
                                    progress = { catItem.percentageOfTotal.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = catDef.color,
                                    trackColor = Slate800
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
