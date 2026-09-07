package com.spcrk.app.ui

import android.app.Application
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spcrk.app.data.KnowledgeCategory
import com.spcrk.app.data.KnowledgeDocument
import com.spcrk.app.ui.l10n.appStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeScreen(
    navController: androidx.navigation.NavController,
    viewModel: KnowledgeViewModel = viewModel(factory = KnowledgeViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val s = appStrings()
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadDocument(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.knowledge) },
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch(!uiState.isSearching) }) {
                        Icon(Icons.Default.Search, contentDescription = s.aiHubSearch)
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (uiState.showCategorySelector) {
                    FloatingActionButton(
                        onClick = { viewModel.showAddCategoryDialog(true) },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = s.newCategory)
                    }
                    FloatingActionButton(
                        onClick = {
                            viewModel.setShowCategorySelector(false)
                            launcher.launch("*/*")
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.NoteAdd, contentDescription = s.uploadDocument)
                    }
                }
                FloatingActionButton(onClick = {
                    viewModel.setShowCategorySelector(!uiState.showCategorySelector)
                }) {
                    Icon(
                        if (uiState.showCategorySelector) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = s.add
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
                        selected = uiState.selectedCategoryId == null,
                        onClick = { viewModel.setSelectedCategoryId(null) },
                        label = { Text(s.all) }
                    )
                }
                items(uiState.categories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategoryId == category.id,
                        onClick = { viewModel.setSelectedCategoryId(category.id) },
                        label = { Text(category.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = s.removeCategory,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        scope.launch {
                                            viewModel.deleteCategory(category)
                                        }
                                    }
                            )
                        }
                    )
                }
            }

            if (uiState.isSearching) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(s.searchDocumentsPlaceholder) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true
                )
            }

            if (uiState.documents.isEmpty()) {
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
                            text = when {
                                uiState.isSearching -> s.emptySearchResult
                                uiState.selectedCategoryId != null -> s.emptyCategory
                                else -> s.emptyKnowledge
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (!uiState.isSearching && uiState.selectedCategoryId == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = s.emptyKnowledgeHint,
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
                    items(uiState.documents) { doc ->
                        KnowledgeDocumentItem(
                            document = doc,
                            category = uiState.categories.find { it.id == doc.categoryId },
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteDocument(doc)
                                }
                            },
                            onClick = {}
                        )
                    }
                }
            }
        }
    }

    if (uiState.showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { viewModel.showAddCategoryDialog(false) },
            onConfirm = { name, type ->
                viewModel.addCategory(name, type)
                viewModel.showAddCategoryDialog(false)
            }
        )
    }
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val s = appStrings()
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("rag") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.newCategory) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.categoryName) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(s.categoryType, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = type == "rag",
                        onClick = { type = "rag" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(s.ragMode, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            s.ragModeDesc,
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
                        Text(s.embeddingMode, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            s.embeddingModeDesc,
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
                Text(s.create)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel)
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
    val s = appStrings()
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
                    text = java.lang.String.format(s.chunkCount, document.chunkCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = s.delete,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(s.deleteDocument) },
            text = { Text(java.lang.String.format(s.deleteDocumentMessage, document.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(s.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(s.cancel)
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
