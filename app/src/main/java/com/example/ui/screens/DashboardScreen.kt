package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.ui.components.SpendingBarChart
import com.example.ui.components.TransactionItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onNavigateToExpenses: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenSetBudget: () -> Unit,
    onSelectExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.allExpenses.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()

    val totalBalance = remember(expenses) { viewModel.calculateTotalBalance(expenses) }
    val monthlySpent = remember(expenses) { viewModel.calculateCurrentMonthExpenses(expenses) }
    val monthlyIncome = remember(expenses) { viewModel.calculateCurrentMonthIncome(expenses) }
    val remainingBudget = remember(monthlyBudget, monthlySpent) { (monthlyBudget - monthlySpent).coerceAtLeast(0.0) }
    val budgetProgress = remember(monthlyBudget, monthlySpent) {
        if (monthlyBudget > 0) (monthlySpent / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
    }
    val dailySpendData = remember(expenses) { viewModel.calculateDailySpendingLast7Days(expenses) }

    val recentExpenses = remember(expenses) {
        expenses.take(4)
    }

    val currentMonthYear = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateSurface)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Spendly",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentMonthYear,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                }

                // Quick Add Mini Pill
                FilledTonalButton(
                    onClick = onOpenAddExpense,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = TealPrimary.copy(alpha = 0.1f),
                        contentColor = TealPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("quick_add_expense_header_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Hero Card: Total Balance & Cashflow
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("hero_balance_card"),
                colors = CardDefaults.cardColors(containerColor = SlateDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                            )
                        )
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF334155))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "USD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = String.format(Locale.US, "$%,.2f", totalBalance),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider(color = Color(0xFF334155), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Income vs Expense Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Monthly Income
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPositive.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.ArrowDownward,
                                    contentDescription = "Income",
                                    tint = EmeraldPositive,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Monthly Income",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = String.format(Locale.US, "+$%,.2f", monthlyIncome),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPositive
                                )
                            }
                        }

                        // Monthly Spending
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RoseNegative.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.ArrowUpward,
                                    contentDescription = "Expenses",
                                    tint = Color(0xFFFB7185),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Monthly Spent",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = String.format(Locale.US, "-$%,.2f", monthlySpent),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Budget Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
                    .testTag("budget_progress_card"),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TealBackgroundTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Savings,
                                    contentDescription = "Budget",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Monthly Budget",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateDark
                            )
                        }

                        // Set / Edit Budget Link
                        Text(
                            text = "Edit Goal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier
                                .clickable { onOpenSetBudget() }
                                .padding(4.dp)
                                .testTag("edit_budget_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Remaining Budget",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.US, "$%,.2f", remainingBudget),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBudget > 0) TealPrimary else RoseNegative
                            )
                        }

                        // Percentage spent badge
                        val percentInt = (budgetProgress * 100).toInt()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (percentInt > 90) RoseNegativeBg else TealBackgroundTint
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$percentInt% used of $${monthlyBudget.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (percentInt > 90) RoseNegativeText else TealPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Animated Progress Bar
                    val animatedProgress by animateFloatAsState(
                        targetValue = budgetProgress,
                        label = "budgetProgressBar"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFECEEF0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (budgetProgress > 0.9f) RoseNegative else TealPrimary
                                )
                        )
                    }
                }
            }
        }

        // Spending Trend Chart Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
                    .testTag("spending_chart_card"),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        Text(
                            text = "Spending This Week",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark
                        )

                        val weekTotal = remember(dailySpendData) { dailySpendData.sumOf { it.amount } }
                        Text(
                            text = String.format(Locale.US, "$%,.2f total", weekTotal),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SpendingBarChart(dailyData = dailySpendData)
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )

                TextButton(
                    onClick = onNavigateToExpenses,
                    modifier = Modifier.testTag("see_all_transactions_button")
                ) {
                    Text(
                        text = "See All",
                        color = TealPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = "See All",
                        tint = TealPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Recent Transactions List
        if (recentExpenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PureWhite)
                        .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = SlateLight,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No expenses logged yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = SlateDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Add' to track your first expense",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
            }
        } else {
            items(recentExpenses, key = { it.id }) { expense ->
                TransactionItem(
                    expense = expense,
                    onClick = { onSelectExpense(expense) },
                    onDelete = { viewModel.deleteExpense(expense) }
                )
            }
        }
    }
}
