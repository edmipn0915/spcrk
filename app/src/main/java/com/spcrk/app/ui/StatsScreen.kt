package com.spcrk.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spcrk.app.ui.theme.LocalSuccessColor
import com.spcrk.app.ui.theme.TechCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBackClick: () -> Unit,
    viewModel: StatsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用量统计") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SummaryCards(uiState.totalTokens, uiState.totalCost, uiState.totalCalls)
                }

                item {
                    PeriodSelector(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { viewModel.selectPeriod(it) }
                    )
                }

                item {
                    UsageTrendChart(uiState.dailyStats, uiState.selectedPeriod)
                }

                item {
                    ModelDistributionCard(uiState.modelStats)
                }

                item {
                    CostEstimateCard(uiState.totalCost, uiState.totalCalls)
                }
            }
        }
    }
}

@Composable
private fun SummaryCards(totalTokens: Int, totalCost: Double, totalCalls: Int) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryCard(
                title = "总 Token",
                value = formatTokenCount(totalTokens),
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            SummaryCard(
                title = "总费用",
                value = String.format("$%.4f", totalCost),
                color = LocalSuccessColor.current
            )
        }
        item {
            SummaryCard(
                title = "调用次数",
                value = totalCalls.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, color: Color) {
    TechCard(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    TechCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PeriodChip(
                label = "今日",
                isSelected = selectedPeriod == TimePeriod.DAY,
                onClick = { onPeriodSelected(TimePeriod.DAY) }
            )
            PeriodChip(
                label = "本周",
                isSelected = selectedPeriod == TimePeriod.WEEK,
                onClick = { onPeriodSelected(TimePeriod.WEEK) }
            )
            PeriodChip(
                label = "本月",
                isSelected = selectedPeriod == TimePeriod.MONTH,
                onClick = { onPeriodSelected(TimePeriod.MONTH) }
            )
        }
    }
}

@Composable
private fun PeriodChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun UsageTrendChart(dailyStats: List<DailyUsageStats>, period: TimePeriod) {
    TechCard {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Token 用量趋势",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (dailyStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxTokens = dailyStats.maxOfOrNull { it.totalTokens }?.coerceAtLeast(1) ?: 1
                val barColors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                    Color(0xFFFF9800),
                    Color(0xFFE91E63)
                )
                val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = canvasWidth / (dailyStats.size * 2)
                    val spacing = barWidth

                    dailyStats.take(12).forEachIndexed { index, stat ->
                        val barHeight = (stat.totalTokens.toFloat() / maxTokens) * (canvasHeight - 40)
                        val x = spacing + index * (barWidth + spacing)
                        val y = canvasHeight - barHeight - 20

                        drawRoundRect(
                            color = barColors[index % barColors.size],
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )

                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                formatTokenCount(stat.totalTokens),
                                x + barWidth / 2,
                                y - 8,
                                android.graphics.Paint().apply {
                                    color = labelColor
                                    textSize = 24f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dailyStats.take(12).forEach { stat ->
                        Text(
                            text = stat.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelDistributionCard(modelStats: List<ModelUsageStats>) {
    TechCard {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "按模型分布",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (modelStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val totalTokens = modelStats.sumOf { it.totalTokens }.coerceAtLeast(1)
                val modelColors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                    Color(0xFFFF9800),
                    Color(0xFFE91E63),
                    Color(0xFF9C27B0)
                )

                modelStats.forEachIndexed { index, stat ->
                    val percentage = (stat.totalTokens.toFloat() / totalTokens * 100).toInt()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(modelColors[index % modelColors.size])
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stat.model,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(stat.totalTokens.toFloat() / totalTokens)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(modelColors[index % modelColors.size])
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${percentage}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatTokenCount(stat.totalTokens),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CostEstimateCard(totalCost: Double, totalCalls: Int) {
    TechCard {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "费用估算",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "当前周期费用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("$%.4f", totalCost),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LocalSuccessColor.current,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "平均每次调用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val avgCost = if (totalCalls > 0) totalCost / totalCalls else 0.0
                    Text(
                        text = String.format("$%.4f", avgCost),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Token 单价参考",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "~ $0.001 / 1K tokens",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun formatTokenCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000f)
        count >= 1000 -> String.format("%.1fK", count / 1000f)
        else -> count.toString()
    }
}
