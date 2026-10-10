package com.example.data.repository

import com.example.data.local.BudgetDao
import com.example.data.local.TransactionDao
import com.example.data.model.BudgetLimitEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class BudgetRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetLimitEntity>> = budgetDao.getAllBudgets()

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun setBudget(budget: BudgetLimitEntity) {
        budgetDao.setBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetLimitEntity) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun deleteBudgetByCategory(category: String) {
        budgetDao.deleteByCategory(category)
    }

    suspend fun clearAllTransactions() {
        transactionDao.clearAll()
    }

    suspend fun clearAllData() {
        transactionDao.clearAll()
    }

    /**
     * Initializes a clean zero-state for all financial inputs and transactions.
     * All transactions start at 0.
     */
    suspend fun initializeCleanZeroState() {
        transactionDao.clearAll()
    }
}
