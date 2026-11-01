package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.ui.components.TransactionItem
import com.example.ui.model.DateFilter
import com.example.ui.model.ExpenseCategory
import com.example.ui.model.SortOrder
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpenseViewModel,
    onOpenAddExpense: () -> Unit,
    onSelectExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredExpenses by viewModel.filteredExpenses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()
    val selectedSortOrder by viewModel.selectedSortOrder.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    val totalSpentInView = remember(filteredExpenses) {
        filteredExpenses.filter { !it.isIncome }.sumOf { it.amount }
    }
    val totalIncomeInView = remember(filteredExpenses) {
        filteredExpenses.filter { it.isIncome }.sumOf { it.amount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateSurface)
            .testTag("expenses_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Title & Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    Text(
                        text = "${filteredExpenses.size} items listed",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                }

                // Sort Dropdown Trigger
                Box {
                    OutlinedButton(
                        onClick = { showSortMenu = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = PureWhite,
                            contentColor = SlateDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("sort_button")
                    ) {
                        Icon(
                            Icons.Filled.Sort,
                            contentDescription = "Sort",
                            modifier = Modifier.size(16.dp),
                            tint = TealPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedSortOrder.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        order.label,
                                        fontWeight = if (selectedSortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedSortOrder == order) TealPrimary else SlateDark
                                    )
                                },
                                onClick = {
                                    viewModel.selectedSortOrder.value = order
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search by merchant, note, or tag...") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = SlateMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search", tint = SlateMuted)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transactions_search_bar"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                )
            )
        }

        // Date Filter Pills (All Time, This Month, This Week, Today)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DateFilter.entries) { filter ->
                    val isSelected = selectedDateFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SlateDark else PureWhite)
                            .border(1.dp, if (isSelected) SlateDark else SlateBorder, RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectedDateFilter.value = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("date_filter_${filter.name}")
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PureWhite else SlateMuted
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // "All Categories" chip
                item {
                    val isAllSelected = selectedCategory == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isAllSelected) TealPrimary else PureWhite)
                            .border(1.dp, if (isAllSelected) TealPrimary else SlateBorder, RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectedCategory.value = null }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("category_filter_all")
                    ) {
                        Text(
                            text = "All Categories",
                            fontSize = 12.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAllSelected) PureWhite else SlateMuted
                        )
                    }
                }

                items(ExpenseCategory.entries) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) category.backgroundColor else PureWhite)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) category.textColor else SlateBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                viewModel.selectedCategory.value = if (isSelected) null else category
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("category_filter_${category.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                category.icon,
                                contentDescription = null,
                                tint = if (isSelected) category.textColor else SlateMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = category.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) category.textColor else SlateDark
                            )
                        }
                    }
                }
            }
        }

        // Filter Summary Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = TealBackgroundTint),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Filtered Spending",
                            fontSize = 11.sp,
                            color = SlateMuted
                        )
                        Text(
                            text = String.format(Locale.US, "$%,.2f", totalSpentInView),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                    }

                    if (totalIncomeInView > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Filtered Income",
                                fontSize = 11.sp,
                                color = SlateMuted
                            )
                            Text(
                                text = String.format(Locale.US, "+$%,.2f", totalIncomeInView),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPositiveText
                            )
                        }
                    }
                }
            }
        }

        // Transactions List
        if (filteredExpenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.SearchOff,
                                contentDescription = null,
                                tint = SlateLight,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your filters or search keywords",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = {
                                viewModel.searchQuery.value = ""
                                viewModel.selectedCategory.value = null
                                viewModel.selectedDateFilter.value = DateFilter.ALL
                            }
                        ) {
                            Text("Reset All Filters", color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredExpenses, key = { it.id }) { expense ->
                TransactionItem(
                    expense = expense,
                    onClick = { onSelectExpense(expense) },
                    onDelete = { viewModel.deleteExpense(expense) }
                )
            }
        }
    }
}
