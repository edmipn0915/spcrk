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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val s = appStrings()
    val settingsStore = getAppContainer(context).settingsStore
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val autoBackup by settingsStore.autoBackupFlow.collectAsState(initial = false)
    val backupPath by settingsStore.backupPathFlow.collectAsState(initial = "")
    val webDavUrl by settingsStore.webDavUrlFlow.collectAsState(initial = "")
    val webDavUser by settingsStore.webDavUserFlow.collectAsState(initial = "")
    val webDavPassword by settingsStore.webDavPasswordFlow.collectAsState(initial = "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.dataTitle) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSection(title = s.backupSettings, icon = Icons.Outlined.Backup) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.autoBackup, style = MaterialTheme.typography.titleMedium)
                            Text(
                                s.autoBackupDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = autoBackup,
                            onCheckedChange = { settingsStore.setAutoBackup(it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupPath,
                        onValueChange = { settingsStore.setBackupPath(it) },
                        label = { Text(s.backupPath) },
                        placeholder = { Text("/storage/emulated/0/Backup") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(s.backupComingSoon)
                            }
                        }) {
                            Text(s.backupNow)
                        }
                    }
                }
            }

            item {
                SettingsSection(title = s.webDavStorage, icon = Icons.Outlined.Cloud) {
                    OutlinedTextField(
                        value = webDavUrl,
                        onValueChange = { settingsStore.setWebDavUrl(it) },
                        label = { Text("WebDAV URL") },
                        placeholder = { Text("https://dav.example.com") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = webDavUser,
                        onValueChange = { settingsStore.setWebDavUser(it) },
                        label = { Text(s.username) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = webDavPassword,
                        onValueChange = { settingsStore.setWebDavPassword(it) },
                        label = { Text(s.password) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(s.webDavComingSoon)
                            }
                        }) {
                            Text(s.testConnection)
                        }
                    }
                }
            }

            item {
                SettingsSection(title = s.importExport, icon = Icons.Outlined.ImportExport) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(s.importComingSoon)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.FileOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.importData)
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(s.exportComingSoon)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.SaveAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.exportData)
                        }
                    }
                }
            }

            item {
                SettingsSection(title = s.noteSync, icon = Icons.Outlined.Note) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Notion") }
                        )
                        AssistChip(
                            onClick = { },
                            label = { Text("语雀") }
                        )
                        AssistChip(
                            onClick = { },
                            label = { Text("Obsidian") }
                        )
                        AssistChip(
                            onClick = { },
                            label = { Text("Joplin") }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s.noteSyncComingSoon,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            item {
                SettingsSection(title = s.dataReset, icon = Icons.Outlined.DeleteForever) {
                    Text(
                        text = s.resetDataWarning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(s.dataResetComingSoon)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(s.resetAllData)
                    }
                }
            }
        }
    }
}


