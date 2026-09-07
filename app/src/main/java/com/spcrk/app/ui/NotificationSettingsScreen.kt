package com.spcrk.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.spcrk.app.getAppContainer
import com.spcrk.app.ui.l10n.appStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit
) {
    val settingsStore = getAppContainer(LocalContext.current).settingsStore

    val notificationEnabled by settingsStore.notificationEnabledFlow.collectAsState(initial = true)
    val notificationSound by settingsStore.notificationSoundFlow.collectAsState(initial = true)
    val notificationVibration by settingsStore.notificationVibrationFlow.collectAsState(initial = true)
    val s = appStrings()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.notificationSettingsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = s.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            s.notificationSwitchTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NotificationToggleRow(
                            title = s.enableNotifications,
                            description = s.enableNotificationsDesc,
                            icon = Icons.Outlined.Notifications,
                            checked = notificationEnabled,
                            onCheckedChange = { settingsStore.setNotificationEnabled(it) }
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        NotificationToggleRow(
                            title = s.notificationSoundTitle,
                            description = s.notificationSoundDesc,
                            icon = Icons.Outlined.VolumeUp,
                            checked = notificationSound,
                            onCheckedChange = { settingsStore.setNotificationSound(it) },
                            enabled = notificationEnabled
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        NotificationToggleRow(
                            title = s.notificationVibrationTitle,
                            description = s.notificationVibrationDesc,
                            icon = Icons.Outlined.Vibration,
                            checked = notificationVibration,
                            onCheckedChange = { settingsStore.setNotificationVibration(it) },
                            enabled = notificationEnabled
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            s.notificationTypesTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NotificationToggleRow(
                            title = s.taskCompleteNotificationTitle,
                            description = s.taskCompleteNotificationDesc,
                            icon = Icons.Outlined.TaskAlt,
                            checked = true,
                            onCheckedChange = { }
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        NotificationToggleRow(
                            title = s.taskFailNotificationTitle,
                            description = s.taskFailNotificationDesc,
                            icon = Icons.Outlined.ErrorOutline,
                            checked = true,
                            onCheckedChange = { }
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        NotificationToggleRow(
                            title = s.updateNotificationTitle,
                            description = s.updateNotificationDesc,
                            icon = Icons.Outlined.SystemUpdate,
                            checked = true,
                            onCheckedChange = { }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            s.doNotDisturbTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.enableDoNotDisturb, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    s.doNotDisturbDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = false,
                                onCheckedChange = { }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(s.doNotDisturbTimeFormat, "22:00", "08:00"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.3f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
