package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

enum class ExpenseCategory(
    val displayName: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val textColor: Color
) {
    FOOD_DINING("Food & Dining", Icons.Filled.Restaurant, CategoryFoodBg, CategoryFoodText),
    HOUSING("Housing & Utilities", Icons.Filled.Home, CategoryHousingBg, CategoryHousingText),
    SHOPPING("Shopping", Icons.Filled.ShoppingBag, CategoryShoppingBg, CategoryShoppingText),
    TRANSPORT("Transport", Icons.Filled.DirectionsCar, CategoryTransportBg, CategoryTransportText),
    ENTERTAINMENT("Entertainment", Icons.Filled.Movie, CategoryEntertainmentBg, CategoryEntertainmentText),
    HEALTH("Health & Care", Icons.Filled.FitnessCenter, CategoryHealthBg, CategoryHealthText),
    BILLS("Bills & Services", Icons.Filled.Receipt, CategoryBillsBg, CategoryBillsText),
    INCOME("Salary & Income", Icons.Filled.Work, CategoryIncomeBg, CategoryIncomeText),
    OTHER("Other", Icons.Filled.LocalAtm, CategoryOtherBg, CategoryOtherText);

    companion object {
        fun fromString(name: String): ExpenseCategory {
            return entries.firstOrNull {
                it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true)
            } ?: OTHER
        }
    }
}

enum class PaymentMethod(
    val displayName: String,
    val icon: ImageVector
) {
    CREDIT_CARD("Credit Card", Icons.Filled.CreditCard),
    DEBIT_CARD("Debit Card", Icons.Filled.Payment),
    GOOGLE_PAY("Google Pay", Icons.Filled.Payment),
    CASH("Cash", Icons.Filled.AttachMoney),
    BANK_TRANSFER("Bank Transfer", Icons.Filled.AccountBalance);

    companion object {
        fun fromString(name: String): PaymentMethod {
            return entries.firstOrNull {
                it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true)
            } ?: CREDIT_CARD
        }
    }
}

enum class DateFilter(val label: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    TODAY("Today")
}

enum class SortOrder(val label: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_DESC("Highest Amount"),
    AMOUNT_ASC("Lowest Amount")
}
