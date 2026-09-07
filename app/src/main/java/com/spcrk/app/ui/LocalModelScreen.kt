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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spcrk.app.ai.api.EmbeddingModelInfo
import com.spcrk.app.ai.api.LocalModelInfo
import com.spcrk.app.ui.l10n.appStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalModelScreen(
    onBackClick: () -> Unit,
    viewModel: LocalModelViewModel = viewModel(factory = LocalModelViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val s = appStrings()

    var selectedTab by remember { mutableStateOf(0) }
    var showImportMenu by remember { mutableStateOf(false) }
    var showUrlDownloadDialog by remember { mutableStateOf(false) }
    var downloadUrl by remember { mutableStateOf("") }
    var downloadFileName by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val success = viewModel.importLocalFile(it)
                if (success) {
                    snackbarHostState.showSnackbar(s.modelImportSuccess)
                } else {
                    snackbarHostState.showSnackbar(s.modelImportFailed)
                }
            }
        }
    }

    fun downloadModelById(
        modelId: String,
        url: String,
        fileName: String
    ) {
        scope.launch {
            val alreadyDownloaded = viewModel.isModelDownloaded(fileName)
            if (alreadyDownloaded) {
                snackbarHostState.showSnackbar(s.modelExists)
                return@launch
            }
            viewModel.downloadModel(
                url = url,
                fileName = fileName,
                onProgress = { _, progress ->
                    // Progress is tracked internally via uiState
                },
                onComplete = { _, success ->
                    scope.launch {
                        if (success) {
                            snackbarHostState.showSnackbar(s.downloadComplete)
                        } else {
                            snackbarHostState.showSnackbar(s.downloadFailed)
                        }
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.localModelsManagerTitle) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showImportMenu = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = s.addModel)
                }
                DropdownMenu(
                    expanded = showImportMenu,
                    onDismissRequest = { showImportMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(s.downloadFromHuggingFace) },
                        onClick = {
                            showImportMenu = false
                            downloadUrl = "https://huggingface.co/models?search=GGUF&sort=downloads"
                            downloadFileName = ""
                            showUrlDownloadDialog = true
                        },
                        leadingIcon = { Icon(Icons.Outlined.CloudDownload, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(s.downloadFromModelScope) },
                        onClick = {
                            showImportMenu = false
                            downloadUrl = "https://modelscope.cn/models?search=GGUF"
                            downloadFileName = ""
                            showUrlDownloadDialog = true
                        },
                        leadingIcon = { Icon(Icons.Outlined.CloudDownload, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(s.importLocalGguf) },
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
                    text = { Text(s.embeddingModel) },
                    icon = { Icon(Icons.Outlined.Schema, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(s.llmModel) },
                    icon = { Icon(Icons.Outlined.Psychology, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(s.downloaded) },
                    icon = { Icon(Icons.Outlined.DownloadDone, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> EmbeddingModelTab(
                    models = uiState.embeddingModels,
                    downloadedModels = uiState.downloadedModels,
                    downloadingIds = uiState.downloadingIds,
                    downloadProgress = uiState.downloadProgress,
                    onDownload = { model ->
                        val fileName = "${model.id}.gguf"
                        val url = model.downloadUrl
                        downloadModelById(model.id, url, fileName)
                    },
                    onDelete = { model ->
                        val fileName = "${model.id}.gguf"
                        if (viewModel.deleteModel(fileName)) {
                            scope.launch {
                                snackbarHostState.showSnackbar("${model.name} ${s.deleted}")
                            }
                        }
                    }
                )
                1 -> LLMModelTab(
                    models = uiState.llmModels,
                    downloadedModels = uiState.downloadedModels,
                    downloadingIds = uiState.downloadingIds,
                    downloadProgress = uiState.downloadProgress,
                    onDownload = { model ->
                        val fileName = "${model.id}.gguf"
                        val url = model.downloadUrl
                        downloadModelById(model.id, url, fileName)
                    },
                    onDelete = { model ->
                        val fileName = "${model.id}.gguf"
                        if (viewModel.deleteModel(fileName)) {
                            scope.launch {
                                snackbarHostState.showSnackbar("${model.name} ${s.deleted}")
                            }
                        }
                    }
                )
                2 -> DownloadedModelTab(
                    models = uiState.downloadedModels,
                    onDelete = { model ->
                        val fileName = model.filePath.substringAfterLast("/")
                        if (viewModel.deleteModel(fileName)) {
                            scope.launch {
                                snackbarHostState.showSnackbar("${model.name} ${s.deleted}")
                            }
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
                viewModel.downloadModel(
                    url = url,
                    fileName = actualFileName,
                    onProgress = { _, _ -> },
                    onComplete = { _, success ->
                        if (success) {
                            scope.launch {
                                snackbarHostState.showSnackbar(s.downloadComplete)
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(s.downloadFailed)
                            }
                        }
                    }
                )
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
    val s = appStrings()
    if (models.isEmpty()) {
        EmptyStateView(s.noEmbeddingModels)
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
    val s = appStrings()
    if (models.isEmpty()) {
        EmptyStateView(s.noLlmModels)
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
    val s = appStrings()
    if (models.isEmpty()) {
        EmptyStateView(s.noDownloadedModels)
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
                                "${model.sizeMB} MB · ${if (model.type == "embedding") s.embeddingModel else "LLM"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDelete(model) }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = s.delete,
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
    val s = appStrings()
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
                                label = { Text(s.recommended, style = MaterialTheme.typography.labelSmall) },
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
                                label = { Text(s.downloaded, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onDelete) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = s.delete,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    else -> {
                        OutlinedButton(onClick = onDownload) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.download)
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
    val s = appStrings()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.downloadModelTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(s.downloadUrlLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text(s.fileNameOptional) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    s.ggufDownloadHint,
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
                Text(s.download)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel)
            }
        }
    )
}
