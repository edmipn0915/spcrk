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
import com.spcrk.app.data.SettingsStore
import com.spcrk.app.data.ModelConfigStore
import com.spcrk.app.data.Repository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val settingsStore = remember { SettingsStore.getInstance() }
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
                title = { Text("数据管理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                SettingsSection(title = "备份设置", icon = Icons.Outlined.Backup) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("自动备份", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "定期自动备份应用数据",
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
                        label = { Text("备份路径") },
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
                                snackbarHostState.showSnackbar("备份功能即将推出")
                            }
                        }) {
                            Text("立即备份")
                        }
                    }
                }
            }

            item {
                SettingsSection(title = "云存储 (WebDAV)", icon = Icons.Outlined.Cloud) {
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
                        label = { Text("用户名") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = webDavPassword,
                        onValueChange = { settingsStore.setWebDavPassword(it) },
                        label = { Text("密码") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("WebDAV 功能即将推出")
                            }
                        }) {
                            Text("测试连接")
                        }
                    }
                }
            }

            item {
                SettingsSection(title = "数据导入导出", icon = Icons.Outlined.ImportExport) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("导入功能即将推出")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.FileOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导入")
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("导出功能即将推出")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.SaveAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出")
                        }
                    }
                }
            }

            item {
                SettingsSection(title = "笔记同步", icon = Icons.Outlined.Note) {
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
                        text = "笔记同步功能即将推出",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            item {
                SettingsSection(title = "数据重置", icon = Icons.Outlined.DeleteForever) {
                    Text(
                        text = "清除所有应用数据，操作不可恢复",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("数据重置功能即将推出")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("重置所有数据")
                    }
                }
            }
        }
    }
}


