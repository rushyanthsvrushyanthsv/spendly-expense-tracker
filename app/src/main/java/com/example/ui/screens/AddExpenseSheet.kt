package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.ui.model.ExpenseCategory
import com.example.ui.model.PaymentMethod
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    initialExpense: ExpenseEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        isIncome: Boolean,
        category: ExpenseCategory,
        paymentMethod: String,
        dateMillis: Long,
        note: String
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isIncome by remember { mutableStateOf(initialExpense?.isIncome ?: false) }
    var amountString by remember {
        mutableStateOf(
            if (initialExpense != null) String.format(Locale.US, "%.2f", initialExpense.amount) else ""
        )
    }
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var note by remember { mutableStateOf(initialExpense?.note ?: "") }
    var selectedCategory by remember {
        mutableStateOf(
            initialExpense?.let { ExpenseCategory.fromString(it.category) }
                ?: if (isIncome) ExpenseCategory.INCOME else ExpenseCategory.FOOD_DINING
        )
    }
    var selectedPaymentMethod by remember {
        mutableStateOf(
            initialExpense?.let { PaymentMethod.fromString(it.paymentMethod) } ?: PaymentMethod.CREDIT_CARD
        )
    }
    var selectedDateMillis by remember {
        mutableStateOf(initialExpense?.dateMillis ?: System.currentTimeMillis())
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }

    val calendar = remember { Calendar.getInstance() }

    val quickAmounts = listOf(5.0, 10.0, 25.0, 50.0, 100.0)
    val quickMerchantSuggestions = if (isIncome) {
        listOf("Salary", "Freelance", "Bonus", "Dividend", "Gift")
    } else {
        listOf("Groceries", "Coffee", "Restaurant", "Subway", "Uber", "Target", "Amazon", "Electric Bill")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PureWhite,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = SlateBorder
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .testTag("add_expense_sheet")
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialExpense != null) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    color = SlateDark,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sheet_button")
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = SlateMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Type Segmented Control (Expense vs Income)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp)
            ) {
                // Expense Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (!isIncome) PureWhite else Color.Transparent)
                        .clickable {
                            isIncome = false
                            if (selectedCategory == ExpenseCategory.INCOME) {
                                selectedCategory = ExpenseCategory.FOOD_DINING
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense",
                        fontWeight = if (!isIncome) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isIncome) SlateDark else SlateMuted,
                        fontSize = 14.sp
                    )
                }

                // Income Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isIncome) PureWhite else Color.Transparent)
                        .clickable {
                            isIncome = true
                            selectedCategory = ExpenseCategory.INCOME
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income",
                        fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                        color = if (isIncome) EmeraldPositiveText else SlateMuted,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Prominent Amount Input Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SlateSurface)
                    .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) EmeraldPositive else TealPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextField(
                        value = amountString,
                        onValueChange = { input ->
                            // Allow numbers and at most one decimal point with 2 decimals
                            if (input.isEmpty() || input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                amountString = input
                                errorMessage = null
                            }
                        },
                        placeholder = {
                            Text(
                                "0.00",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateLight
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SlateDark,
                            textAlign = TextAlign.Start
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .width(180.dp)
                            .testTag("amount_input_field")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Amount Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickAmounts) { addAmount ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
                            .background(PureWhite)
                            .clickable {
                                val current = amountString.toDoubleOrNull() ?: 0.0
                                amountString = String.format(Locale.US, "%.2f", current + addAmount)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+$${addAmount.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Merchant / Title Input
            Text(
                text = if (isIncome) "Source / Payer" else "Merchant / Purpose",
                style = MaterialTheme.typography.labelLarge,
                color = SlateDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                placeholder = { Text(if (isIncome) "e.g. Monthly Salary, Tech Corp" else "e.g. Trader Joe's, Coffee") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input_field"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = SlateBorder
                )
            )

            // Quick merchant suggestions
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickMerchantSuggestions) { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable { title = suggestion }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 11.sp,
                            color = SlateMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                color = SlateDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ExpenseCategory.entries.filter { if (isIncome) it == ExpenseCategory.INCOME || it == ExpenseCategory.OTHER else it != ExpenseCategory.INCOME }) { category ->
                    val isSelected = selectedCategory == category
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) TealPrimary else SlateBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(if (isSelected) category.backgroundColor else PureWhite)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("category_chip_${category.name}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(category.backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.displayName,
                                tint = category.textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = category.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SlateDark else SlateMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Date & Payment Method Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Picker Button
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelLarge,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                            .background(SlateSurface)
                            .clickable {
                                calendar.timeInMillis = selectedDateMillis
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        calendar.set(year, month, dayOfMonth)
                                        selectedDateMillis = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                            .testTag("date_picker_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = "Date",
                                tint = TealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = dateFormat.format(Date(selectedDateMillis)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateDark,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Payment Method Selector
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.labelLarge,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    var paymentMenuExpanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                            .background(SlateSurface)
                            .clickable { paymentMenuExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                            .testTag("payment_method_dropdown")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                selectedPaymentMethod.icon,
                                contentDescription = selectedPaymentMethod.displayName,
                                tint = TealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = selectedPaymentMethod.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateDark,
                                maxLines = 1
                            )
                        }

                        DropdownMenu(
                            expanded = paymentMenuExpanded,
                            onDismissRequest = { paymentMenuExpanded = false }
                        ) {
                            PaymentMethod.entries.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method.displayName) },
                                    leadingIcon = {
                                        Icon(method.icon, contentDescription = method.displayName, tint = TealPrimary)
                                    },
                                    onClick = {
                                        selectedPaymentMethod = method
                                        paymentMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Note / Comment
            Text(
                text = "Note (Optional)",
                style = MaterialTheme.typography.labelLarge,
                color = SlateDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Add extra details or tags...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input_field"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = SlateBorder
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = RoseNegative,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons (Save & Delete if editing)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (initialExpense != null && onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("delete_expense_button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RoseNegative
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseNegativeBg)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }

                Button(
                    onClick = {
                        val amount = amountString.toDoubleOrNull()
                        if (amount == null || amount <= 0.0) {
                            errorMessage = "Please enter a valid amount greater than 0"
                            return@Button
                        }
                        val finalTitle = if (title.isBlank()) {
                            selectedCategory.displayName
                        } else {
                            title
                        }

                        onSave(
                            finalTitle,
                            amount,
                            isIncome,
                            selectedCategory,
                            selectedPaymentMethod.displayName,
                            selectedDateMillis,
                            note
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("save_expense_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialExpense != null) "Update Transaction" else "Save Transaction",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
