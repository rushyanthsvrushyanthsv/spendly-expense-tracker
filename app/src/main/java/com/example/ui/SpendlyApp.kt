package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel

enum class SpendlyNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_dashboard"),
    EXPENSES("Expenses", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "tab_expenses"),
    STATISTICS("Analytics", Icons.Filled.PieChart, Icons.Outlined.PieChart, "tab_statistics"),
    BUDGET("Budget", Icons.Filled.Savings, Icons.Outlined.Savings, "tab_budget")
}

@Composable
fun SpendlyApp(
    viewModel: ExpenseViewModel
) {
    var currentTab by remember { mutableStateOf(SpendlyNavTab.DASHBOARD) }
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var selectedExpenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }

    // Android back navigation handler
    BackHandler(enabled = currentTab != SpendlyNavTab.DASHBOARD) {
        currentTab = SpendlyNavTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SlateSurface,
        bottomBar = {
            // Modern Floating Bottom Bar with center Quick-Add Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(26.dp), spotColor = SlateDark.copy(alpha = 0.12f))
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(26.dp)),
                    color = PureWhite
                ) {
                    NavigationBar(
                        containerColor = PureWhite,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(72.dp)
                    ) {
                        // Home Tab
                        NavigationBarItem(
                            selected = currentTab == SpendlyNavTab.DASHBOARD,
                            onClick = { currentTab = SpendlyNavTab.DASHBOARD },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == SpendlyNavTab.DASHBOARD) SpendlyNavTab.DASHBOARD.selectedIcon else SpendlyNavTab.DASHBOARD.unselectedIcon,
                                    contentDescription = "Home",
                                    tint = if (currentTab == SpendlyNavTab.DASHBOARD) TealPrimary else SlateMuted
                                )
                            },
                            label = {
                                Text(
                                    text = "Home",
                                    fontSize = 11.sp,
                                    fontWeight = if (currentTab == SpendlyNavTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTab == SpendlyNavTab.DASHBOARD) TealPrimary else SlateMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = TealBackgroundTint
                            ),
                            modifier = Modifier.testTag(SpendlyNavTab.DASHBOARD.testTag)
                        )

                        // Expenses Tab
                        NavigationBarItem(
                            selected = currentTab == SpendlyNavTab.EXPENSES,
                            onClick = { currentTab = SpendlyNavTab.EXPENSES },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == SpendlyNavTab.EXPENSES) SpendlyNavTab.EXPENSES.selectedIcon else SpendlyNavTab.EXPENSES.unselectedIcon,
                                    contentDescription = "Expenses",
                                    tint = if (currentTab == SpendlyNavTab.EXPENSES) TealPrimary else SlateMuted
                                )
                            },
                            label = {
                                Text(
                                    text = "Expenses",
                                    fontSize = 11.sp,
                                    fontWeight = if (currentTab == SpendlyNavTab.EXPENSES) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTab == SpendlyNavTab.EXPENSES) TealPrimary else SlateMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = TealBackgroundTint
                            ),
                            modifier = Modifier.testTag(SpendlyNavTab.EXPENSES.testTag)
                        )

                        // Quick Add Action Button (Center item)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    selectedExpenseToEdit = null
                                    showAddExpenseSheet = true
                                },
                                shape = CircleShape,
                                containerColor = TealPrimary,
                                contentColor = PureWhite,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                                modifier = Modifier
                                    .size(50.dp)
                                    .testTag("floating_quick_add_button")
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Add Expense",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Statistics Tab
                        NavigationBarItem(
                            selected = currentTab == SpendlyNavTab.STATISTICS,
                            onClick = { currentTab = SpendlyNavTab.STATISTICS },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == SpendlyNavTab.STATISTICS) SpendlyNavTab.STATISTICS.selectedIcon else SpendlyNavTab.STATISTICS.unselectedIcon,
                                    contentDescription = "Analytics",
                                    tint = if (currentTab == SpendlyNavTab.STATISTICS) TealPrimary else SlateMuted
                                )
                            },
                            label = {
                                Text(
                                    text = "Analytics",
                                    fontSize = 11.sp,
                                    fontWeight = if (currentTab == SpendlyNavTab.STATISTICS) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTab == SpendlyNavTab.STATISTICS) TealPrimary else SlateMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = TealBackgroundTint
                            ),
                            modifier = Modifier.testTag(SpendlyNavTab.STATISTICS.testTag)
                        )

                        // Budget Tab
                        NavigationBarItem(
                            selected = currentTab == SpendlyNavTab.BUDGET,
                            onClick = { currentTab = SpendlyNavTab.BUDGET },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == SpendlyNavTab.BUDGET) SpendlyNavTab.BUDGET.selectedIcon else SpendlyNavTab.BUDGET.unselectedIcon,
                                    contentDescription = "Budget",
                                    tint = if (currentTab == SpendlyNavTab.BUDGET) TealPrimary else SlateMuted
                                )
                            },
                            label = {
                                Text(
                                    text = "Budget",
                                    fontSize = 11.sp,
                                    fontWeight = if (currentTab == SpendlyNavTab.BUDGET) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTab == SpendlyNavTab.BUDGET) TealPrimary else SlateMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = TealBackgroundTint
                            ),
                            modifier = Modifier.testTag(SpendlyNavTab.BUDGET.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                SpendlyNavTab.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToExpenses = { currentTab = SpendlyNavTab.EXPENSES },
                        onOpenAddExpense = {
                            selectedExpenseToEdit = null
                            showAddExpenseSheet = true
                        },
                        onOpenSetBudget = { currentTab = SpendlyNavTab.BUDGET },
                        onSelectExpense = { expense ->
                            selectedExpenseToEdit = expense
                            showAddExpenseSheet = true
                        }
                    )
                }

                SpendlyNavTab.EXPENSES -> {
                    ExpensesScreen(
                        viewModel = viewModel,
                        onOpenAddExpense = {
                            selectedExpenseToEdit = null
                            showAddExpenseSheet = true
                        },
                        onSelectExpense = { expense ->
                            selectedExpenseToEdit = expense
                            showAddExpenseSheet = true
                        }
                    )
                }

                SpendlyNavTab.STATISTICS -> {
                    StatisticsScreen(
                        viewModel = viewModel
                    )
                }

                SpendlyNavTab.BUDGET -> {
                    BudgetScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Add or Edit Expense Sheet
    if (showAddExpenseSheet) {
        AddExpenseSheet(
            initialExpense = selectedExpenseToEdit,
            onDismiss = {
                showAddExpenseSheet = false
                selectedExpenseToEdit = null
            },
            onSave = { title, amount, isIncome, category, paymentMethod, dateMillis, note ->
                val editing = selectedExpenseToEdit
                if (editing != null) {
                    viewModel.updateExpense(
                        editing.copy(
                            title = title,
                            amount = amount,
                            isIncome = isIncome,
                            category = category.name,
                            paymentMethod = paymentMethod,
                            dateMillis = dateMillis,
                            note = note
                        )
                    )
                } else {
                    viewModel.addExpense(
                        title = title,
                        amount = amount,
                        isIncome = isIncome,
                        category = category,
                        paymentMethod = paymentMethod,
                        dateMillis = dateMillis,
                        note = note
                    )
                }
                showAddExpenseSheet = false
                selectedExpenseToEdit = null
            },
            onDelete = selectedExpenseToEdit?.let { expense ->
                {
                    viewModel.deleteExpense(expense)
                    showAddExpenseSheet = false
                    selectedExpenseToEdit = null
                }
            }
        )
    }
}
