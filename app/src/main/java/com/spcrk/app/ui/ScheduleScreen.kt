package com.spcrk.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spcrk.app.data.ScheduledTask
import com.spcrk.app.ui.l10n.appStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onBackClick: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val s = appStrings()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.schedulesTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = s.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = s.create)
            }
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
        } else if (uiState.tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = s.noScheduledTasks,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = s.createTaskHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.tasks) { task ->
                    TaskCard(
                        task = task,
                        nextRunTime = viewModel.getNextRunTime(task),
                        onToggle = { viewModel.toggleTaskEnabled(task) },
                        onEdit = { viewModel.showEditDialog(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }

        if (uiState.showDialog) {
            TaskDialog(
                task = uiState.editingTask,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { name, cronExpression, action, actionParams ->
                    if (uiState.editingTask != null) {
                        viewModel.updateTask(
                            uiState.editingTask!!.copy(
                                name = name,
                                cronExpression = cronExpression,
                                action = action,
                                actionParams = actionParams
                            )
                        )
                    } else {
                        viewModel.createTask(name, cronExpression, action, actionParams)
                    }
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: ScheduledTask,
    nextRunTime: String,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val s = appStrings()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (task.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.action,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = task.isEnabled,
                    onCheckedChange = { onToggle() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = s.nextRunTimeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = nextRunTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = s.edit,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = s.delete,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(s.deleteTaskTitle) },
            text = { Text(String.format(s.deleteTaskMessage, task.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text(s.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@Composable
private fun TaskDialog(
    task: ScheduledTask?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit,
    viewModel: ScheduleViewModel
) {
    var name by remember { mutableStateOf(task?.name ?: "") }
    var selectedPreset by remember {
        mutableStateOf(
            task?.let { parsePresetFromCron(it.cronExpression) } ?: SchedulePreset.EVERY_DAILY
        )
    }
    var customCron by remember { mutableStateOf(task?.cronExpression ?: "0 9 * * *") }
    var action by remember { mutableStateOf(task?.action ?: "download") }
    var actionParams by remember { mutableStateOf(task?.actionParams ?: "") }

    val cronExpression = if (selectedPreset == SchedulePreset.CUSTOM) {
        customCron
    } else {
        viewModel.presetToCronExpression(selectedPreset)
    }
    val s = appStrings()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (task == null) s.newTaskTitle else s.editTaskTitle)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.taskNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = s.executionFrequencyLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column {
                    PresetChip(
                        label = s.everyMinute,
                        isSelected = selectedPreset == SchedulePreset.EVERY_MINUTE,
                        onClick = { selectedPreset = SchedulePreset.EVERY_MINUTE }
                    )
                    PresetChip(
                        label = s.everyFiveMinutes,
                        isSelected = selectedPreset == SchedulePreset.EVERY_5_MINUTES,
                        onClick = { selectedPreset = SchedulePreset.EVERY_5_MINUTES }
                    )
                    PresetChip(
                        label = s.everyFifteenMinutes,
                        isSelected = selectedPreset == SchedulePreset.EVERY_15_MINUTES,
                        onClick = { selectedPreset = SchedulePreset.EVERY_15_MINUTES }
                    )
                    PresetChip(
                        label = s.everyHour,
                        isSelected = selectedPreset == SchedulePreset.EVERY_HOUR,
                        onClick = { selectedPreset = SchedulePreset.EVERY_HOUR }
                    )
                    PresetChip(
                        label = s.everyDayPreset,
                        isSelected = selectedPreset == SchedulePreset.EVERY_DAILY,
                        onClick = { selectedPreset = SchedulePreset.EVERY_DAILY }
                    )
                }

                if (selectedPreset == SchedulePreset.CUSTOM) {
                    OutlinedTextField(
                        value = customCron,
                        onValueChange = { customCron = it },
                        label = { Text(s.cronExpressionLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    text = String.format(s.cronFormat, cronExpression),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text(s.actionTypeLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = actionParams,
                    onValueChange = { actionParams = it },
                    label = { Text(s.actionParamsLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, cronExpression, action, actionParams)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(s.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel)
            }
        }
    )
}

@Composable
private fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        modifier = Modifier.padding(end = 4.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

private fun parsePresetFromCron(cronExpression: String): SchedulePreset {
    return when (cronExpression) {
        "* * * * *" -> SchedulePreset.EVERY_MINUTE
        "*/5 * * * *" -> SchedulePreset.EVERY_5_MINUTES
        "*/15 * * * *" -> SchedulePreset.EVERY_15_MINUTES
        "0 * * * *" -> SchedulePreset.EVERY_HOUR
        "0 9 * * *" -> SchedulePreset.EVERY_DAILY
        else -> SchedulePreset.CUSTOM
    }
}
