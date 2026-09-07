package com.spcrk.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.spcrk.app.data.model.ModelConfig
import com.spcrk.app.data.ModelConfigStore
import com.spcrk.app.ai.presetModelsByProvider
import com.spcrk.app.ai.providerDisplayName
import com.spcrk.app.getAppContainer
import com.spcrk.app.ui.l10n.appStrings
import com.spcrk.app.ui.theme.TechCard
import com.spcrk.app.ui.theme.TechPrimaryButton
import com.spcrk.app.ui.theme.TechSecondaryButton
import com.spcrk.app.ui.theme.TechSwitch
import kotlinx.coroutines.launch

private data class FetchResult(
    val provider: String,
    val models: List<String>
)

private fun providerIcon(provider: String): androidx.compose.ui.graphics.vector.ImageVector = when (provider) {
    "openai" -> Icons.Outlined.Cloud
    "anthropic" -> Icons.Outlined.Psychology
    "gemini" -> Icons.Outlined.CloudQueue
    "ollama" -> Icons.Outlined.Computer
    "agnes-ai" -> Icons.Outlined.Star
    else -> Icons.Outlined.Api
}

private fun providerTags(provider: String): List<String> =
    com.spcrk.app.ai.providerTags[provider].orEmpty()

private fun providerNote(provider: String): String? =
    com.spcrk.app.ai.providerNotes[provider]

private fun maskKey(key: String, notSetLabel: String): String =
    if (key.isBlank()) notSetLabel
    else if (key.length <= 8) "••••••••"
    else "${key.take(4)}••••••••${key.takeLast(4)}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val store = getAppContainer(context).modelConfigStore

    var configs by remember { mutableStateOf(store.loadConfigs()) }
    var selectedProvider by remember { mutableStateOf<String?>(null) }
    var showAddPresetDialog by remember { mutableStateOf(false) }
    var showAddCustomDialog by remember { mutableStateOf(false) }
    val s = appStrings()

    // 主列表顯示全部供應商（ProviderCatalog 60 家），未配置的顯示「尚未配置」
    val providers = com.spcrk.app.ai.ProviderCatalog.all.map { it.id }

    fun refresh() {
        configs = store.loadConfigs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.providersTitle) },
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
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { showAddPresetDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.List, contentDescription = s.addPresetModel)
                }
                FloatingActionButton(onClick = { showAddCustomDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = s.addCustomModel)
                }
            }
        }
    ) { padding ->
        if (providers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        s.noProvidersConfigured,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        s.noProvidersHint,
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
                val sortedProviders = providers.sortedWith(
                    compareByDescending<String> { providerTags(it).contains("recommended") }
                        .thenBy { providerDisplayName(it) }
                )
                items(sortedProviders) { provider ->
                    val models = configs.filter { it.provider == provider }
                    ProviderListCard(
                        provider = provider,
                        models = models,
                        tags = providerTags(provider),
                        onClick = { selectedProvider = provider },
                        onToggleGroup = { enabled ->
                            models.forEach { store.updateConfig(it.copy(isEnabled = enabled)) }
                            refresh()
                        }
                    )
                }
            }
        }
    }

    if (showAddPresetDialog) {
        AddPresetModelDialog(
            store = store,
            providers = providers,
            onDismiss = { showAddPresetDialog = false },
            onAdd = { provider, models ->
                val base = store.getModelsByProvider(provider).firstOrNull()?.baseUrl ?: ""
                val key = store.getModelsByProvider(provider).firstOrNull()?.apiKey ?: ""
                store.addModelsForProvider(provider, base, key, models)
                refresh()
                showAddPresetDialog = false
            }
        )
    }

    if (showAddCustomDialog) {
        AddProviderDialog(
            onDismiss = { showAddCustomDialog = false },
            onAdd = { config ->
                store.addConfig(config)
                refresh()
                showAddCustomDialog = false
            }
        )
    }

    selectedProvider?.let { provider ->
        ProviderDetailScreen(
            provider = provider,
            store = store,
            onBack = { selectedProvider = null },
            onChanged = { refresh() }
        )
    }
}

/**
 * chatbox 式厂商卡片：点击卡片进入详情页，卡片右侧为厂商级启用开关。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderListCard(
    provider: String,
    models: List<ModelConfig>,
    tags: List<String>,
    onClick: () -> Unit,
    onToggleGroup: (Boolean) -> Unit
) {
    val first = models.firstOrNull()
    val hasModels = first != null
    val s = appStrings()
    TechCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = providerIcon(provider),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        com.spcrk.app.ai.ProviderCatalog.displayName(provider),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (tags.contains("recommended")) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(s.recommended, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
                Text(
                    if (hasModels) String.format(s.modelCountFormat, models.size, first?.baseUrl ?: "")
                    else s.notConfiguredAddModel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TechSwitch(
                checked = first?.isEnabled ?: false,
                enabled = hasModels,
                onCheckedChange = onToggleGroup
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = s.enterDetails,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 厂商详情页：厂商启用开关、API 配置编辑、预置模型添加、API 拉取模型、模型列表管理。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderDetailScreen(
    provider: String,
    store: ModelConfigStore,
    onBack: () -> Unit,
    onChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var models by remember { mutableStateOf(store.getModelsByProvider(provider)) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddPresetDialog by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    var fetchResult by remember { mutableStateOf<FetchResult?>(null) }
    val s = appStrings()

    fun refresh() {
        models = store.getModelsByProvider(provider)
        onChanged()
    }

    val groupEnabled = models.firstOrNull()?.isEnabled ?: false
    val baseUrl = models.firstOrNull()?.baseUrl ?: ""
    val apiKey = models.firstOrNull()?.apiKey ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(providerDisplayName(provider)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            // 厂商启用开关
            item {
                TechCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = providerIcon(provider),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.enableProvider, style = MaterialTheme.typography.titleMedium)
                            Text(
                                s.enableProviderDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TechSwitch(
                            checked = groupEnabled,
                            onCheckedChange = { enabled ->
                                models.forEach { store.updateConfig(it.copy(isEnabled = enabled)) }
                                refresh()
                            }
                        )
                    }
                }
            }

            // API 配置
            item {
                TechCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                s.apiConfig,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TechSecondaryButton(
                                text = s.edit,
                                onClick = { showEditDialog = true },
                                modifier = Modifier.height(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Base URL: $baseUrl",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "API Key: ${maskKey(apiKey, s.notConfigured)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 添加 / 获取模型
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TechSecondaryButton(
                        text = s.presetModels,
                        onClick = { showAddPresetDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    TechPrimaryButton(
                        text = if (isFetching) s.fetching else s.fetchModels,
                        onClick = {
                            scope.launch {
                                isFetching = true
                                try {
                                    val fetched = store.fetchModelsFromApi(provider, baseUrl, apiKey)
                                    if (fetched.isEmpty()) {
                                        snackbarHostState.showSnackbar(s.noModelsFetched)
                                    } else {
                                        fetchResult = FetchResult(provider, fetched)
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(e.message ?: s.fetchModelsFailed)
                                } finally {
                                    isFetching = false
                                }
                            }
                        },
                        enabled = !isFetching,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 厂商提示信息
            providerNote(provider)?.let { note ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 模型列表标题
            item {
                Text(
                    String.format(s.modelsTitleFormat, models.size),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (models.isEmpty()) {
                item {
                    Text(
                        s.noModelsHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(models) { model ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            ModelEntryRow(
                                model = model,
                                tags = providerTags(provider),
                                onToggle = {
                                    store.updateConfig(model.copy(isEnabled = !model.isEnabled))
                                    refresh()
                                },
                                onSetDefault = {
                                    store.setDefault(model.id)
                                    refresh()
                                },
                                onDelete = {
                                    store.removeConfig(model.id)
                                    refresh()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditProviderDialog(
            provider = provider,
            baseUrl = baseUrl,
            apiKey = apiKey,
            onDismiss = { showEditDialog = false },
            onSave = { b, k ->
                store.updateProvider(provider, b, k)
                refresh()
                showEditDialog = false
            }
        )
    }

    if (showAddPresetDialog) {
        AddPresetModelDialog(
            store = store,
            providers = listOf(provider),
            onDismiss = { showAddPresetDialog = false },
            onAdd = { p, modelNames ->
                store.addModelsForProvider(p, baseUrl, apiKey, modelNames)
                refresh()
                showAddPresetDialog = false
            }
        )
    }

    fetchResult?.let { result ->
        FetchModelsDialog(
            provider = result.provider,
            models = result.models,
            onDismiss = { fetchResult = null },
            onAddAll = {
                store.addModelsForProvider(result.provider, baseUrl, apiKey, result.models)
                refresh()
                fetchResult = null
                scope.launch { snackbarHostState.showSnackbar(String.format(s.modelsAddedFormat, result.models.size)) }
            }
        )
    }
}

@Composable
private fun ModelEntryRow(
    model: ModelConfig,
    tags: List<String>,
    onToggle: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    val s = appStrings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ModelTraining,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            model.modelName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (tags.contains("free")) {
            AssistChip(
                onClick = {},
                label = { Text(s.free, style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.height(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        if (model.isDefault) {
            AssistChip(
                onClick = onSetDefault,
                label = { Text(s.default) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        TechSwitch(
            checked = model.isEnabled,
            onCheckedChange = { onToggle() }
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = s.delete,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AddPresetModelDialog(
    store: ModelConfigStore,
    providers: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, List<String>) -> Unit
) {
    var provider by remember { mutableStateOf(providers.firstOrNull() ?: "") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var providerExpanded by remember { mutableStateOf(false) }
    val s = appStrings()

    val availableModels = presetModelsByProvider[provider].orEmpty()
    val existingModels = store.getModelsByProvider(provider).map { it.modelName }.toSet()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(s.addPresetModel) },
        text = {
            Column {
                if (providers.isEmpty()) {
                    Text(
                        s.noProviderConfigHint,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    if (providers.size > 1) {
                        ExposedDropdownMenuBox(
                            expanded = providerExpanded,
                            onExpandedChange = { providerExpanded = !providerExpanded }
                        ) {
                            OutlinedTextField(
                                value = providerDisplayName(provider),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(s.provider) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = providerExpanded,
                                onDismissRequest = { providerExpanded = false }
                            ) {
                                providers.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(providerDisplayName(p)) },
                                        onClick = {
                                            provider = p
                                            selected = emptySet()
                                            providerExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (availableModels.isEmpty()) {
                        Text(
                            s.noPresetModels,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            s.selectModelsToAdd,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        availableModels.forEach { modelName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = modelName !in existingModels) {
                                        selected = if (modelName in selected) selected - modelName else selected + modelName
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = modelName in selected,
                                    onCheckedChange = {
                                        selected = if (modelName in selected) selected - modelName else selected + modelName
                                    },
                                    enabled = modelName !in existingModels
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    modelName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (modelName in existingModels)
                                        MaterialTheme.colorScheme.outline
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                if (modelName in existingModels) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        s.alreadyAdded,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TechPrimaryButton(
                text = s.add,
                onClick = {
                    if (selected.isNotEmpty()) {
                        onAdd(provider, selected.toList())
                    }
                },
                enabled = selected.isNotEmpty()
            )
        },
        dismissButton = {
            TechSecondaryButton(
                text = s.cancel,
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun FetchModelsDialog(
    provider: String,
    models: List<String>,
    onDismiss: () -> Unit,
    onAddAll: () -> Unit
) {
    val s = appStrings()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(String.format(s.fetchedModelsTitle, models.size)) },
        text = {
            Column {
                Text(
                    "${s.provider}：${providerDisplayName(provider)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                models.take(20).forEach { modelName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.ModelTraining,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(modelName, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (models.size > 20) {
                    Text(
                        String.format(s.etcModelsFormat, models.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TechPrimaryButton(
                text = s.addAll,
                onClick = onAddAll
            )
        },
        dismissButton = {
            TechSecondaryButton(
                text = s.cancel,
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun AddProviderDialog(
    onDismiss: () -> Unit,
    onAdd: (ModelConfig) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("openai") }
    var baseUrl by remember { mutableStateOf("https://api.openai.com/v1") }
    var apiKey by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("gpt-4o-mini") }
    var expanded by remember { mutableStateOf(false) }
    val s = appStrings()

    val providerTypes = com.spcrk.app.ai.ProviderCatalog.all.map { it.id to it.displayName }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(s.addCustomModel) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = providerTypes.find { it.first == provider }?.second ?: "OpenAI",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s.providerType) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        providerTypes.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    provider = value
                                    expanded = false
                                    baseUrl = com.spcrk.app.ai.ProviderCatalog.baseUrl(value)
                                    modelName = presetModelsByProvider[value]?.firstOrNull() ?: ""
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.name) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text(s.modelName) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    s.addProviderHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TechPrimaryButton(
                text = s.add,
                onClick = {
                    if (name.isNotBlank() && apiKey.isNotBlank() && modelName.isNotBlank()) {
                        val config = ModelConfig(
                            name = modelName,
                            provider = provider,
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            modelName = modelName,
                            isEnabled = false
                        )
                        onAdd(config)
                    }
                }
            )
        },
        dismissButton = {
            TechSecondaryButton(
                text = s.cancel,
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun EditProviderDialog(
    provider: String,
    baseUrl: String,
    apiKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var newBaseUrl by remember { mutableStateOf(baseUrl) }
    var newApiKey by remember { mutableStateOf(apiKey) }
    val s = appStrings()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(String.format(s.editProviderConfig, providerDisplayName(provider))) },
        text = {
            Column {
                OutlinedTextField(
                    value = newBaseUrl,
                    onValueChange = { newBaseUrl = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newApiKey,
                    onValueChange = { newApiKey = it },
                    label = { Text("API Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    s.editProviderHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TechPrimaryButton(
                text = s.save,
                onClick = {
                    if (newBaseUrl.isNotBlank()) {
                        onSave(newBaseUrl, newApiKey)
                    }
                }
            )
        },
        dismissButton = {
            TechSecondaryButton(
                text = s.cancel,
                onClick = onDismiss
            )
        }
    )
}
