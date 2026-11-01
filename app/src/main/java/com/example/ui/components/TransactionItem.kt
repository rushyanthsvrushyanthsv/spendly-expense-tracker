package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.ui.model.ExpenseCategory
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    expense: ExpenseEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = ExpenseCategory.fromString(expense.category)
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedDate = remember(expense.dateMillis) { dateFormat.format(Date(expense.dateMillis)) }
    val formattedTime = remember(expense.dateMillis) { timeFormat.format(Date(expense.dateMillis)) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${expense.id}")
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with Squircle Background
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(category.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.displayName,
                    tint = category.textColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Merchant / Title & Metadata
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SlateDark,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Mini Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(category.backgroundColor.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = category.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = category.textColor
                        )
                    }

                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = SlateMuted
                    )

                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = SlateMuted
                    )
                }

                if (expense.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = expense.note,
                        fontSize = 11.sp,
                        color = SlateLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Tabular Amount & Payment Method
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val amountText = if (expense.isIncome) {
                    String.format(Locale.US, "+$%,.2f", expense.amount)
                } else {
                    String.format(Locale.US, "-$%,.2f", expense.amount)
                }

                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (expense.isIncome) EmeraldPositiveText else SlateDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = expense.paymentMethod,
                    fontSize = 11.sp,
                    color = SlateMuted
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete '${expense.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = RoseNegative)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
