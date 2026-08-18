package com.spcrk.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spcrk.app.ai.DocumentManager
import com.spcrk.app.data.KnowledgeCategory
import com.spcrk.app.data.KnowledgeDocument
import com.spcrk.app.data.Repository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeScreen(navController: NavController) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val repository = remember { Repository(applicationContext) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showCategorySelector by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var uploadCategoryId by remember { mutableStateOf<Long?>(null) }

    val categories by repository.getAllCategories().collectAsState(initial = emptyList())

    val documents by when {
        isSearching && searchQuery.isNotEmpty() -> repository.searchKnowledgeDocuments(searchQuery)
        selectedCategoryId != null -> repository.getKnowledgeDocumentsByCategory(selectedCategoryId!!)
        else -> repository.getAllKnowledgeDocuments()
    }.collectAsState(initial = emptyList())

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val docManager = DocumentManager(applicationContext)
                    val docInfo = docManager.loadDocument(it)
                    repository.addKnowledgeDocument(
                        KnowledgeDocument(
                            title = docInfo.name,
                            content = docInfo.content,
                            filePath = docInfo.uri,
                            fileType = docInfo.name.substringAfterLast('.', "unknown"),
                            chunkCount = 1,
                            categoryId = uploadCategoryId
                        )
                    )
                    uploadCategoryId = null
                } catch (e: Exception) {
                    errorMessage = e.message ?: "文件上传失败"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("知识库") },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showCategorySelector) {
                    FloatingActionButton(
                        onClick = { showAddCategoryDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "新建分类")
                    }
                    FloatingActionButton(
                        onClick = {
                            showCategorySelector = false
                            launcher.launch("*/*")
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.NoteAdd, contentDescription="上传文档")
                    }
                }
                FloatingActionButton(onClick = {
                    if (showCategorySelector) {
                        showCategorySelector = false
                    } else {
                        showCategorySelector = true
                    }
                }) {
                    Icon(
                        if (showCategorySelector) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "添加"
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("全部") }
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(category.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "移除分类",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        scope.launch {
                                            repository.deleteCategory(category)
                                            if (selectedCategoryId == category.id) {
                                                selectedCategoryId = null
                                            }
                                        }
                                    }
                            )
                        }
                    )
                }
            }

            if (isSearching) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("搜索文档...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                    },
                    singleLine = true
                )
            }

            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (if (isSearching) "未找到相关文档" else if (selectedCategoryId != null) "该分类下暂无文档" else "知识库为空"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (!isSearching && selectedCategoryId == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击右下角按钮上传文档或新建分类",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(documents) { doc ->
                        KnowledgeDocumentItem(
                            document = doc,
                            category = categories.find { it.id == doc.categoryId },
                            onDelete = {
                                scope.launch {
                                    repository.deleteKnowledgeDocument(doc)
                                }
                            },
                            onClick = {}
                        )
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, type ->
                scope.launch {
                    repository.addCategory(
                        KnowledgeCategory(name = name, type = type)
                    )
                }
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("rag") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建分类") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("分类类型", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = type == "rag",
                        onClick = { type = "rag" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("RAG 模式", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "直接检索文档片段注入上下文",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = type == "embedding",
                        onClick = { type = "embedding" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("嵌入模式", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "使用嵌入模型向量化后语义检索",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name, type) },
                enabled = name.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun KnowledgeDocumentItem(
    document: KnowledgeDocument,
    category: KnowledgeCategory?,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getFileTypeIcon(document.fileType),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = document.fileType.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (category != null) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "${document.chunkCount} 个片段",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除文档") },
            text = { Text("确定要删除「${document.title}」吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun getFileTypeIcon(fileType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (fileType.lowercase()) {
        "pdf" -> Icons.Default.PictureAsPdf
        "docx", "doc" -> Icons.Default.Description
        "md", "txt" -> Icons.Default.Article
        "json", "xml", "html" -> Icons.Default.Code
        else -> Icons.Default.InsertDriveFile
    }
}
