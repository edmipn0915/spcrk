package com.spcrk.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import com.spcrk.app.ui.theme.techRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.spcrk.app.getAppContainer
import com.spcrk.app.ui.l10n.appStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependenciesSettingsScreen(
    onBackClick: () -> Unit
) {
    val settingsStore = getAppContainer(LocalContext.current).settingsStore
    val s = appStrings()

    val pythonPath by settingsStore.pythonPathFlow.collectAsState(initial = "")
    val nodePath by settingsStore.nodePathFlow.collectAsState(initial = "")
    val ollamaUrl by settingsStore.ollamaUrlFlow.collectAsState(initial = "http://localhost:11434")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.dependenciesTitle) },
                navigationIcon = {
                    IconButton(onClick = {},
modifier = Modifier.techRipple(onClick = onBackClick)) {
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
                DependencyCard(
                    title = s.pythonEnvironment,
                    description = s.pythonEnvironmentDesc,
                    icon = Icons.Outlined.Code,
                    status = if (pythonPath.isNotEmpty()) s.configured else s.notConfigured
                ) {
                    OutlinedTextField(
                        value = pythonPath,
                        onValueChange = { settingsStore.setPythonPath(it) },
                        label = { Text(s.pythonPath) },
                        placeholder = { Text("/usr/bin/python3") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                DependencyCard(
                    title = s.nodeJsEnvironment,
                    description = s.nodeJsEnvironmentDesc,
                    icon = Icons.Outlined.Javascript,
                    status = if (nodePath.isNotEmpty()) s.configured else s.notConfigured
                ) {
                    OutlinedTextField(
                        value = nodePath,
                        onValueChange = { settingsStore.setNodePath(it) },
                        label = { Text(s.nodeJsPath) },
                        placeholder = { Text("/usr/bin/node") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                DependencyCard(
                    title = s.ollamaLocalModel,
                    description = s.ollamaLocalModelDesc,
                    icon = Icons.Outlined.Computer,
                    status = if (ollamaUrl.isNotEmpty()) s.configured else s.notConfigured
                ) {
                    OutlinedTextField(
                        value = ollamaUrl,
                        onValueChange = { settingsStore.setOllamaUrl(it) },
                        label = { Text(s.ollamaUrl) },
                        placeholder = { Text("http://localhost:11434") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = {},
modifier = Modifier.techRipple(onClick = { })) {
                            Text(s.testConnection)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = {},
modifier = Modifier.techRipple(onClick = { })) {
                            Text(s.pullModel)
                        }
                    }
                }
            }

            item {
                DependencyCard(
                    title = s.lmStudio,
                    description = s.lmStudioDesc,
                    icon = Icons.Outlined.Memory,
                    status = s.notConfigured
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        label = { Text(s.lmStudioUrl) },
                        placeholder = { Text("http://localhost:1234") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                DependencyCard(
                    title = s.environmentCheck,
                    description = s.environmentCheckDesc,
                    icon = Icons.Outlined.CheckCircle,
                    status = ""
                ) {
                    EnvironmentCheckRow("Python", pythonPath.isNotEmpty())
                    EnvironmentCheckRow("Node.js", nodePath.isNotEmpty())
                    EnvironmentCheckRow("Ollama", ollamaUrl.isNotEmpty())
                    EnvironmentCheckRow("Git", false)
                }
            }
        }
    }
}

@Composable
private fun DependencyCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    status: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (status.isNotEmpty()) {
                    AssistChip(
                        onClick = { },
                        label = { Text(status) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EnvironmentCheckRow(name: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isOk) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
            contentDescription = null,
            tint = if (isOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(name, style = MaterialTheme.typography.bodyMedium)
    }
}
