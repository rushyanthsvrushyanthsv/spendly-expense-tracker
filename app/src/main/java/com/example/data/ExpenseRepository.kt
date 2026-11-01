package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.model.ExpenseCategory
import com.example.ui.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("spendly_prefs", Context.MODE_PRIVATE)

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteById(id: Long) {
        expenseDao.deleteById(id)
    }

    fun getMonthlyBudget(): Double {
        return prefs.getFloat("monthly_budget", 2800.0f).toDouble()
    }

    fun setMonthlyBudget(amount: Double) {
        prefs.edit().putFloat("monthly_budget", amount.toFloat()).apply()
    }

    suspend fun seedInitialDataIfEmpty() {
        if (expenseDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            fun timeOffset(daysAgo: Int, hour: Int = 14, min: Int = 30): Long {
                cal.timeInMillis = now
                cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)
                return cal.timeInMillis
            }

            val seedExpenses = listOf(
                ExpenseEntity(
                    title = "Monthly Salary Deposit",
                    amount = 4500.00,
                    isIncome = true,
                    category = ExpenseCategory.INCOME.name,
                    paymentMethod = PaymentMethod.BANK_TRANSFER.displayName,
                    dateMillis = timeOffset(6, 9, 0),
                    note = "Bi-weekly direct deposit"
                ),
                ExpenseEntity(
                    title = "Whole Foods Market",
                    amount = 86.40,
                    isIncome = false,
                    category = ExpenseCategory.FOOD_DINING.name,
                    paymentMethod = PaymentMethod.CREDIT_CARD.displayName,
                    dateMillis = timeOffset(0, 11, 25),
                    note = "Weekly organic groceries & fruits"
                ),
                ExpenseEntity(
                    title = "Artisan Espresso Roastery",
                    amount = 6.75,
                    isIncome = false,
                    category = ExpenseCategory.FOOD_DINING.name,
                    paymentMethod = PaymentMethod.GOOGLE_PAY.displayName,
                    dateMillis = timeOffset(0, 8, 45),
                    note = "Oat milk cappuccino & croissant"
                ),
                ExpenseEntity(
                    title = "Metro Transit Pass",
                    amount = 32.00,
                    isIncome = false,
                    category = ExpenseCategory.TRANSPORT.name,
                    paymentMethod = PaymentMethod.DEBIT_CARD.displayName,
                    dateMillis = timeOffset(1, 17, 10),
                    note = "Monthly subway recharge"
                ),
                ExpenseEntity(
                    title = "Urban Outfitters",
                    amount = 54.20,
                    isIncome = false,
                    category = ExpenseCategory.SHOPPING.name,
                    paymentMethod = PaymentMethod.CREDIT_CARD.displayName,
                    dateMillis = timeOffset(1, 15, 30),
                    note = "Summer linen shirt"
                ),
                ExpenseEntity(
                    title = "Equinox Fitness Club",
                    amount = 85.00,
                    isIncome = false,
                    category = ExpenseCategory.HEALTH.name,
                    paymentMethod = PaymentMethod.CREDIT_CARD.displayName,
                    dateMillis = timeOffset(2, 7, 0),
                    note = "Monthly fitness & yoga membership"
                ),
                ExpenseEntity(
                    title = "Fiber Internet & Utilities",
                    amount = 65.50,
                    isIncome = false,
                    category = ExpenseCategory.BILLS.name,
                    paymentMethod = PaymentMethod.BANK_TRANSFER.displayName,
                    dateMillis = timeOffset(3, 10, 15),
                    note = "High speed broadband"
                ),
                ExpenseEntity(
                    title = "Trattoria Della Nonna",
                    amount = 74.80,
                    isIncome = false,
                    category = ExpenseCategory.FOOD_DINING.name,
                    paymentMethod = PaymentMethod.CREDIT_CARD.displayName,
                    dateMillis = timeOffset(4, 19, 45),
                    note = "Dinner date pasta & wine"
                ),
                ExpenseEntity(
                    title = "Cinema & Streaming",
                    amount = 28.50,
                    isIncome = false,
                    category = ExpenseCategory.ENTERTAINMENT.name,
                    paymentMethod = PaymentMethod.GOOGLE_PAY.displayName,
                    dateMillis = timeOffset(5, 21, 0),
                    note = "Movie tickets & popcorn"
                ),
                ExpenseEntity(
                    title = "Freelance Design Retainer",
                    amount = 650.00,
                    isIncome = true,
                    category = ExpenseCategory.INCOME.name,
                    paymentMethod = PaymentMethod.BANK_TRANSFER.displayName,
                    dateMillis = timeOffset(3, 16, 0),
                    note = "UI concept sprint payout"
                )
            )
            expenseDao.insertAll(seedExpenses)
        }
    }
}
