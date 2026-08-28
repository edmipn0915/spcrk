package com.spcrk.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spcrk.app.data.model.ModelConfig
import com.spcrk.app.ai.PresetModels
import com.spcrk.app.ui.theme.LocalSuccessColor
import com.spcrk.app.ui.theme.TechCard
import com.spcrk.app.ui.theme.TechPrimaryButton
import com.spcrk.app.ui.theme.TechSecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManageScreen(
    onBackClick: () -> Unit,
    viewModel: ModelManageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模型管理") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加模型")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PresetProvidersSection(
                onAddPreset = { viewModel.addPresetModel(it) },
                onDiscoverOllama = { viewModel.discoverOllamaModels(it) },
                existingProviders = uiState.configs.map { it.provider }.toSet()
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.configs) { config ->
                    ModelConfigCard(
                        config = config,
                        isDefault = uiState.defaultModelId == config.id || config.isDefault,
                        onEdit = { viewModel.editConfig(config) },
                        onDelete = { viewModel.removeConfig(config.id) },
                        onSetDefault = { viewModel.setDefault(config.id) },
                        onTest = { viewModel.testConnection(config) },
                        isTesting = uiState.isTesting,
                        testResult = uiState.testResult[config.id] ?: ""
                    )
                }
            }
        }

        if (uiState.showAddDialog || uiState.editingConfig != null) {
            ModelConfigDialog(
                config = uiState.editingConfig,
                onDismiss = { viewModel.hideAddDialog() },
                onSave = { config ->
                    if (uiState.editingConfig != null) {
                        viewModel.updateConfig(config)
                    } else {
                        viewModel.addConfig(config)
                    }
                }
            )
        }

        if (uiState.showOllamaDialog) {
            OllamaModelsDialog(
                models = uiState.ollamaModels,
                isDiscovering = uiState.isDiscovering,
                onDismiss = { viewModel.hideOllamaDialog() },
                onSelectModel = { modelName -> viewModel.addOllamaModel(modelName, "http://localhost:11434/v1") }
            )
        }
    }
}

@Composable
fun PresetProvidersSection(
    onAddPreset: (String) -> Unit,
    onDiscoverOllama: (String) -> Unit,
    existingProviders: Set<String>
) {
    val presets = PresetModels.presets
    val displayPresets = presets.filter { it.key != "ollama" }

    TechCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "快速添加预设模型",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayPresets.entries.toList()) { (key, preset) ->
                    val isAdded = existingProviders.contains(key)
                    if (isAdded) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(preset.name, style = MaterialTheme.typography.bodySmall) },
                            icon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    } else {
                        AssistChip(
                            onClick = { onAddPreset(key) },
                            label = { Text(preset.name, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }

            if (!existingProviders.contains("ollama")) {
                Spacer(modifier = Modifier.height(8.dp))
                TechSecondaryButton(
                    text = "发现 Ollama 本地模型",
                    onClick = { onDiscoverOllama("http://localhost:11434") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ModelConfigCard(
    config: ModelConfig,
    isDefault: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
    onTest: () -> Unit,
    isTesting: Boolean,
    testResult: String
) {
    TechCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isDefault) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("默认") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Text(
                text = config.modelName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = config.baseUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TechSecondaryButton(
                    text = if (isTesting) "测试中" else "测试",
                    onClick = onTest,
                    enabled = !isTesting,
                    modifier = Modifier.height(36.dp)
                )
                TechSecondaryButton(
                    text = "编辑",
                    onClick = onEdit,
                    modifier = Modifier.height(36.dp)
                )
                TechSecondaryButton(
                    text = "默认",
                    onClick = onSetDefault,
                    modifier = Modifier.height(36.dp)
                )
                TechSecondaryButton(
                    text = "删除",
                    onClick = onDelete,
                    modifier = Modifier.height(36.dp)
                )
            }
            if (testResult.isNotEmpty()) {
                Text(
                    text = testResult,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (testResult.contains("成功")) LocalSuccessColor.current else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ModelConfigDialog(
    config: ModelConfig?,
    onDismiss: () -> Unit,
    onSave: (ModelConfig) -> Unit
) {
    var name by remember { mutableStateOf(config?.name ?: "") }
    var provider by remember { mutableStateOf(config?.provider ?: "custom") }
    var apiKey by remember { mutableStateOf(config?.apiKey ?: "") }
    var baseUrl by remember { mutableStateOf(config?.baseUrl ?: "https://api.openai.com/v1") }
    var modelName by remember { mutableStateOf(config?.modelName ?: "") }
    var temperature by remember { mutableStateOf(config?.temperature?.toString() ?: "0.7") }
    var maxTokens by remember { mutableStateOf(config?.maxTokens?.toString() ?: "4096") }
    var timeout by remember { mutableStateOf(config?.timeout?.toString() ?: "60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                if (config == null) "添加模型" else "编辑模型",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("提供商") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("模型名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("温度") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = maxTokens,
                    onValueChange = { maxTokens = it },
                    label = { Text("最大 Token") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = timeout,
                    onValueChange = { timeout = it },
                    label = { Text("超时(秒)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TechPrimaryButton(
                text = "保存",
                onClick = {
                    onSave(
                        ModelConfig(
                            id = config?.id ?: java.util.UUID.randomUUID().toString(),
                            name = name,
                            provider = provider,
                            apiKey = apiKey,
                            baseUrl = baseUrl,
                            modelName = modelName,
                            temperature = temperature.toFloatOrNull() ?: 0.7f,
                            maxTokens = maxTokens.toIntOrNull() ?: 4096,
                            isEnabled = config?.isEnabled ?: true,
                            isDefault = config?.isDefault ?: false,
                            timeout = timeout.toIntOrNull() ?: 60,
                            createdAt = config?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
            )
        },
        dismissButton = {
            TechSecondaryButton(
                text = "取消",
                onClick = onDismiss
            )
        }
    )
}

@Composable
fun OllamaModelsDialog(
    models: List<OllamaModelInfo>,
    isDiscovering: Boolean,
    onDismiss: () -> Unit,
    onSelectModel: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("选择 Ollama 模型", color = MaterialTheme.colorScheme.onSurface)
        },
        text = {
            when {
                isDiscovering -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("正在发现本地模型...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                models.isEmpty() -> {
                    Text("未发现可用模型", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(models) { model ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectModel(model.name) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = model.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = formatSize(model.size),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TechSecondaryButton(
                text = "关闭",
                onClick = onDismiss
            )
        }
    )
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.1f KB", bytes / 1_000.0)
        else -> "$bytes B"
    }
}
