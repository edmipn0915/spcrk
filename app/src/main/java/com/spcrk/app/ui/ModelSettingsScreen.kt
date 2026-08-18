package com.spcrk.app.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.spcrk.app.ai.ModelConfig
import com.spcrk.app.ai.providerDisplayName
import com.spcrk.app.data.ModelConfigStore
import com.spcrk.app.data.SettingsStore
import com.spcrk.app.ui.theme.TechCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val modelStore = remember { ModelConfigStore(context) }
    val settingsStore = remember { SettingsStore.getInstance() }
    val configs = remember { modelStore.getEnabledConfigs() }

    val defaultModelId by settingsStore.defaultModelIdFlow.collectAsState(initial = "")
    val quickModelId by settingsStore.quickModelIdFlow.collectAsState(initial = "")
    val translateModelId by settingsStore.translateModelIdFlow.collectAsState(initial = "")
    val topicNamingModelId by settingsStore.topicNamingModelIdFlow.collectAsState(initial = "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模型设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
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
                ModelSelectorCard(
                    title = "默认模型",
                    subtitle = "新建对话时使用的模型",
                    icon = Icons.Outlined.Chat,
                    configs = configs,
                    selectedId = defaultModelId,
                    onSelect = { settingsStore.setDefaultModelId(it) }
                )
            }
            item {
                ModelSelectorCard(
                    title = "快速模型",
                    subtitle = "快捷回复使用的模型",
                    icon = Icons.Outlined.Bolt,
                    configs = configs,
                    selectedId = quickModelId,
                    onSelect = { settingsStore.setQuickModelId(it) }
                )
            }
            item {
                ModelSelectorCard(
                    title = "翻译模型",
                    subtitle = "翻译功能使用的模型",
                    icon = Icons.Outlined.Translate,
                    configs = configs,
                    selectedId = translateModelId,
                    onSelect = { settingsStore.setTranslateModelId(it) }
                )
            }
            item {
                ModelSelectorCard(
                    title = "话题命名模型",
                    subtitle = "自动生成话题标题使用的模型",
                    icon = Icons.Outlined.Title,
                    configs = configs,
                    selectedId = topicNamingModelId,
                    onSelect = { settingsStore.setTopicNamingModelId(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelectorCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    configs: List<ModelConfig>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TechCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = configs.find { it.id == selectedId }?.modelName ?: "未选择",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    configs.forEach { config ->
                        DropdownMenuItem(
                            text = { Text("${config.modelName} (${providerDisplayName(config.provider)})") },
                            onClick = {
                                onSelect(config.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
