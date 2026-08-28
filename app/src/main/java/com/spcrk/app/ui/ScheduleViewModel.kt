package com.spcrk.app.ui

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.spcrk.app.AppContainer
import com.spcrk.app.SCHEDULE_CHANNEL_ID
import com.spcrk.app.getAppContainer
import com.spcrk.app.data.ScheduledTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class ScheduleUiState(
    val tasks: List<ScheduledTask> = emptyList(),
    val isLoading: Boolean = true,
    val showDialog: Boolean = false,
    val editingTask: ScheduledTask? = null
)

enum class SchedulePreset {
    EVERY_MINUTE, EVERY_5_MINUTES, EVERY_15_MINUTES, EVERY_HOUR, EVERY_DAILY, CUSTOM
}

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val container: AppContainer = getAppContainer(application)
    private val repository = container.repository
    private val workManager = WorkManager.getInstance(application)
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            repository.getAllScheduledTasks().collect { tasks ->
                _uiState.value = ScheduleUiState(tasks = tasks, isLoading = false)
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showDialog = true, editingTask = null)
    }

    fun showEditDialog(task: ScheduledTask) {
        _uiState.value = _uiState.value.copy(showDialog = true, editingTask = task)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false, editingTask = null)
    }

    fun createTask(
        name: String,
        cronExpression: String,
        action: String,
        actionParams: String
    ) {
        viewModelScope.launch {
            val preset = parseCronPreset(cronExpression)
            val intervalMinutes = presetToIntervalMinutes(preset)

            val nextRun = calculateNextRun(intervalMinutes)

            val task = ScheduledTask(
                name = name,
                cronExpression = cronExpression,
                action = action,
                actionParams = actionParams,
                isEnabled = true,
                nextRun = nextRun
            )

            val taskId = repository.addScheduledTask(task)
            if (task.isEnabled) {
                scheduleWorkManager(task.copy(id = taskId), intervalMinutes)
            }

            _uiState.value = _uiState.value.copy(showDialog = false, editingTask = null)
        }
    }

    fun updateTask(task: ScheduledTask) {
        viewModelScope.launch {
            val preset = parseCronPreset(task.cronExpression)
            val intervalMinutes = presetToIntervalMinutes(preset)

            val nextRun = if (task.isEnabled) {
                calculateNextRun(intervalMinutes)
            } else {
                task.nextRun
            }

            val updatedTask = task.copy(nextRun = nextRun)
            repository.updateScheduledTask(updatedTask)

            cancelWorkManager(task.id.toString())
            if (updatedTask.isEnabled) {
                scheduleWorkManager(updatedTask, intervalMinutes)
            }
        }
    }

    fun toggleTaskEnabled(task: ScheduledTask) {
        viewModelScope.launch {
            val preset = parseCronPreset(task.cronExpression)
            val intervalMinutes = presetToIntervalMinutes(preset)

            val updatedTask = if (!task.isEnabled) {
                val nextRun = calculateNextRun(intervalMinutes)
                task.copy(isEnabled = true, nextRun = nextRun)
            } else {
                task.copy(isEnabled = false)
            }

            repository.updateScheduledTask(updatedTask)

            if (updatedTask.isEnabled) {
                scheduleWorkManager(updatedTask, intervalMinutes)
            } else {
                cancelWorkManager(task.id.toString())
            }
        }
    }

    fun deleteTask(task: ScheduledTask) {
        viewModelScope.launch {
            cancelWorkManager(task.id.toString())
            repository.deleteScheduledTask(task)
        }
    }

    private fun scheduleWorkManager(task: ScheduledTask, intervalMinutes: Long) {
        val workRequest = PeriodicWorkRequestBuilder<ScheduledTaskWorker>(
            intervalMinutes, TimeUnit.MINUTES
        )
            .setInputData(
                workDataOf(
                    "task_id" to task.id,
                    "task_name" to task.name,
                    "action" to task.action,
                    "action_params" to task.actionParams
                )
            )
            .addTag("scheduled_task_${task.id}")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "task_${task.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelWorkManager(taskId: String) {
        workManager.cancelUniqueWork("task_$taskId")
    }

    private fun calculateNextRun(intervalMinutes: Long): Long {
        return System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(intervalMinutes)
    }

    private fun parseCronPreset(cronExpression: String): SchedulePreset {
        return when (cronExpression) {
            "* * * * *" -> SchedulePreset.EVERY_MINUTE
            "*/5 * * * *" -> SchedulePreset.EVERY_5_MINUTES
            "*/15 * * * *" -> SchedulePreset.EVERY_15_MINUTES
            "0 * * * *" -> SchedulePreset.EVERY_HOUR
            "0 9 * * *" -> SchedulePreset.EVERY_DAILY
            else -> SchedulePreset.CUSTOM
        }
    }

    private fun presetToIntervalMinutes(preset: SchedulePreset): Long {
        return when (preset) {
            SchedulePreset.EVERY_MINUTE -> 1
            SchedulePreset.EVERY_5_MINUTES -> 5
            SchedulePreset.EVERY_15_MINUTES -> 15
            SchedulePreset.EVERY_HOUR -> 60
            SchedulePreset.EVERY_DAILY -> 1440
            SchedulePreset.CUSTOM -> 60
        }
    }

    fun presetToCronExpression(preset: SchedulePreset): String {
        return when (preset) {
            SchedulePreset.EVERY_MINUTE -> "* * * * *"
            SchedulePreset.EVERY_5_MINUTES -> "*/5 * * * *"
            SchedulePreset.EVERY_15_MINUTES -> "*/15 * * * *"
            SchedulePreset.EVERY_HOUR -> "0 * * * *"
            SchedulePreset.EVERY_DAILY -> "0 9 * * *"
            SchedulePreset.CUSTOM -> "0 9 * * *"
        }
    }

    fun getNextRunTime(task: ScheduledTask): String {
        if (!task.isEnabled) return "已禁用"
        if (task.nextRun <= 0) return "未设置"

        val diff = task.nextRun - System.currentTimeMillis()
        if (diff <= 0) return "即将执行"

        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60

        return when {
            hours > 24 -> "${hours / 24}天${hours % 24}小时后"
            hours > 0 -> "${hours}小时${minutes}分钟后"
            minutes > 0 -> "${minutes}分钟后"
            else -> "即将执行"
        }
    }
}

class ScheduledTaskWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val taskName = inputData.getString("task_name") ?: return Result.failure()
        val action = inputData.getString("action") ?: ""
        val actionParams = inputData.getString("action_params") ?: ""
        val taskId = inputData.getLong("task_id", 0)

        sendNotification(taskId, taskName, action, actionParams)

        return Result.success()
    }

    private fun sendNotification(taskId: Long, taskName: String, action: String, actionParams: String) {
        val context = applicationContext
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, SCHEDULE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("定时任务执行")
            .setContentText("任务「$taskName」已执行：$action")
            .setStyle(NotificationCompat.BigTextStyle().bigText("任务「$taskName」已执行：$action\n参数：$actionParams"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }
}
