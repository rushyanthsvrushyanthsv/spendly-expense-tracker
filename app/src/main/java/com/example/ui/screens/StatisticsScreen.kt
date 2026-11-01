package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.SpendingBarChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Calendar
import java.util.Locale

enum class StatsPeriod(val label: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

@Composable
fun StatisticsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.THIS_MONTH) }

    // Filter expenses based on selectedPeriod
    val periodExpenses = remember(allExpenses, selectedPeriod) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        allExpenses.filter { item ->
            when (selectedPeriod) {
                StatsPeriod.ALL_TIME -> true
                StatsPeriod.THIS_WEEK -> {
                    val oneWeekAgo = now - 7L * 24 * 60 * 60 * 1000
                    item.dateMillis >= oneWeekAgo
                }
                StatsPeriod.THIS_MONTH -> {
                    val itemCal = Calendar.getInstance().apply { timeInMillis = item.dateMillis }
                    cal.timeInMillis = now
                    itemCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                            itemCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                }
            }
        }
    }

    val totalExpense = remember(periodExpenses) {
        periodExpenses.filter { !it.isIncome }.sumOf { it.amount }
    }
    val totalIncome = remember(periodExpenses) {
        periodExpenses.filter { it.isIncome }.sumOf { it.amount }
    }
    val categorySpends = remember(periodExpenses) {
        viewModel.calculateCategoryBreakdown(periodExpenses)
    }
    val dailySpendData = remember(allExpenses) {
        viewModel.calculateDailySpendingLast7Days(allExpenses)
    }

    val topCategory = categorySpends.firstOrNull()

    val averageDailySpend = remember(totalExpense, selectedPeriod) {
        val days = when (selectedPeriod) {
            StatsPeriod.THIS_WEEK -> 7
            StatsPeriod.THIS_MONTH -> Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
            StatsPeriod.ALL_TIME -> 30
        }
        if (days > 0) totalExpense / days else 0.0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateSurface)
            .testTag("statistics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Analytics & Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                Text(
                    text = "Track patterns & category breakdowns",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
            }
        }

        // Period Segmented Control (Pill-shaped)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp)
            ) {
                StatsPeriod.entries.forEach { period ->
                    val isSelected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PureWhite else Color.Transparent)
                            .clickable { selectedPeriod = period }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SlateDark else SlateMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Summary Metric Cards Grid (2x2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Spent Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(RoseNegativeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = RoseNegative, modifier = Modifier.size(14.dp))
                            }
                            Text(text = "Total Spent", fontSize = 11.sp, color = SlateMuted)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(Locale.US, "$%,.2f", totalExpense),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                    }
                }

                // Daily Average Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(TealBackgroundTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ShowChart, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(14.dp))
                            }
                            Text(text = "Daily Avg", fontSize = 11.sp, color = SlateMuted)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(Locale.US, "$%,.2f", averageDailySpend),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }
            }
        }

        // Spending by Category (Donut Chart + Summary)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spending by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )

                        Text(
                            text = "${categorySpends.size} categories",
                            fontSize = 12.sp,
                            color = SlateMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    CategoryDonutChart(
                        categorySpends = categorySpends,
                        totalExpense = totalExpense
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Category Breakdown Items
                    if (categorySpends.isEmpty()) {
                        Text(
                            text = "No expenses recorded for this period",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categorySpends.forEach { item ->
                                Column(modifier = Modifier.fillMaxWidth()) {
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
                                            Column {
                                                Text(
                                                    text = item.category.displayName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = SlateDark
                                                )
                                                Text(
                                                    text = "${item.count} transactions",
                                                    fontSize = 10.sp,
                                                    color = SlateMuted
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = String.format(Locale.US, "$%,.2f", item.amount),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SlateDark
                                            )
                                            Text(
                                                text = "${(item.percentage * 100).toInt()}%",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = item.category.textColor
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Category progress bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFF1F5F9))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(item.percentage.coerceIn(0.01f, 1f))
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(item.category.textColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Weekly Trends Chart Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Daily Spending Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Last 7 days spending distribution",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SpendingBarChart(dailyData = dailySpendData)
                }
            }
        }

        // Spending Insights Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateDark),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFACC15),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Smart Financial Insight",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (topCategory != null) {
                        Text(
                            text = "${topCategory.category.displayName} is your largest expense category (${(topCategory.percentage * 100).toInt()}% of spending). Keeping meals cooked at home could save up to $150 this month.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 20.sp
                        )
                    } else {
                        Text(
                            text = "Log transactions consistently to unlock personalized spending tips and budget forecasts.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
