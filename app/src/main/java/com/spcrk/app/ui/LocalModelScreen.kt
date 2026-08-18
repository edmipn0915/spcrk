package com.spcrk.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spcrk.app.ai.EmbeddingModelInfo
import com.spcrk.app.ai.LocalModelInfo
import com.spcrk.app.ai.LocalModelManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalModelScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val modelManager = remember { LocalModelManager(context) }

    var selectedTab by remember { mutableStateOf(0) }
    var embeddingModels by remember { mutableStateOf(modelManager.getAvailableEmbeddingModels()) }
    var llmModels by remember { mutableStateOf(modelManager.getAvailableLLMModels()) }
    var downloadedModels by remember { mutableStateOf(modelManager.getDownloadedModels()) }
    var downloadingIds by remember { mutableStateOf(setOf<String>()) }
    var downloadProgress by remember { mutableStateOf(mapOf<String, Float>()) }
    var showImportMenu by remember { mutableStateOf(false) }
    var showUrlDownloadDialog by remember { mutableStateOf(false) }
    var downloadUrl by remember { mutableStateOf("") }
    var downloadFileName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    fun refreshModels() {
        embeddingModels = modelManager.getAvailableEmbeddingModels()
        llmModels = modelManager.getAvailableLLMModels()
        downloadedModels = modelManager.getDownloadedModels()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val success = modelManager.importLocalFile(it)
                if (success) {
                    snackbarHostState.showSnackbar("模型导入成功")
                    refreshModels()
                } else {
                    snackbarHostState.showSnackbar("模型导入失败")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("本地模型管理") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showImportMenu = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加模型")
                }
                DropdownMenu(
                    expanded = showImportMenu,
                    onDismissRequest = { showImportMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("从 Hugging Face 下载") },
                        onClick = {
                            showImportMenu = false
                            downloadUrl = "https://huggingface.co/models?search=GGUF&sort=downloads"
                            downloadFileName = ""
                            showUrlDownloadDialog = true
                        },
                        leadingIcon = { Icon(Icons.Outlined.CloudDownload, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("从 ModelScope 下载") },
                        onClick = {
                            showImportMenu = false
                            downloadUrl = "https://modelscope.cn/models?search=GGUF"
                            downloadFileName = ""
                            showUrlDownloadDialog = true
                        },
                        leadingIcon = { Icon(Icons.Outlined.CloudDownload, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("导入本地 GGUF") },
                        onClick = {
                            showImportMenu = false
                            filePickerLauncher.launch("*/*")
                        },
                        leadingIcon = { Icon(Icons.Outlined.FolderOpen, contentDescription = null) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("嵌入模型") },
                    icon = { Icon(Icons.Outlined.Schema, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("LLM 模型") },
                    icon = { Icon(Icons.Outlined.Psychology, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("已下载") },
                    icon = { Icon(Icons.Outlined.DownloadDone, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> EmbeddingModelTab(
                    models = embeddingModels,
                    downloadedModels = downloadedModels,
                    downloadingIds = downloadingIds,
                    downloadProgress = downloadProgress,
                    onDownload = { model ->
                        val fileName = "${model.id}.gguf"
                        if (modelManager.isModelDownloaded(fileName)) {
                            scope.launch {
                                snackbarHostState.showSnackbar("模型已存在")
                            }
                        } else {
                            downloadingIds = downloadingIds + model.id
                            scope.launch {
                                modelManager.downloadModel(
                                    url = model.downloadUrl,
                                    fileName = fileName,
                                    onProgress = { progress ->
                                        downloadProgress = downloadProgress + (model.id to progress)
                                    },
                                    onComplete = { success ->
                                        downloadingIds = downloadingIds - model.id
                                        downloadProgress = downloadProgress - model.id
                                        scope.launch {
                                            if (success) {
                                                snackbarHostState.showSnackbar("${model.name} 下载完成")
                                                refreshModels()
                                            } else {
                                                snackbarHostState.showSnackbar("${model.name} 下载失败")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    },
                    onDelete = { model ->
                        val fileName = "${model.id}.gguf"
                        if (modelManager.deleteModel(fileName)) {
                            scope.launch {
                                snackbarHostState.showSnackbar("${model.name} 已删除")
                            }
                            refreshModels()
                        }
                    }
                )
                1 -> LLMModelTab(
                    models = llmModels,
                    downloadedModels = downloadedModels,
                    downloadingIds = downloadingIds,
                    downloadProgress = downloadProgress,
                    onDownload = { model ->
                        val fileName = "${model.id}.gguf"
                        if (modelManager.isModelDownloaded(fileName)) {
                            scope.launch {
                                snackbarHostState.showSnackbar("模型已存在")
                            }
                        } else {
                            downloadingIds = downloadingIds + model.id
                            scope.launch {
                                modelManager.downloadModel(
                                    url = model.downloadUrl,
                                    fileName = fileName,
                                    onProgress = { progress ->
                                        downloadProgress = downloadProgress + (model.id to progress)
                                    },
                                    onComplete = { success ->
                                        downloadingIds = downloadingIds - model.id
                                        downloadProgress = downloadProgress - model.id
                                        scope.launch {
                                            if (success) {
                                                snackbarHostState.showSnackbar("${model.name} 下载完成")
                                                refreshModels()
                                            } else {
                                                snackbarHostState.showSnackbar("${model.name} 下载失败")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    },
                    onDelete = { model ->
                        val fileName = "${model.id}.gguf"
                        if (modelManager.deleteModel(fileName)) {
                            scope.launch {
                                snackbarHostState.showSnackbar("${model.name} 已删除")
                            }
                            refreshModels()
                        }
                    }
                )
                2 -> DownloadedModelTab(
                    models = downloadedModels,
                    onDelete = { model ->
                        if (modelManager.deleteModel(model.filePath.substringAfterLast("/"))) {
                            scope.launch {
                                snackbarHostState.showSnackbar("${model.name} 已删除")
                            }
                            refreshModels()
                        }
                    }
                )
            }
        }
    }

    if (showUrlDownloadDialog) {
        UrlDownloadDialog(
            initialUrl = downloadUrl,
            onDismiss = { showUrlDownloadDialog = false },
            onConfirm = { url, fileName ->
                showUrlDownloadDialog = false
                val actualFileName = if (fileName.isBlank()) "downloaded_model.gguf" else fileName
                downloadingIds = downloadingIds + actualFileName
                scope.launch {
                    modelManager.downloadModel(
                        url = url,
                        fileName = actualFileName,
                        onProgress = { progress ->
                            downloadProgress = downloadProgress + (actualFileName to progress)
                        },
                        onComplete = { success ->
                            downloadingIds = downloadingIds - actualFileName
                            downloadProgress = downloadProgress - actualFileName
                            scope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("下载完成")
                                    refreshModels()
                                } else {
                                    snackbarHostState.showSnackbar("下载失败")
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun EmbeddingModelTab(
    models: List<EmbeddingModelInfo>,
    downloadedModels: List<LocalModelInfo>,
    downloadingIds: Set<String>,
    downloadProgress: Map<String, Float>,
    onDownload: (EmbeddingModelInfo) -> Unit,
    onDelete: (EmbeddingModelInfo) -> Unit
) {
    if (models.isEmpty()) {
        EmptyStateView("暂无可用的嵌入模型")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(models) { model ->
                val isDownloaded = downloadedModels.any { it.id == model.id && it.status == "ready" }
                val isDownloading = downloadingIds.contains(model.id)
                val progress = downloadProgress[model.id] ?: 0f
                ModelCard(
                    name = model.name,
                    sizeMB = model.sizeMB,
                    isRecommended = model.isRecommended,
                    isDownloaded = isDownloaded,
                    isDownloading = isDownloading,
                    progress = progress,
                    onDownload = { onDownload(model) },
                    onDelete = { onDelete(model) }
                )
            }
        }
    }
}

@Composable
private fun LLMModelTab(
    models: List<EmbeddingModelInfo>,
    downloadedModels: List<LocalModelInfo>,
    downloadingIds: Set<String>,
    downloadProgress: Map<String, Float>,
    onDownload: (EmbeddingModelInfo) -> Unit,
    onDelete: (EmbeddingModelInfo) -> Unit
) {
    if (models.isEmpty()) {
        EmptyStateView("暂无可用的 LLM 模型")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(models) { model ->
                val isDownloaded = downloadedModels.any { it.id == model.id && it.status == "ready" }
                val isDownloading = downloadingIds.contains(model.id)
                val progress = downloadProgress[model.id] ?: 0f
                ModelCard(
                    name = model.name,
                    sizeMB = model.sizeMB,
                    isRecommended = model.isRecommended,
                    isDownloaded = isDownloaded,
                    isDownloading = isDownloading,
                    progress = progress,
                    onDownload = { onDownload(model) },
                    onDelete = { onDelete(model) }
                )
            }
        }
    }
}

@Composable
private fun DownloadedModelTab(
    models: List<LocalModelInfo>,
    onDelete: (LocalModelInfo) -> Unit
) {
    if (models.isEmpty()) {
        EmptyStateView("暂无已下载的模型")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(models) { model ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (model.type == "embedding") Icons.Outlined.Schema else Icons.Outlined.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(model.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${model.sizeMB} MB · ${if (model.type == "embedding") "嵌入模型" else "LLM"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDelete(model) }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelCard(
    name: String,
    sizeMB: Int,
    isRecommended: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    progress: Float,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, style = MaterialTheme.typography.titleMedium)
                        if (isRecommended) {
                            Spacer(modifier = Modifier.width(8.dp))
                            SuggestionChip(
                                onClick = {},
                                label = { Text("推荐", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(20.dp)
                            )
                        }
                    }
                    Text(
                        "$sizeMB MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                when {
                    isDownloading -> {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    isDownloaded -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AssistChip(
                                onClick = {},
                                label = { Text("已下载", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onDelete) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    else -> {
                        OutlinedButton(onClick = onDownload) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("下载")
                        }
                    }
                }
            }

            if (isDownloading) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyStateView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UrlDownloadDialog(
    initialUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var url by remember { mutableStateOf(initialUrl) }
    var fileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("下载模型") },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("下载链接") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("文件名 (可选)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "支持从 Hugging Face 或 ModelScope 下载 GGUF 格式模型",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (url.isNotBlank()) onConfirm(url, fileName) },
                enabled = url.isNotBlank()
            ) {
                Text("下载")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
