package com.spcrk.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spcrk.app.VideoDownloaderApp
import com.spcrk.app.data.UsageRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class ModelUsageStats(
    val model: String,
    val totalTokens: Int = 0,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val cost: Double = 0.0,
    val callCount: Int = 0
)

data class DailyUsageStats(
    val date: String,
    val totalTokens: Int = 0,
    val cost: Double = 0.0,
    val callCount: Int = 0
)

data class StatsUiState(
    val totalTokens: Int = 0,
    val totalCost: Double = 0.0,
    val totalCalls: Int = 0,
    val dailyStats: List<DailyUsageStats> = emptyList(),
    val modelStats: List<ModelUsageStats> = emptyList(),
    val selectedPeriod: TimePeriod = TimePeriod.DAY,
    val isLoading: Boolean = true
)

enum class TimePeriod {
    DAY, WEEK, MONTH
}

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as VideoDownloaderApp).repository
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val modelPricing = mapOf(
        "gpt-4" to Pair(0.03, 0.06),
        "gpt-4-turbo" to Pair(0.01, 0.03),
        "gpt-3.5-turbo" to Pair(0.0005, 0.0015),
        "claude-3-opus" to Pair(0.015, 0.075),
        "claude-3-sonnet" to Pair(0.003, 0.015),
        "claude-3-haiku" to Pair(0.00025, 0.00125),
        "deepseek-chat" to Pair(0.001, 0.002),
        "qwen-turbo" to Pair(0.0003, 0.0006),
        "qwen-plus" to Pair(0.001, 0.002)
    )

    init {
        loadStats()
    }

    fun selectPeriod(period: TimePeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val period = _uiState.value.selectedPeriod
            val startTime = calculateStartTime(period)

            repository.getUsageRecordsSince(startTime).collect { records ->
                val filteredRecords = records.filter { it.timestamp >= startTime }
                val aggregated = aggregateByPeriod(filteredRecords, period)
                val byModel = aggregateByModel(filteredRecords)
                val totalTokens = filteredRecords.sumOf { it.totalTokens }
                val totalCost = filteredRecords.sumOf { it.cost }
                val totalCalls = filteredRecords.size

                _uiState.value = StatsUiState(
                    totalTokens = totalTokens,
                    totalCost = totalCost,
                    totalCalls = totalCalls,
                    dailyStats = aggregated,
                    modelStats = byModel,
                    selectedPeriod = period,
                    isLoading = false
                )
            }
        }
    }

    private fun calculateStartTime(period: TimePeriod): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        return when (period) {
            TimePeriod.DAY -> now - TimeUnit.DAYS.toMillis(1)
            TimePeriod.WEEK -> now - TimeUnit.DAYS.toMillis(7)
            TimePeriod.MONTH -> now - TimeUnit.DAYS.toMillis(30)
        }
    }

    private fun aggregateByPeriod(records: List<UsageRecord>, period: TimePeriod): List<DailyUsageStats> {
        val dateFormat = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
        val fullDateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        return records.groupBy { record ->
            val cal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
            when (period) {
                TimePeriod.DAY -> {
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    String.format("%02d:00", hour)
                }
                TimePeriod.WEEK, TimePeriod.MONTH -> {
                    dateFormat.format(cal.time)
                }
            }
        }.map { (date, group) ->
            DailyUsageStats(
                date = date,
                totalTokens = group.sumOf { it.totalTokens },
                cost = group.sumOf { it.cost },
                callCount = group.size
            )
        }.sortedBy { it.date }
    }

    private fun aggregateByModel(records: List<UsageRecord>): List<ModelUsageStats> {
        return records.groupBy { it.model }
            .map { (model, group) ->
                ModelUsageStats(
                    model = model,
                    totalTokens = group.sumOf { it.totalTokens },
                    promptTokens = group.sumOf { it.promptTokens },
                    completionTokens = group.sumOf { it.completionTokens },
                    cost = group.sumOf { it.cost },
                    callCount = group.size
                )
            }
            .sortedByDescending { it.totalTokens }
    }

    fun calculateCost(model: String, promptTokens: Int, completionTokens: Int): Double {
        val pricing = modelPricing[model] ?: Pair(0.001, 0.002)
        return (promptTokens * pricing.first + completionTokens * pricing.second) / 1000.0
    }
}
