package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.DailySpend
import java.util.Locale

@Composable
fun SpendingBarChart(
    dailyData: List<DailySpend>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxSpend = remember(dailyData) {
        val max = dailyData.maxOfOrNull { it.amount } ?: 1.0
        if (max > 0.0) max else 100.0
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spending_chart_container")
    ) {
        // Selected day indicator tooltip
        val activeSpend = selectedIndex?.let { dailyData.getOrNull(it) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (activeSpend != null) "${activeSpend.dayLabel}'s Spend" else "Daily Overview",
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted
            )
            Text(
                text = if (activeSpend != null) String.format(Locale.US, "$%.2f", activeSpend.amount) else "Tap bar for details",
                style = MaterialTheme.typography.labelLarge,
                color = if (activeSpend != null) TealPrimary else SlateLight,
                fontWeight = FontWeight.Bold
            )
        }

        // Bars Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            dailyData.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index || (selectedIndex == null && item.isToday)
                val targetFraction = (item.amount / maxSpend).toFloat().coerceIn(0.06f, 1.0f)
                val animatedHeightFraction by animateFloatAsState(
                    targetValue = targetFraction,
                    animationSpec = tween(durationMillis = 600),
                    label = "barHeight"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            selectedIndex = if (selectedIndex == index) null else index
                        }
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedHeightFraction)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(
                                when {
                                    isSelected -> TealPrimary
                                    item.amount > 0 -> TealPrimary.copy(alpha = 0.35f)
                                    else -> Color(0xFFE2E8F0)
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Day label
                    Text(
                        text = item.dayLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TealPrimary else SlateMuted,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
