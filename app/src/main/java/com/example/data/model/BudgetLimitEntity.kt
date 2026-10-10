package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_limits")
data class BudgetLimitEntity(
    @PrimaryKey
    val category: String,
    val monthlyLimit: Double,
    val alertThresholdPercent: Int = 80 // Triggers warning when spending reaches this %
)
