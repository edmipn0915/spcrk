package com.spcrk.app.ui.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spcrk.app.ai.ChatViewModel
import com.spcrk.app.ai.SkillDisplayItem
import com.spcrk.app.ai.AgentStep
import com.spcrk.app.ai.AgentStepType
import com.spcrk.app.ai.AgentStepStatus
import com.spcrk.app.ai.Message
import com.spcrk.app.ai.McpToolCallInfo
import com.spcrk.app.ai.ConversationSummary
import com.spcrk.app.ui.theme.LocalSuccessColor
import com.spcrk.app.ui.theme.TechTextField
import com.spcrk.app.ui.theme.glass
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showModelSelector by remember { mutableStateOf(false) }
    var showSkillSelector by remember { mutableStateOf(false) }
    var showSearchCitations by remember { mutableStateOf<Long?>(null) }
    var showDeleteConversationDialog by remember { mutableStateOf<String?>(null) }
    var showDrawer by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.attachDocument(it.toString()) }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.attachImage(it.toString()) }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { showDrawer = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "对话历史",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.currentModel.ifEmpty { "AI 聊天" },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Box {
                                IconButton(onClick = { showModelSelector = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "选择模型",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = showModelSelector,
                                    onDismissRequest = { showModelSelector = false }
                                ) {
                                    val options = viewModel.getAvailableModels()
                                    if (options.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("未配置模型，请到设置中添加", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                            onClick = { showModelSelector = false }
                                        )
                                    } else {
                                        options.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option.label, color = MaterialTheme.colorScheme.onSurface) },
                                                onClick = {
                                                    viewModel.selectModel(option.id)
                                                    showModelSelector = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isStreaming = uiState.isSending && message.id == uiState.messages.lastOrNull()?.id,
                            onToggleSearchCitations = { messageId ->
                                showSearchCitations = if (showSearchCitations == messageId) null else messageId
                            },
                            isSearchCitationsExpanded = showSearchCitations == message.id
                        )
                    }
                }

                AnimatedVisibility(
                    visible = uiState.enableSearch || uiState.enableMcp || uiState.enableKnowledge || uiState.attachedDocument != null || uiState.attachedImageUri != null,
                    enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(180)),
                    exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut(tween(150))
                ) {
                    AttachmentStatusBar(
                        enableSearch = uiState.enableSearch,
                        enableMcp = uiState.enableMcp,
                        enableKnowledge = uiState.enableKnowledge,
                        attachedDocument = uiState.attachedDocument,
                        attachedImageUri = uiState.attachedImageUri,
                        onClearAttachments = { viewModel.clearAttachments() }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleSearch() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.enableSearch) Icons.Outlined.Search else Icons.Outlined.SearchOff,
                                    contentDescription = "搜索增强",
                                    tint = if (uiState.enableSearch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { documentPicker.launch("*/*") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = "上传文档",
                                    tint = if (uiState.attachedDocument != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { imagePicker.launch("image/*") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = "上传图片",
                                    tint = if (uiState.attachedImageUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { showSkillSelector = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Extension,
                                    contentDescription = "Skill",
                                    tint = if (uiState.activeSkill != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleMcp() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Hub,
                                    contentDescription = "MCP",
                                    tint = if (uiState.enableMcp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleKnowledge() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MenuBook,
                                    contentDescription = "知识库",
                                    tint = if (uiState.enableKnowledge) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TechTextField(
                                value = uiState.currentInput,
                                onValueChange = { viewModel.updateInput(it) },
                                modifier = Modifier.weight(1f),
                                placeholder = "输入消息...",
                                enabled = !uiState.isSending,
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            FloatingActionButton(
                                onClick = {
                                    if (uiState.isSending) {
                                        viewModel.cancelSending()
                                    } else if (uiState.currentInput.isNotBlank()) {
                                        viewModel.sendMessage()
                                        keyboardController?.hide()
                                    }
                                },
                                modifier = Modifier.size(48.dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = if (uiState.isSending) Icons.Outlined.Close else Icons.Default.Send,
                                    contentDescription = if (uiState.isSending) "取消" else "发送",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showSkillSelector) {
            ModalBottomSheet(
                onDismissRequest = { showSkillSelector = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                SkillSelectorPanel(
                    skillDisplayItems = viewModel.skillDisplayItems,
                    onSkillSelected = { trigger ->
                        showSkillSelector = false
                        viewModel.updateInput("/$trigger ")
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showDrawer,
            enter = slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(tween(200)),
            exit = slideOutHorizontally(animationSpec = tween(260, easing = FastOutSlowInEasing)) + fadeOut(tween(180))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .glass(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "返回",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "返回",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.startNewConversation()
                                showDrawer = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("新建对话")
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            "对话历史",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            items(uiState.conversations) { conv ->
                                ConversationHistoryItem(
                                    summary = conv,
                                    isSelected = conv.id == uiState.currentConversationId,
                                    onClick = {
                                        viewModel.switchConversation(conv.id)
                                        showDrawer = false
                                    },
                                    onDelete = { showDeleteConversationDialog = conv.id }
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { showDrawer = false }
                )
            }
        }

        showDeleteConversationDialog?.let { convId ->
            AlertDialog(
                onDismissRequest = { showDeleteConversationDialog = null },
                title = { Text("删除对话") },
                text = { Text("确定要删除这条对话吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteConversation(convId)
                        showDeleteConversationDialog = null
                    }) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConversationDialog = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun ConversationHistoryItem(
    summary: ConversationSummary,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = formatTimestamp(summary.lastTimestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
private fun AttachmentStatusBar(
    enableSearch: Boolean,
    enableMcp: Boolean,
    enableKnowledge: Boolean,
    attachedDocument: String?,
    attachedImageUri: String?,
    onClearAttachments: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (enableSearch) {
            SuggestionChip(
                onClick = { },
                label = { Text("搜索", fontSize = 11.sp) },
                icon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    labelColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (enableMcp) {
            SuggestionChip(
                onClick = { },
                label = { Text("MCP", fontSize = 11.sp) },
                icon = {
                    Icon(
                        Icons.Outlined.Hub,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    labelColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (enableKnowledge) {
            SuggestionChip(
                onClick = { },
                label = { Text("知识库", fontSize = 11.sp) },
                icon = {
                    Icon(
                        Icons.Outlined.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    labelColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (attachedDocument != null) {
            SuggestionChip(
                onClick = { },
                label = { Text("文档已附加", fontSize = 11.sp) },
                icon = {
                    Icon(
                        Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    labelColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (attachedImageUri != null) {
            SuggestionChip(
                onClick = { },
                label = { Text("图片已附加", fontSize = 11.sp) },
                icon = {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    labelColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onClearAttachments,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = "清除附件",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SkillSelectorPanel(
    skillDisplayItems: kotlinx.coroutines.flow.Flow<List<SkillDisplayItem>>,
    onSkillSelected: (String) -> Unit
) {
    val skills by skillDisplayItems.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "选择 Skill",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (skills.isEmpty()) {
            Text(
                text = "暂无已安装的 Skill",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            skills.forEach { skill ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSkillSelected(skill.trigger) },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Extension,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = skill.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (skill.description.isNotEmpty()) {
                                Text(
                                    text = skill.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "/${skill.trigger}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isStreaming: Boolean = false,
    onToggleSearchCitations: (Long) -> Unit = {},
    isSearchCitationsExpanded: Boolean = false
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                shape = RoundedCornerShape(
                    topStart = if (isUser) 16.dp else 4.dp,
                    topEnd = if (isUser) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (!isUser && message.isAgentMode && message.agentSteps.isNotEmpty()) {
                        AgentStepsPanel(steps = message.agentSteps)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (message.content.isEmpty() && isStreaming) {
                        StreamingCursor()
                    } else {
                        val displayText = if (isStreaming && message.content.isEmpty()) {
                            ""
                        } else {
                            message.content
                        }
                        if (displayText.isNotEmpty()) {
                            if (isUser) {
                                Text(
                                    text = displayText,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                FormattedText(
                                    text = displayText,
                                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (!isUser && message.mcpToolCalls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        message.mcpToolCalls.forEach { toolCall ->
                            McpToolCallIndicator(toolCall = toolCall)
                        }
                    }

                    if (!isUser && message.searchResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clickable { onToggleSearchCitations(message.id) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${message.searchResults.size} 个搜索来源",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "我",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isSearchCitationsExpanded && message.searchResults.isNotEmpty(),
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(180)),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut(tween(150))
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(start = if (isUser) 0.dp else 44.dp, top = 4.dp)
            ) {
                message.searchResults.forEachIndexed { index, result ->
                    SearchCitationItem(
                        index = index + 1,
                        title = result.title,
                        url = result.url,
                        snippet = result.snippet
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamingCursor() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Box(
        modifier = Modifier
            .size(width = 8.dp, height = 16.dp)
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
private fun FormattedText(text: String, textColor: Color) {
    val codeBlockPattern = Regex("```(\\w+)?\\n([\\s\\S]*?)```")
    var lastIndex = 0

    Column {
        codeBlockPattern.findAll(text).forEach { match ->
            val prefix = text.substring(lastIndex, match.range.first)
            if (prefix.isNotBlank()) {
                RenderMarkdownBlock(prefix, textColor)
                Spacer(modifier = Modifier.height(4.dp))
            }

            val language = match.groupValues[1]
            val code = match.groupValues[2].trim()

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (language.isNotEmpty()) {
                        Text(
                            text = language,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = code,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            lastIndex = match.range.last + 1
        }

        val remaining = text.substring(lastIndex)
        if (remaining.isNotBlank()) {
            RenderMarkdownBlock(remaining.trim(), textColor)
        }
    }
}

@Composable
private fun RenderMarkdownBlock(text: String, textColor: Color) {
    val lines = text.lines()
    var paragraph = StringBuilder()

    @Composable
    fun flushParagraph() {
        if (paragraph.isNotBlank()) {
            RenderInlineMarkdown(paragraph.toString().trim(), textColor, style = MaterialTheme.typography.bodyMedium)
            paragraph = StringBuilder()
        }
    }

    Column {
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.isBlank() -> {
                    flushParagraph()
                    Spacer(modifier = Modifier.height(4.dp))
                }
                line.startsWith("### ") -> {
                    flushParagraph()
                    RenderInlineMarkdown(
                        line.removePrefix("### "),
                        textColor,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                line.startsWith("## ") -> {
                    flushParagraph()
                    RenderInlineMarkdown(
                        line.removePrefix("## "),
                        textColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                line.startsWith("# ") -> {
                    flushParagraph()
                    RenderInlineMarkdown(
                        line.removePrefix("# "),
                        textColor,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") || line.matches(Regex("\\d+\\. .*")) -> {
                    flushParagraph()
                    Row(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = "•  ",
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        RenderInlineMarkdown(
                            line.replace(Regex("^[\\d]+\\. "), "").removePrefix("- ").removePrefix("* "),
                            textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    if (paragraph.isNotEmpty()) paragraph.append(' ')
                    paragraph.append(line)
                }
            }
        }
        flushParagraph()
    }
}

@Composable
private fun RenderInlineMarkdown(
    text: String,
    textColor: Color,
    style: TextStyle,
    fontWeight: FontWeight? = null
) {
    val boldPattern = Regex("\\*\\*(.+?)\\*\\*")
    val inlineCodePattern = Regex("`([^`]+)`")
    var lastIndex = 0
    var boldApplied = false

    Column {
        boldPattern.findAll(text).forEach { match ->
            val prefix = text.substring(lastIndex, match.range.first)
            if (prefix.isNotEmpty()) {
                Text(
                    text = prefix.replace(inlineCodePattern, "$1"),
                    color = textColor,
                    style = style,
                    fontWeight = fontWeight
                )
            }
            Text(
                text = match.groupValues[1].replace(inlineCodePattern, "$1"),
                color = textColor,
                style = style,
                fontWeight = FontWeight.Bold
            )
            boldApplied = true
            lastIndex = match.range.last + 1
        }
        val remaining = text.substring(lastIndex)
        if (remaining.isNotEmpty()) {
            Text(
                text = remaining.replace(inlineCodePattern, "$1"),
                color = textColor,
                style = style,
                fontWeight = if (boldApplied) null else fontWeight
            )
        }
    }
}

@Composable
private fun McpToolCallIndicator(toolCall: McpToolCallInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Hub,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = toolCall.toolName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                when (toolCall.status) {
                    "calling" -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 1.5.dp
                        )
                    }
                    "completed" -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "完成",
                            tint = LocalSuccessColor.current,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    "error" -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "错误",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            if (toolCall.result != null && toolCall.status != "calling") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = toolCall.result,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
private fun AgentStepsPanel(steps: List<AgentStep>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        steps.forEach { step ->
            AgentStepItem(step = step)
        }
    }
}

@Composable
private fun AgentStepItem(step: AgentStep) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (step.status) {
                AgentStepStatus.RUNNING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                AgentStepStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                AgentStepStatus.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (step.type) {
                        AgentStepType.THINK -> Icons.Outlined.Psychology
                        AgentStepType.TOOL_CALL -> Icons.Outlined.Build
                        AgentStepType.SKILL -> Icons.Outlined.Extension
                        AgentStepType.SEARCH -> Icons.Outlined.Search
                    },
                    contentDescription = null,
                    tint = when (step.status) {
                        AgentStepStatus.ERROR -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                when (step.status) {
                    AgentStepStatus.RUNNING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp
                        )
                    }
                    AgentStepStatus.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "完成",
                            tint = LocalSuccessColor.current,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    AgentStepStatus.ERROR -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "错误",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded && (step.content.isNotEmpty() || step.details != null),
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(180)),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    if (step.content.isNotEmpty()) {
                        Text(
                            text = step.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (step.details != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = step.details,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp),
                                maxLines = 8,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCitationItem(
    index: Int,
    title: String,
    url: String,
    snippet: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}
