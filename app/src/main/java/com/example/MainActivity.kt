package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BudgetAlertDialog
import com.example.ui.components.PresentationScreen
import com.example.ui.components.TransactionBottomSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.BudgetViewModel
import com.example.ui.viewmodel.NavTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BudgetApp()
            }
        }
    }
}

@Composable
fun BudgetApp(
    viewModel: BudgetViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.showPresentation) {
        PresentationScreen(
            onProceed = { viewModel.proceedToDashboard() },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Handle back button to return to Dashboard tab or Presentation
        BackHandler {
            if (state.selectedNavTab != NavTab.DASHBOARD) {
                viewModel.selectNavTab(NavTab.DASHBOARD)
            } else {
                viewModel.openPresentation()
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = Color(0xFF090D16),
            bottomBar = {
                NavigationBar(
                    containerColor = Slate900,
                    contentColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        Triple(NavTab.DASHBOARD, "Dashboard", Icons.Default.Dashboard to Icons.Outlined.Dashboard),
                        Triple(NavTab.ANALYTICS, "Analytics", Icons.Default.BarChart to Icons.Outlined.BarChart),
                        Triple(NavTab.BUDGETS, "Alerts", Icons.Default.NotificationsActive to Icons.Outlined.NotificationsActive)
                    )

                    items.forEach { (tab, label, icons) ->
                        val isSelected = state.selectedNavTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectNavTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) icons.first else icons.second,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = EmeraldPrimary,
                                indicatorColor = EmeraldPrimary,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.openAddTransactionDialog() },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("add_transaction_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Record Transaction"
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (state.selectedNavTab) {
                    NavTab.DASHBOARD -> {
                        DashboardScreen(
                            state = state,
                            onAddTransaction = { viewModel.openAddTransactionDialog() },
                            onEditTransaction = { tx -> viewModel.openAddTransactionDialog(tx) },
                            onOpenPresentation = { viewModel.openPresentation() },
                            onNavigateToAnalytics = { viewModel.selectNavTab(NavTab.ANALYTICS) },
                            onNavigateToBudgets = { viewModel.selectNavTab(NavTab.BUDGETS) },
                            onResetData = { viewModel.resetAllDataToZero() }
                        )
                    }
                    NavTab.ANALYTICS -> {
                        AnalyticsScreen(
                            state = state,
                            onPeriodSelected = { period -> viewModel.setAnalyticsPeriod(period) }
                        )
                    }
                    NavTab.BUDGETS -> {
                        BudgetScreen(
                            state = state,
                            onOpenSetBudget = { viewModel.openSetBudgetDialog() },
                            onDeleteBudget = { cat -> viewModel.deleteBudget(cat) }
                        )
                    }
                }
            }
        }

        // Add/Edit Transaction Bottom Sheet
        if (state.isAddTransactionOpen) {
            TransactionBottomSheet(
                editingTransaction = state.editingTransaction,
                currencySymbol = state.currencySymbol,
                onDismiss = { viewModel.closeAddTransactionDialog() },
                onSave = { type, amount, category, tag, note, timestamp ->
                    viewModel.saveTransaction(type, amount, category, tag, note, timestamp)
                },
                onDelete = { id ->
                    viewModel.deleteTransaction(id)
                    viewModel.closeAddTransactionDialog()
                }
            )
        }

        // Set/Edit Budget Alert Dialog
        if (state.isSetBudgetOpen) {
            BudgetAlertDialog(
                currencySymbol = state.currencySymbol,
                onDismiss = { viewModel.closeSetBudgetDialog() },
                onSave = { category, limit, threshold ->
                    viewModel.saveBudget(category, limit, threshold)
                }
            )
        }
    }
}
