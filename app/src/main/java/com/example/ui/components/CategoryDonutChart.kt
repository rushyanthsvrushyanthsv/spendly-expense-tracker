package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMuted
import com.example.ui.viewmodel.CategorySpend
import java.util.Locale

@Composable
fun CategoryDonutChart(
    categorySpends: List<CategorySpend>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 22.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeftOffset = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

            if (categorySpends.isEmpty()) {
                drawArc(
                    color = Color(0xFFE2E8F0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeftOffset,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                var currentAngle = -90f
                categorySpends.forEach { item ->
                    val sweep = (item.percentage * 360f).coerceAtLeast(3f)
                    drawArc(
                        color = item.category.textColor,
                        startAngle = currentAngle,
                        sweepAngle = sweep - 2f, // micro-gap between slices
                        useCenter = false,
                        topLeft = topLeftOffset,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    currentAngle += sweep
                }
            }
        }

        // Center Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Spent",
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted
            )
            Text(
                text = String.format(Locale.US, "$%,.0f", totalExpense),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SlateDark
            )
        }
    }
}
