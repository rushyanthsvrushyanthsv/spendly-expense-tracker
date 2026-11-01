package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import com.example.ui.model.DateFilter
import com.example.ui.model.ExpenseCategory
import com.example.ui.model.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailySpend(
    val dayLabel: String,
    val dateMillis: Long,
    val amount: Double,
    val isToday: Boolean
)

data class CategorySpend(
    val category: ExpenseCategory,
    val amount: Double,
    val percentage: Float,
    val count: Int
)

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _monthlyBudget = MutableStateFlow(repository.getMonthlyBudget())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<ExpenseCategory?>(null)
    val selectedDateFilter = MutableStateFlow(DateFilter.ALL)
    val selectedSortOrder = MutableStateFlow(SortOrder.DATE_DESC)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses,
        searchQuery,
        selectedCategory,
        selectedDateFilter,
        selectedSortOrder
    ) { expenses, query, cat, dateFilter, sortOrder ->
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        expenses.filter { item ->
            // Search query filter
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.note.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true) ||
                    item.paymentMethod.contains(query, ignoreCase = true)

            // Category filter
            val matchesCategory = cat == null ||
                    item.category.equals(cat.name, ignoreCase = true)

            // Date filter
            val matchesDate = when (dateFilter) {
                DateFilter.ALL -> true
                DateFilter.TODAY -> {
                    val itemCal = Calendar.getInstance().apply { timeInMillis = item.dateMillis }
                    cal.timeInMillis = now
                    itemCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                            itemCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
                }
                DateFilter.THIS_WEEK -> {
                    val oneWeekAgo = now - 7L * 24 * 60 * 60 * 1000
                    item.dateMillis >= oneWeekAgo
                }
                DateFilter.THIS_MONTH -> {
                    val itemCal = Calendar.getInstance().apply { timeInMillis = item.dateMillis }
                    cal.timeInMillis = now
                    itemCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                            itemCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                }
            }

            matchesQuery && matchesCategory && matchesDate
        }.let { list ->
            when (sortOrder) {
                SortOrder.DATE_DESC -> list.sortedByDescending { it.dateMillis }
                SortOrder.DATE_ASC -> list.sortedBy { it.dateMillis }
                SortOrder.AMOUNT_DESC -> list.sortedByDescending { it.amount }
                SortOrder.AMOUNT_ASC -> list.sortedBy { it.amount }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addExpense(
        title: String,
        amount: Double,
        isIncome: Boolean,
        category: ExpenseCategory,
        paymentMethod: String,
        dateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    title = title.trim(),
                    amount = amount,
                    isIncome = isIncome,
                    category = category.name,
                    paymentMethod = paymentMethod,
                    dateMillis = dateMillis,
                    note = note.trim()
                )
            )
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun deleteExpenseById(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun setBudget(amount: Double) {
        _monthlyBudget.value = amount
        repository.setMonthlyBudget(amount)
    }

    // Helper calculations for UI
    fun calculateCurrentMonthExpenses(expenses: List<ExpenseEntity>): Double {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val itemCal = Calendar.getInstance()
        return expenses.filter {
            !it.isIncome && run {
                itemCal.timeInMillis = it.dateMillis
                itemCal.get(Calendar.MONTH) == currentMonth && itemCal.get(Calendar.YEAR) == currentYear
            }
        }.sumOf { it.amount }
    }

    fun calculateCurrentMonthIncome(expenses: List<ExpenseEntity>): Double {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val itemCal = Calendar.getInstance()
        return expenses.filter {
            it.isIncome && run {
                itemCal.timeInMillis = it.dateMillis
                itemCal.get(Calendar.MONTH) == currentMonth && itemCal.get(Calendar.YEAR) == currentYear
            }
        }.sumOf { it.amount }
    }

    fun calculateTotalBalance(expenses: List<ExpenseEntity>): Double {
        val totalIncome = expenses.filter { it.isIncome }.sumOf { it.amount }
        val totalExpense = expenses.filter { !it.isIncome }.sumOf { it.amount }
        return totalIncome - totalExpense
    }

    fun calculateDailySpendingLast7Days(expenses: List<ExpenseEntity>): List<DailySpend> {
        val result = mutableListOf<DailySpend>()
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 6 downTo 0) {
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis

            val isToday = (i == 0)
            val label = if (isToday) "Today" else dayFormat.format(Date(startOfDay))

            val dayTotal = expenses.filter {
                !it.isIncome && it.dateMillis in startOfDay..endOfDay
            }.sumOf { it.amount }

            result.add(
                DailySpend(
                    dayLabel = label,
                    dateMillis = startOfDay,
                    amount = dayTotal,
                    isToday = isToday
                )
            )
        }
        return result
    }

    fun calculateCategoryBreakdown(expenses: List<ExpenseEntity>): List<CategorySpend> {
        val expenseItems = expenses.filter { !it.isIncome }
        val totalExpense = expenseItems.sumOf { it.amount }
        if (totalExpense <= 0.0) return emptyList()

        return expenseItems.groupBy { ExpenseCategory.fromString(it.category) }
            .map { (cat, items) ->
                val sum = items.sumOf { it.amount }
                CategorySpend(
                    category = cat,
                    amount = sum,
                    percentage = (sum / totalExpense).toFloat(),
                    count = items.size
                )
            }
            .sortedByDescending { it.amount }
    }
}

class ExpenseViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpenseViewModel(repository) as T
    }
}
