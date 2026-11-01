package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Locale

@Composable
fun BudgetScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.allExpenses.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()

    val monthlySpent = remember(expenses) { viewModel.calculateCurrentMonthExpenses(expenses) }
    val remainingBudget = remember(monthlyBudget, monthlySpent) { (monthlyBudget - monthlySpent).coerceAtLeast(0.0) }
    val budgetProgress = remember(monthlyBudget, monthlySpent) {
        if (monthlyBudget > 0) (monthlySpent / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
    }
    val categorySpends = remember(expenses) { viewModel.calculateCategoryBreakdown(expenses) }

    var showEditDialog by remember { mutableStateOf(false) }
    var newBudgetString by remember { mutableStateOf("") }

    val presetBudgets = listOf(1500.0, 2000.0, 2500.0, 2800.0, 3500.0, 5000.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateSurface)
            .testTag("budget_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Budget & Targets",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                Text(
                    text = "Set limits and track monthly allowances",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
            }
        }

        // Active Monthly Budget Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Budget Limit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark
                        )

                        FilledTonalButton(
                            onClick = {
                                newBudgetString = monthlyBudget.toInt().toString()
                                showEditDialog = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = TealBackgroundTint,
                                contentColor = TealPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("change_budget_button")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = String.format(Locale.US, "$%,.2f", monthlyBudget),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar
                    val animatedProgress by animateFloatAsState(
                        targetValue = budgetProgress,
                        label = "budgetPageProgress"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFECEEF0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (budgetProgress > 0.9f) RoseNegative else TealPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Spent this month", fontSize = 11.sp, color = SlateMuted)
                            Text(
                                text = String.format(Locale.US, "$%,.2f", monthlySpent),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Remaining balance", fontSize = 11.sp, color = SlateMuted)
                            Text(
                                text = String.format(Locale.US, "$%,.2f", remainingBudget),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBudget > 0) TealPrimary else RoseNegative
                            )
                        }
                    }
                }
            }
        }

        // Quick Preset Budget Options
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Quick Budget Presets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetBudgets.take(3).forEach { amount ->
                            val isSelected = (monthlyBudget == amount)
                            OutlinedButton(
                                onClick = { viewModel.setBudget(amount) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) TealPrimary else PureWhite,
                                    contentColor = if (isSelected) PureWhite else SlateDark
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) TealPrimary else SlateBorder
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "$${amount.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetBudgets.drop(3).forEach { amount ->
                            val isSelected = (monthlyBudget == amount)
                            OutlinedButton(
                                onClick = { viewModel.setBudget(amount) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) TealPrimary else PureWhite,
                                    contentColor = if (isSelected) PureWhite else SlateDark
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) TealPrimary else SlateBorder
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "$${amount.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Budget Spending
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Category Budget Usage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (categorySpends.isEmpty()) {
                        Text(
                            text = "No expenses logged to calculate category allocations",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            categorySpends.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(item.category.backgroundColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                item.category.icon,
                                                contentDescription = null,
                                                tint = item.category.textColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = item.category.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SlateDark
                                        )
                                    }

                                    Text(
                                        text = String.format(Locale.US, "$%,.2f", item.amount),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Budget Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Set Monthly Budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your desired target budget for the month:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newBudgetString,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                newBudgetString = it
                            }
                        },
                        leadingIcon = {
                            Text(
                                "$",
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                fontSize = 18.sp
                            )
                        },
                        placeholder = { Text("2800") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_input_dialog"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newBudgetString.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.setBudget(amount)
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    modifier = Modifier.testTag("confirm_budget_button")
                ) {
                    Text("Save Target")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }
}
