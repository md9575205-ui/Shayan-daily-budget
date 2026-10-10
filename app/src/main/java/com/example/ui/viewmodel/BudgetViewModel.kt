package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BudgetLimitEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class NavTab {
    DASHBOARD,
    ANALYTICS,
    BUDGETS
}

enum class AnalyticsPeriod {
    WEEKLY,
    MONTHLY
}

data class BarComparisonItem(
    val label: String,
    val subLabel: String = "",
    val income: Double,
    val expense: Double,
    val timestamp: Long = 0L
)

data class CategorySpendingItem(
    val category: String,
    val totalSpent: Double,
    val percentageOfTotal: Float,
    val monthlyLimit: Double? = null,
    val alertThresholdPercent: Int? = null,
    val isOverBudget: Boolean = false,
    val isNearBudget: Boolean = false
)

data class BudgetAlertItem(
    val category: String,
    val spent: Double,
    val limit: Double,
    val thresholdPercent: Int,
    val percentUsed: Int,
    val isExceeded: Boolean
)

data class BudgetUiState(
    val showPresentation: Boolean = true,
    val selectedNavTab: NavTab = NavTab.DASHBOARD,
    val analyticsPeriod: AnalyticsPeriod = AnalyticsPeriod.WEEKLY,
    val currencySymbol: String = "₹",
    val transactions: List<TransactionEntity> = emptyList(),
    val budgets: List<BudgetLimitEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val thisMonthIncome: Double = 0.0,
    val thisMonthExpense: Double = 0.0,
    val thisWeekIncome: Double = 0.0,
    val thisWeekExpense: Double = 0.0,
    val weeklyChartBars: List<BarComparisonItem> = emptyList(),
    val monthlyChartBars: List<BarComparisonItem> = emptyList(),
    val weeklyTotalIncome: Double = 0.0,
    val weeklyTotalExpense: Double = 0.0,
    val monthlyTotalIncome: Double = 0.0,
    val monthlyTotalExpense: Double = 0.0,
    val categorySpendings: List<CategorySpendingItem> = emptyList(),
    val budgetAlerts: List<BudgetAlertItem> = emptyList(),
    val isAddTransactionOpen: Boolean = false,
    val isSetBudgetOpen: Boolean = false,
    val editingTransaction: TransactionEntity? = null
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BudgetRepository

    private val _showPresentation = MutableStateFlow(true)
    private val _selectedNavTab = MutableStateFlow(NavTab.DASHBOARD)
    private val _analyticsPeriod = MutableStateFlow(AnalyticsPeriod.WEEKLY)
    private val _isAddTransactionOpen = MutableStateFlow(false)
    private val _isSetBudgetOpen = MutableStateFlow(false)
    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)

    val uiState: StateFlow<BudgetUiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BudgetRepository(db.transactionDao(), db.budgetDao())

        viewModelScope.launch {
            repository.initializeCleanZeroState()
        }

        uiState = combine(
            repository.allTransactions,
            repository.allBudgets,
            _showPresentation,
            _selectedNavTab,
            _analyticsPeriod,
            _isAddTransactionOpen,
            _isSetBudgetOpen,
            _editingTransaction
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val transactions = args[0] as List<TransactionEntity>
            @Suppress("UNCHECKED_CAST")
            val budgets = args[1] as List<BudgetLimitEntity>
            val showPresentation = args[2] as Boolean
            val navTab = args[3] as NavTab
            val period = args[4] as AnalyticsPeriod
            val isAddOpen = args[5] as Boolean
            val isBudgetOpen = args[6] as Boolean
            val editingTx = args[7] as TransactionEntity?

            computeUiState(
                transactions = transactions,
                budgets = budgets,
                showPresentation = showPresentation,
                navTab = navTab,
                period = period,
                isAddOpen = isAddOpen,
                isBudgetOpen = isBudgetOpen,
                editingTx = editingTx
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetUiState()
        )
    }

    private fun computeUiState(
        transactions: List<TransactionEntity>,
        budgets: List<BudgetLimitEntity>,
        showPresentation: Boolean,
        navTab: NavTab,
        period: AnalyticsPeriod,
        isAddOpen: Boolean,
        isBudgetOpen: Boolean,
        editingTx: TransactionEntity?
    ): BudgetUiState {
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        var totalInc = 0.0
        var totalExp = 0.0
        var monthInc = 0.0
        var monthExp = 0.0
        var weekInc = 0.0
        var weekExp = 0.0

        val cal = Calendar.getInstance()

        // 7 days ago timestamp
        val sevenDaysAgo = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Month start timestamp
        val monthStart = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val categoryExpenseMap = mutableMapOf<String, Double>()

        for (tx in transactions) {
            cal.timeInMillis = tx.timestamp
            val isThisMonth = cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear

            if (tx.type == "INCOME") {
                totalInc += tx.amount
                if (isThisMonth) monthInc += tx.amount
                if (tx.timestamp >= sevenDaysAgo) weekInc += tx.amount
            } else {
                totalExp += tx.amount
                if (isThisMonth) {
                    monthExp += tx.amount
                    categoryExpenseMap[tx.category] = (categoryExpenseMap[tx.category] ?: 0.0) + tx.amount
                }
                if (tx.timestamp >= sevenDaysAgo) weekExp += tx.amount
            }
        }

        // Compute Weekly Chart Data (Last 7 days, daily comparison)
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val weeklyBars = mutableListOf<BarComparisonItem>()
        var weeklyTotalIncSum = 0.0
        var weeklyTotalExpSum = 0.0

        for (i in 6 downTo 0) {
            val dayStart = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayEnd = (dayStart.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            var dayInc = 0.0
            var dayExp = 0.0
            for (tx in transactions) {
                if (tx.timestamp in dayStart.timeInMillis..dayEnd.timeInMillis) {
                    if (tx.type == "INCOME") dayInc += tx.amount else dayExp += tx.amount
                }
            }

            weeklyTotalIncSum += dayInc
            weeklyTotalExpSum += dayExp

            val label = if (i == 0) "Today" else dayFormat.format(dayStart.time)
            val subLabel = dateFormat.format(dayStart.time)
            weeklyBars.add(
                BarComparisonItem(
                    label = label,
                    subLabel = subLabel,
                    income = dayInc,
                    expense = dayExp,
                    timestamp = dayStart.timeInMillis
                )
            )
        }

        // Compute Monthly Chart Data (4 weeks breakdown for the last 28 days)
        val monthlyBars = mutableListOf<BarComparisonItem>()
        var monthlyTotalIncSum = 0.0
        var monthlyTotalExpSum = 0.0

        for (w in 3 downTo 0) {
            val weekStart = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -(w * 7 + 6))
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val weekEnd = (weekStart.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 6)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            var wInc = 0.0
            var wExp = 0.0
            for (tx in transactions) {
                if (tx.timestamp in weekStart.timeInMillis..weekEnd.timeInMillis) {
                    if (tx.type == "INCOME") wInc += tx.amount else wExp += tx.amount
                }
            }
            monthlyTotalIncSum += wInc
            monthlyTotalExpSum += wExp

            val label = "Wk ${4 - w}"
            val subLabel = "${dateFormat.format(weekStart.time)} - ${dateFormat.format(weekEnd.time)}"
            monthlyBars.add(
                BarComparisonItem(
                    label = label,
                    subLabel = subLabel,
                    income = wInc,
                    expense = wExp,
                    timestamp = weekStart.timeInMillis
                )
            )
        }

        // Compute Category Spending Items & Percentage
        val budgetMap = budgets.associateBy { it.category }
        val categoryItems = categoryExpenseMap.entries.map { (cat, spent) ->
            val budget = budgetMap[cat]
            val pctOfTotal = if (monthExp > 0) (spent / monthExp).toFloat() else 0f
            val isOver = budget != null && spent > budget.monthlyLimit
            val isNear = budget != null && spent >= (budget.monthlyLimit * (budget.alertThresholdPercent / 100.0))
            CategorySpendingItem(
                category = cat,
                totalSpent = spent,
                percentageOfTotal = pctOfTotal,
                monthlyLimit = budget?.monthlyLimit,
                alertThresholdPercent = budget?.alertThresholdPercent,
                isOverBudget = isOver,
                isNearBudget = isNear && !isOver
            )
        }.sortedByDescending { it.totalSpent }

        // Compute Budget Alerts
        val alerts = mutableListOf<BudgetAlertItem>()
        for (budget in budgets) {
            val spent = categoryExpenseMap[budget.category] ?: 0.0
            val limit = budget.monthlyLimit
            val threshold = budget.alertThresholdPercent
            val pct = if (limit > 0) ((spent / limit) * 100).toInt() else 0

            if (spent >= limit) {
                alerts.add(
                    BudgetAlertItem(
                        category = budget.category,
                        spent = spent,
                        limit = limit,
                        thresholdPercent = threshold,
                        percentUsed = pct,
                        isExceeded = true
                    )
                )
            } else if (pct >= threshold) {
                alerts.add(
                    BudgetAlertItem(
                        category = budget.category,
                        spent = spent,
                        limit = limit,
                        thresholdPercent = threshold,
                        percentUsed = pct,
                        isExceeded = false
                    )
                )
            }
        }

        return BudgetUiState(
            showPresentation = showPresentation,
            selectedNavTab = navTab,
            analyticsPeriod = period,
            currencySymbol = "₹",
            transactions = transactions,
            budgets = budgets,
            totalIncome = totalInc,
            totalExpense = totalExp,
            netBalance = totalInc - totalExp,
            thisMonthIncome = monthInc,
            thisMonthExpense = monthExp,
            thisWeekIncome = weekInc,
            thisWeekExpense = weekExp,
            weeklyChartBars = weeklyBars,
            monthlyChartBars = monthlyBars,
            weeklyTotalIncome = weeklyTotalIncSum,
            weeklyTotalExpense = weeklyTotalExpSum,
            monthlyTotalIncome = monthlyTotalIncSum,
            monthlyTotalExpense = monthlyTotalExpSum,
            categorySpendings = categoryItems,
            budgetAlerts = alerts,
            isAddTransactionOpen = isAddOpen,
            isSetBudgetOpen = isBudgetOpen,
            editingTransaction = editingTx
        )
    }

    // Actions
    fun proceedToDashboard() {
        _showPresentation.value = false
    }

    fun openPresentation() {
        _showPresentation.value = true
    }

    fun selectNavTab(tab: NavTab) {
        _selectedNavTab.value = tab
    }

    fun setAnalyticsPeriod(period: AnalyticsPeriod) {
        _analyticsPeriod.value = period
    }

    fun openAddTransactionDialog(transaction: TransactionEntity? = null) {
        _editingTransaction.value = transaction
        _isAddTransactionOpen.value = true
    }

    fun closeAddTransactionDialog() {
        _isAddTransactionOpen.value = false
        _editingTransaction.value = null
    }

    fun saveTransaction(
        type: String,
        amount: Double,
        category: String,
        tag: String,
        note: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val currentEditing = _editingTransaction.value
            if (currentEditing != null) {
                repository.updateTransaction(
                    currentEditing.copy(
                        type = type,
                        amount = amount,
                        category = category,
                        tag = tag,
                        note = note,
                        timestamp = timestamp
                    )
                )
            } else {
                repository.insertTransaction(
                    TransactionEntity(
                        type = type,
                        amount = amount,
                        category = category,
                        tag = tag,
                        note = note,
                        timestamp = timestamp
                    )
                )
            }
            closeAddTransactionDialog()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun openSetBudgetDialog() {
        _isSetBudgetOpen.value = true
    }

    fun closeSetBudgetDialog() {
        _isSetBudgetOpen.value = false
    }

    fun saveBudget(category: String, monthlyLimit: Double, alertThresholdPercent: Int) {
        viewModelScope.launch {
            repository.setBudget(
                BudgetLimitEntity(
                    category = category,
                    monthlyLimit = monthlyLimit,
                    alertThresholdPercent = alertThresholdPercent
                )
            )
            closeSetBudgetDialog()
        }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            repository.deleteBudgetByCategory(category)
        }
    }

    fun resetAllDataToZero() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}

fun formatCurrency(amount: Double, symbol: String = "₹"): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return "$symbol${formatter.format(amount)}"
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
