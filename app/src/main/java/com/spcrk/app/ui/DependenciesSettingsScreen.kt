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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependenciesSettingsScreen(
    onBackClick: () -> Unit
) {
    val settingsStore = getAppContainer(LocalContext.current).settingsStore

    val pythonPath by settingsStore.pythonPathFlow.collectAsState(initial = "")
    val nodePath by settingsStore.nodePathFlow.collectAsState(initial = "")
    val ollamaUrl by settingsStore.ollamaUrlFlow.collectAsState(initial = "http://localhost:11434")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("依赖设置") },
                navigationIcon = {
                    IconButton(onClick = {},
modifier = Modifier.techRipple(onClick = onBackClick)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                    title = "Python 环境",
                    description = "配置 Python 解释器路径，用于运行 MCP 服务器",
                    icon = Icons.Outlined.Code,
                    status = if (pythonPath.isNotEmpty()) "已配置" else "未配置"
                ) {
                    OutlinedTextField(
                        value = pythonPath,
                        onValueChange = { settingsStore.setPythonPath(it) },
                        label = { Text("Python 路径") },
                        placeholder = { Text("/usr/bin/python3") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                DependencyCard(
                    title = "Node.js 环境",
                    description = "配置 Node.js 路径，用于运行 npx 命令",
                    icon = Icons.Outlined.Javascript,
                    status = if (nodePath.isNotEmpty()) "已配置" else "未配置"
                ) {
                    OutlinedTextField(
                        value = nodePath,
                        onValueChange = { settingsStore.setNodePath(it) },
                        label = { Text("Node.js 路径") },
                        placeholder = { Text("/usr/bin/node") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                DependencyCard(
                    title = "Ollama 本地模型",
                    description = "配置 Ollama 服务地址，使用本地大模型",
                    icon = Icons.Outlined.Computer,
                    status = if (ollamaUrl.isNotEmpty()) "已配置" else "未配置"
                ) {
                    OutlinedTextField(
                        value = ollamaUrl,
                        onValueChange = { settingsStore.setOllamaUrl(it) },
                        label = { Text("Ollama URL") },
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
                            Text("测试连接")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = {},
modifier = Modifier.techRipple(onClick = { })) {
                            Text("拉取模型")
                        }
                    }
                }
            }

            item {
                DependencyCard(
                    title = "LM Studio",
                    description = "配置 LM Studio 服务地址",
                    icon = Icons.Outlined.Memory,
                    status = "未配置"
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        label = { Text("LM Studio URL") },
                        placeholder = { Text("http://localhost:1234") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                DependencyCard(
                    title = "环境依赖检查",
                    description = "检查所有环境依赖是否满足运行要求",
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
