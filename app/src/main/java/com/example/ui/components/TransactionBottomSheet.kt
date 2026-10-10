package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

data class CategoryDef(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

val EXPENSE_CATEGORIES = listOf(
    CategoryDef("Food", Icons.Default.Fastfood, Color(0xFFF97316)),
    CategoryDef("Transport", Icons.Default.DirectionsCar, Color(0xFF0EA5E9)),
    CategoryDef("Shopping", Icons.Default.ShoppingBag, Color(0xFFEC4899)),
    CategoryDef("Bills", Icons.Default.ReceiptLong, Color(0xFF8B5CF6)),
    CategoryDef("Rent", Icons.Default.House, Color(0xFFEAB308)),
    CategoryDef("Entertainment", Icons.Default.SportsEsports, Color(0xFFA855F7)),
    CategoryDef("Health", Icons.Default.LocalHospital, Color(0xFF10B981)),
    CategoryDef("Other", Icons.Default.Category, Color(0xFF64748B))
)

val INCOME_CATEGORIES = listOf(
    CategoryDef("Salary", Icons.Default.BusinessCenter, Color(0xFF10B981)),
    CategoryDef("Freelance", Icons.Default.Laptop, Color(0xFF06B6D4)),
    CategoryDef("Investments", Icons.Default.TrendingUp, Color(0xFF6366F1)),
    CategoryDef("Gifts", Icons.Default.CardGiftcard, Color(0xFFF43F5E)),
    CategoryDef("Other", Icons.Default.Category, Color(0xFF64748B))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBottomSheet(
    editingTransaction: TransactionEntity?,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Double, category: String, tag: String, note: String, timestamp: Long) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.type ?: "EXPENSE")
    }
    var amountString by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.let { "%.2f".format(it.amount).trimEnd('0').trimEnd('.') } ?: "")
    }
    var selectedCategory by remember(editingTransaction, type) {
        mutableStateOf(
            editingTransaction?.category ?: if (type == "EXPENSE") "Food" else "Salary"
        )
    }
    var tagText by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.tag ?: "")
    }
    var noteText by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.note ?: "")
    }

    val availableCategories = if (type == "EXPENSE") EXPENSE_CATEGORIES else INCOME_CATEGORIES

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate900,
        contentColor = Color.White,
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingTransaction != null) "Edit Transaction" else "Record Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Type Selector: Expense vs Income
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800)
                    .padding(4.dp)
            ) {
                // Expense Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (type == "EXPENSE") ExpenseRose else Color.Transparent)
                        .clickable {
                            type = "EXPENSE"
                            if (!EXPENSE_CATEGORIES.any { it.name == selectedCategory }) {
                                selectedCategory = "Food"
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Daily Expense",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "EXPENSE") Color.White else Color(0xFF94A3B8)
                    )
                }

                // Income Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (type == "INCOME") EmeraldPrimary else Color.Transparent)
                        .clickable {
                            type = "INCOME"
                            if (!INCOME_CATEGORIES.any { it.name == selectedCategory }) {
                                selectedCategory = "Salary"
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Daily Income",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "INCOME") Color.Black else Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input Field
            Text(
                text = "Amount ($currencySymbol)",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = amountString,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                        amountString = input
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                placeholder = { Text("0.00", color = Color(0xFF64748B)) },
                leadingIcon = {
                    Text(
                        text = currencySymbol,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "EXPENSE") ExpenseRose else EmeraldPrimary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (type == "EXPENSE") ExpenseRose else EmeraldPrimary,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick increment chips (+100, +500, +1000, +5000)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100, 500, 1000, 2000, 5000).forEach { inc ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate800,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                        modifier = Modifier.clickable {
                            val current = amountString.toDoubleOrNull() ?: 0.0
                            amountString = "%.0f".format(current + inc)
                        }
                    ) {
                        Text(
                            text = "+$currencySymbol$inc",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Selection Chips
            Text(
                text = "Category Tag",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { cat ->
                    val isSelected = selectedCategory == cat.name
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) cat.color.copy(alpha = 0.25f) else Slate800,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) cat.color else Slate700
                        ),
                        modifier = Modifier.clickable { selectedCategory = cat.name }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = cat.name,
                                tint = cat.color,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-tag / Custom Tag
            Text(
                text = "Custom Tag (Optional)",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = tagText,
                onValueChange = { tagText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tag_input"),
                placeholder = { Text("e.g. Groceries, Metro, Bonus, Dinner", color = Color(0xFF64748B)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Note Field
            Text(
                text = "Note / Memo (Optional)",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input"),
                placeholder = { Text("Additional details...", color = Color(0xFF64748B)) },
                maxLines = 2,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save and Delete Buttons
            val amountVal = amountString.toDoubleOrNull() ?: 0.0
            val isValid = amountVal > 0.0

            Button(
                onClick = {
                    if (isValid) {
                        onSave(
                            type,
                            amountVal,
                            selectedCategory,
                            tagText.trim(),
                            noteText.trim(),
                            editingTransaction?.timestamp ?: System.currentTimeMillis()
                        )
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "EXPENSE") ExpenseRose else EmeraldPrimary,
                    contentColor = if (type == "EXPENSE") Color.White else Color.Black
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (editingTransaction != null) "Update Record" else "Save Transaction",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (editingTransaction != null && onDelete != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onDelete(editingTransaction.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_transaction_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRose),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRose.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Delete Transaction", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
