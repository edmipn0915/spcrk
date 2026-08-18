package com.spcrk.app.ai

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spcrk.app.data.ModelConfigStore
import com.spcrk.app.data.Repository
import com.spcrk.app.data.SettingsStore
import com.spcrk.app.data.ChatMessage
import com.spcrk.app.data.UsageRecord
import com.spcrk.app.VideoDownloaderApp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Base64

enum class AgentStepType { THINK, TOOL_CALL, SKILL, SEARCH }

enum class AgentStepStatus { RUNNING, COMPLETED, ERROR }

data class AgentStep(
    val id: String,
    val type: AgentStepType,
    val title: String,
    val content: String = "",
    val status: AgentStepStatus = AgentStepStatus.RUNNING,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class Message(
    val id: Long = 0,
    val role: String,
    val content: String,
    val searchResults: List<SearchResult> = emptyList(),
    val mcpToolCalls: List<McpToolCallInfo> = emptyList(),
    val agentSteps: List<AgentStep> = emptyList(),
    val isAgentMode: Boolean = false
)

data class McpToolCallInfo(
    val toolName: String,
    val status: String,
    val result: String? = null
)

data class ConversationSummary(
    val id: String,
    val title: String,
    val lastTimestamp: Long,
    val messageCount: Int
)

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isSending: Boolean = false,
    val currentInput: String = "",
    val currentModel: String = "",
    val currentProvider: String = "",
    val currentConfigId: String? = null,
    val currentConversationId: String = "",
    val conversations: List<ConversationSummary> = emptyList(),
    val errorMessage: String? = null,
    val enableSearch: Boolean = false,
    val enableMcp: Boolean = false,
    val enableKnowledge: Boolean = false,
    val searchResults: List<SearchResult> = emptyList(),
    val activeSkill: String? = null,
    val attachedDocument: String? = null,
    val attachedImageUri: String? = null
)

data class ModelOption(
    val id: String,
    val label: String
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as VideoDownloaderApp).repository
    private val modelStore = ModelConfigStore(application)
    private val aiManager = AIManager()
    private val searchEngine = SearchEngine()
    private val mcpManager = McpManager()
    private val skillManager = SkillManager(application)
    private val documentManager = DocumentManager(application)
    private val ocrManager = OcrManager(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var sendJob: Job? = null
    private var messageIdCounter = 0L

    private val visionModels = setOf(
        "gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-4-vision", "gpt-4v",
        "claude-3", "claude-3-5", "claude-3-7", "claude-sonnet", "claude-opus",
        "gemini-1.5", "gemini-2", "gemini-pro-vision", "gemini-3", "gemini-3.1", "gemini-3.5", "gemini-3.6",
        "agnes-2.5-flash", "agnes-2.0-flash",
        "doubao-seed-1.6-vision", "qwen-vl", "qwen2-vl",
        "llava", "molmo", "internvl", "phi-3-vision"
    )

    init {
        val config = currentConfig()
        if (config != null) {
            _uiState.value = _uiState.value.copy(
                currentModel = config.modelName,
                currentProvider = providerDisplayName(config.provider),
                currentConfigId = config.id
            )
        }
        startNewConversation()
        loadConversations()
        aiManager.onUsageRecord = { promptTokens, completionTokens ->
            viewModelScope.launch {
                repository.addUsageRecord(
                    UsageRecord(
                        model = _uiState.value.currentModel.ifEmpty { "unknown" },
                        provider = _uiState.value.currentProvider.ifEmpty { "unknown" },
                        promptTokens = promptTokens,
                        completionTokens = completionTokens,
                        totalTokens = promptTokens + completionTokens,
                        cost = calculateCost(promptTokens, completionTokens)
                    )
                )
            }
        }
    }

    fun updateInput(text: String) {
        _uiState.value = _uiState.value.copy(currentInput = text)
    }

    fun sendMessage() {
        val input = _uiState.value.currentInput.trim()
        if (input.isEmpty() || _uiState.value.isSending) return

        if (input.startsWith("/")) {
            val parts = input.substring(1).split(" ", limit = 2)
            val trigger = parts[0]
            val args = if (parts.size > 1) parts[1] else ""
            triggerSkill(trigger, args)
            _uiState.value = _uiState.value.copy(currentInput = "")
            return
        }

        val userMessage = Message(id = ++messageIdCounter, role = "user", content = input)
        val isAgentMode = _uiState.value.enableMcp || _uiState.value.enableSearch || _uiState.value.enableKnowledge
        val initialSteps = if (isAgentMode) {
            listOf(
                AgentStep(
                    id = "think-${System.currentTimeMillis()}",
                    type = AgentStepType.THINK,
                    title = "思考中...",
                    content = "",
                    status = AgentStepStatus.RUNNING
                )
            )
        } else emptyList()
        val loadingMessage = Message(
            id = ++messageIdCounter,
            role = "assistant",
            content = "",
            isAgentMode = isAgentMode,
            agentSteps = initialSteps
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage + loadingMessage,
            isSending = true,
            currentInput = "",
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                repository.addMessage(
                    ChatMessage(
                        role = "user",
                        content = input,
                        model = _uiState.value.currentModel,
                        timestamp = System.currentTimeMillis(),
                        conversationId = _uiState.value.currentConversationId
                    )
                )
                loadConversations()
            } catch (_: Exception) { }
        }

        sendJob = viewModelScope.launch {
            try {
                val config = currentConfig()
                if (config == null) {
                    val updatedMessages = _uiState.value.messages.toMutableList()
                    val lastIndex = updatedMessages.lastIndex
                    if (lastIndex >= 0) {
                        updatedMessages[lastIndex] = updatedMessages[lastIndex].copy(
                            content = "错误: 请先配置AI模型"
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        messages = updatedMessages,
                        isSending = false,
                        errorMessage = "请先配置AI模型"
                    )
                    return@launch
                }

                val searchResults = if (_uiState.value.enableSearch) {
                    val results = searchEngine.search(input, currentSearchConfig())
                    _uiState.value = _uiState.value.copy(searchResults = results)
                    results
                } else {
                    emptyList()
                }

                val conversationMessages = buildConversationMessages(userMessage, searchResults)
                val tools = buildToolDefinitions()

                val aiMessageIndex = _uiState.value.messages.lastIndex
                var fullResponse = ""
                val pendingToolCalls = mutableListOf<Triple<String, String, String>>()

                aiManager.chatStream(
                    config,
                    conversationMessages,
                    _uiState.value.enableSearch,
                    _uiState.value.enableMcp,
                    tools.ifEmpty { null }
                ).collect { event ->
                    when (event) {
                        is StreamEvent.Text -> {
                            fullResponse += event.content
                            val parsed = parseAgentResponse(fullResponse)
                            val currentSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
                            val thinkIndex = currentSteps.indexOfFirst { it.type == AgentStepType.THINK }
                            if (thinkIndex >= 0) {
                                val existingThink = currentSteps[thinkIndex]
                                val thinkContent = if (parsed.thinkContent.isNotEmpty()) parsed.thinkContent else existingThink.content
                                currentSteps[thinkIndex] = existingThink.copy(
                                    title = if (thinkContent.isNotEmpty()) "思考中..." else "思考中...",
                                    content = thinkContent,
                                    status = AgentStepStatus.RUNNING
                                )
                            }
                            val messages = _uiState.value.messages.toMutableList()
                            if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
                                messages[aiMessageIndex] = messages[aiMessageIndex].copy(
                                    content = parsed.normalContent,
                                    searchResults = searchResults,
                                    agentSteps = currentSteps
                                )
                            }
                            _uiState.value = _uiState.value.copy(messages = messages)
                        }
                        is StreamEvent.ToolCall -> {
                            pendingToolCalls.add(Triple(event.id, event.name, event.arguments))
                        }
                        is StreamEvent.Reasoning -> {
                            val currentSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
                            val thinkIndex = currentSteps.indexOfFirst { it.type == AgentStepType.THINK }
                            if (thinkIndex >= 0) {
                                val existing = currentSteps[thinkIndex]
                                currentSteps[thinkIndex] = existing.copy(
                                    content = existing.content + event.content,
                                    status = AgentStepStatus.RUNNING,
                                    title = "思考中..."
                                )
                            } else {
                                currentSteps.add(
                                    AgentStep(
                                        id = "think-${System.currentTimeMillis()}",
                                        type = AgentStepType.THINK,
                                        title = "思考中...",
                                        content = event.content,
                                        status = AgentStepStatus.RUNNING
                                    )
                                )
                            }
                            val messages = _uiState.value.messages.toMutableList()
                            if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
                                messages[aiMessageIndex] = messages[aiMessageIndex].copy(agentSteps = currentSteps)
                            }
                            _uiState.value = _uiState.value.copy(messages = messages)
                        }
                        is StreamEvent.Done -> {}
                    }
                }

                val finalParsed = parseAgentResponse(fullResponse)
                val finalSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
                val thinkIdx = finalSteps.indexOfFirst { it.type == AgentStepType.THINK }
                if (thinkIdx >= 0) {
                    val existingThink = finalSteps[thinkIdx]
                    val thinkContent = if (finalParsed.thinkContent.isNotEmpty()) finalParsed.thinkContent else existingThink.content
                    finalSteps[thinkIdx] = existingThink.copy(
                        title = if (thinkContent.isNotEmpty()) "思考完毕" else "思考完成",
                        content = thinkContent,
                        status = AgentStepStatus.COMPLETED
                    )
                    val messages = _uiState.value.messages.toMutableList()
                    if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
                        messages[aiMessageIndex] = messages[aiMessageIndex].copy(
                            content = finalParsed.normalContent,
                            agentSteps = finalSteps.toList()
                        )
                        _uiState.value = _uiState.value.copy(messages = messages)
                    }
                }

                if (pendingToolCalls.isNotEmpty()) {
                    processStandardToolCalls(pendingToolCalls, aiMessageIndex, conversationMessages, config)
                } else if (_uiState.value.enableMcp && fullResponse.contains("[MCP_TOOL_CALL:")) {
                    processMcpToolCalls(fullResponse, aiMessageIndex, conversationMessages, config)
                } else {
                    saveAiResponseToConversation(fullResponse)
                    _uiState.value = _uiState.value.copy(isSending = false)
                }
            } catch (e: Exception) {
                val messages = _uiState.value.messages.toMutableList()
                val lastIndex = messages.lastIndex
                if (lastIndex >= 0) {
                    messages[lastIndex] = messages[lastIndex].copy(
                        content = "错误: ${e.message}"
                    )
                }
                _uiState.value = _uiState.value.copy(
                    messages = messages,
                    isSending = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private fun saveAiResponseToConversation(response: String) {
        if (response.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addMessage(
                    ChatMessage(
                        role = "assistant",
                        content = response,
                        model = _uiState.value.currentModel,
                        timestamp = System.currentTimeMillis(),
                        conversationId = _uiState.value.currentConversationId
                    )
                )
                loadConversations()
            } catch (_: Exception) { }
        }
    }

    fun regenerateLastResponse() {
        val messages = _uiState.value.messages
        if (messages.size < 2) return

        val lastUserMessage = messages.dropLast(1).lastOrNull { it.role == "user" } ?: return
        val updatedMessages = messages.filterIndexed { index, _ -> index < messages.size - 2 }

        val isAgentMode = _uiState.value.enableMcp || _uiState.value.enableSearch || _uiState.value.enableKnowledge
        val initialSteps = if (isAgentMode) {
            listOf(
                AgentStep(
                    id = "think-${System.currentTimeMillis()}",
                    type = AgentStepType.THINK,
                    title = "思考中...",
                    content = "",
                    status = AgentStepStatus.RUNNING
                )
            )
        } else emptyList()
        val loadingMessage = Message(
            id = ++messageIdCounter,
            role = "assistant",
            content = "",
            isAgentMode = isAgentMode,
            agentSteps = initialSteps
        )
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages + loadingMessage,
            isSending = true,
            errorMessage = null
        )

        sendJob = viewModelScope.launch {
            try {
                val config = currentConfig()
                if (config == null) {
                    _uiState.value = _uiState.value.copy(isSending = false)
                    return@launch
                }

                val searchResults = if (_uiState.value.enableSearch) {
                    searchEngine.search(lastUserMessage.content, currentSearchConfig())
                } else {
                    emptyList()
                }

                val conversationMessages = buildConversationMessages(lastUserMessage, searchResults)
                val tools = buildToolDefinitions()
                val aiMessageIndex = _uiState.value.messages.lastIndex
                var fullResponse = ""
                val pendingToolCalls = mutableListOf<Triple<String, String, String>>()

                aiManager.chatStream(
                    config,
                    conversationMessages,
                    _uiState.value.enableSearch,
                    _uiState.value.enableMcp,
                    tools.ifEmpty { null }
                ).collect { event ->
                    when (event) {
                        is StreamEvent.Text -> {
                            fullResponse += event.content
                            val parsed = parseAgentResponse(fullResponse)
                            val currentSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
                            val thinkIndex = currentSteps.indexOfFirst { it.type == AgentStepType.THINK }
                            if (thinkIndex >= 0) {
                                val existingThink = currentSteps[thinkIndex]
                                val thinkContent = if (parsed.thinkContent.isNotEmpty()) parsed.thinkContent else existingThink.content
                                currentSteps[thinkIndex] = existingThink.copy(
                                    title = if (thinkContent.isNotEmpty()) "思考中..." else "思考中...",
                                    content = thinkContent,
                                    status = AgentStepStatus.RUNNING
                                )
                            }
                            val msgs = _uiState.value.messages.toMutableList()
                            if (aiMessageIndex >= 0 && aiMessageIndex < msgs.size) {
                                msgs[aiMessageIndex] = msgs[aiMessageIndex].copy(
                                    content = parsed.normalContent,
                                    searchResults = searchResults,
                                    agentSteps = currentSteps
                                )
                            }
                            _uiState.value = _uiState.value.copy(messages = msgs)
                        }
                        is StreamEvent.ToolCall -> {
                            pendingToolCalls.add(Triple(event.id, event.name, event.arguments))
                        }
                        is StreamEvent.Reasoning -> {
                            val currentSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
                            val thinkIndex = currentSteps.indexOfFirst { it.type == AgentStepType.THINK }
                            if (thinkIndex >= 0) {
                                val existing = currentSteps[thinkIndex]
                                currentSteps[thinkIndex] = existing.copy(
                                    content = existing.content + event.content,
                                    status = AgentStepStatus.RUNNING,
                                    title = "思考中..."
                                )
                            } else {
                                currentSteps.add(
                                    AgentStep(
                                        id = "think-${System.currentTimeMillis()}",
                                        type = AgentStepType.THINK,
                                        title = "思考中...",
                                        content = event.content,
                                        status = AgentStepStatus.RUNNING
                                    )
                                )
                            }
                            val msgs = _uiState.value.messages.toMutableList()
                            if (aiMessageIndex >= 0 && aiMessageIndex < msgs.size) {
                                msgs[aiMessageIndex] = msgs[aiMessageIndex].copy(agentSteps = currentSteps)
                            }
                            _uiState.value = _uiState.value.copy(messages = msgs)
                        }
                        is StreamEvent.Done -> {}
                    }
                }

                val finalParsed = parseAgentResponse(fullResponse)
                val finalSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
                val thinkIdx = finalSteps.indexOfFirst { it.type == AgentStepType.THINK }
                if (thinkIdx >= 0) {
                    val existingThink = finalSteps[thinkIdx]
                    val thinkContent = if (finalParsed.thinkContent.isNotEmpty()) finalParsed.thinkContent else existingThink.content
                    finalSteps[thinkIdx] = existingThink.copy(
                        title = if (thinkContent.isNotEmpty()) "思考完毕" else "思考完成",
                        content = thinkContent,
                        status = AgentStepStatus.COMPLETED
                    )
                    val msgs = _uiState.value.messages.toMutableList()
                    if (aiMessageIndex >= 0 && aiMessageIndex < msgs.size) {
                        msgs[aiMessageIndex] = msgs[aiMessageIndex].copy(
                            content = finalParsed.normalContent,
                            agentSteps = finalSteps.toList()
                        )
                        _uiState.value = _uiState.value.copy(messages = msgs)
                    }
                }

                if (pendingToolCalls.isNotEmpty()) {
                    processStandardToolCalls(pendingToolCalls, aiMessageIndex, conversationMessages, config)
                } else if (_uiState.value.enableMcp && fullResponse.contains("[MCP_TOOL_CALL:")) {
                    processMcpToolCalls(fullResponse, aiMessageIndex, conversationMessages, config)
                } else {
                    saveAiResponseToConversation(fullResponse)
                    _uiState.value = _uiState.value.copy(isSending = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSending = false)
            }
        }
    }

    fun deleteMessage(messageId: Long) {
        val filtered = _uiState.value.messages.filter { it.id != messageId }
        _uiState.value = _uiState.value.copy(messages = filtered)
    }

    fun toggleSearch() {
        _uiState.value = _uiState.value.copy(enableSearch = !_uiState.value.enableSearch)
    }

    fun toggleMcp() {
        _uiState.value = _uiState.value.copy(enableMcp = !_uiState.value.enableMcp)
    }

    fun toggleKnowledge() {
        _uiState.value = _uiState.value.copy(enableKnowledge = !_uiState.value.enableKnowledge)
    }

    fun selectModel(configId: String) {
        val config = modelStore.loadConfigs().find { it.id == configId && it.isEnabled }
        if (config != null) {
            _uiState.value = _uiState.value.copy(
                currentModel = config.modelName,
                currentProvider = providerDisplayName(config.provider),
                currentConfigId = config.id
            )
        }
    }

    fun cancelSending() {
        sendJob?.cancel()
        _uiState.value = _uiState.value.copy(isSending = false)
    }

    fun attachDocument(uri: String) {
        _uiState.value = _uiState.value.copy(attachedDocument = uri)
    }

    fun attachImage(uri: String) {
        _uiState.value = _uiState.value.copy(attachedImageUri = uri)
    }

    fun clearAttachments() {
        _uiState.value = _uiState.value.copy(
            attachedDocument = null,
            attachedImageUri = null
        )
    }

    fun triggerSkill(trigger: String, input: String) {
        val userMessage = Message(id = ++messageIdCounter, role = "user", content = "/$trigger $input")
        val loadingMessage = Message(id = ++messageIdCounter, role = "assistant", content = "")

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage + loadingMessage,
            isSending = true,
            errorMessage = null,
            activeSkill = trigger,
            currentInput = ""
        )

        sendJob = viewModelScope.launch {
            val result = skillManager.triggerSkill(trigger, input)
            val messages = _uiState.value.messages.toMutableList()
            val lastIndex = messages.lastIndex
            if (lastIndex >= 0) {
                messages[lastIndex] = messages[lastIndex].copy(content = result)
            }
            _uiState.value = _uiState.value.copy(
                messages = messages,
                isSending = false,
                activeSkill = null
            )
        }
    }

    fun getAvailableModels(): List<ModelOption> {
        return modelStore.getEnabledConfigs().map {
            ModelOption(
                id = it.id,
                label = "${it.modelName} (${providerDisplayName(it.provider)})"
            )
        }
    }

    private fun currentConfig(): ModelConfig? {
        val all = modelStore.loadConfigs()
        val selected = _uiState.value.currentConfigId?.let { id -> all.find { it.id == id } }
        return selected?.takeIf { it.isEnabled }
            ?: settingsDefaultConfig(all)
            ?: modelStore.getDefault()?.takeIf { it.isEnabled }
            ?: modelStore.getEnabledConfigs().firstOrNull()
    }

    private fun settingsDefaultConfig(all: List<ModelConfig>): ModelConfig? {
        val id = SettingsStore.getInstance().defaultModelIdFlow.value
        if (id.isBlank()) return null
        return all.find { it.id == id }?.takeIf { it.isEnabled }
    }

    fun getMcpManager(): McpManager = mcpManager

    fun getSkillManager(): SkillManager = skillManager

    fun startNewConversation() {
        val newId = java.util.UUID.randomUUID().toString()
        val all = modelStore.loadConfigs()
        val def = settingsDefaultConfig(all)
            ?: modelStore.getDefault()?.takeIf { it.isEnabled }
            ?: modelStore.getEnabledConfigs().firstOrNull()
        _uiState.value = _uiState.value.copy(
            currentConversationId = newId,
            messages = emptyList(),
            errorMessage = null,
            currentConfigId = def?.id,
            currentModel = def?.modelName ?: "",
            currentProvider = def?.let { providerDisplayName(it.provider) } ?: ""
        )
    }

    fun switchConversation(convId: String) {
        if (convId == _uiState.value.currentConversationId) return
        _uiState.value = _uiState.value.copy(
            currentConversationId = convId,
            isSending = false,
            errorMessage = null
        )
        viewModelScope.launch {
            try {
                val history = repository.getConversation(convId).firstOrNull() ?: emptyList()
                val msgs = history.map { dbMsg ->
                    Message(
                        id = dbMsg.id,
                        role = dbMsg.role,
                        content = dbMsg.content
                    )
                }
                _uiState.value = _uiState.value.copy(messages = msgs)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(messages = emptyList())
            }
        }
    }

    fun deleteConversation(convId: String) {
        viewModelScope.launch {
            try {
                repository.deleteConversationMessages(convId)
                if (convId == _uiState.value.currentConversationId) {
                    startNewConversation()
                }
                loadConversations()
            } catch (_: Exception) { }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            try {
                repository.getConversations().collect { ids ->
                    val summaries = ids.mapNotNull { id ->
                        try {
                            val msgs = repository.getConversation(id).firstOrNull() ?: emptyList()
                            if (msgs.isEmpty()) return@mapNotNull null
                            val firstUserMsg = msgs.firstOrNull { it.role == "user" }
                            val title = firstUserMsg?.content?.take(20) ?: "对话"
                            ConversationSummary(
                                id = id,
                                title = title,
                                lastTimestamp = msgs.lastOrNull()?.timestamp ?: 0L,
                                messageCount = msgs.size
                            )
                        } catch (_: Exception) { null }
                    }.sortedByDescending { it.lastTimestamp }
                    _uiState.value = _uiState.value.copy(conversations = summaries)
                }
            } catch (_: Exception) { }
        }
    }

    private fun currentSearchConfig(): SearchConfig {
        val store = SettingsStore.getInstance()
        return SearchConfig(
            engine = store.searchEngineFlow.value,
            apiKey = store.searchApiKeyFlow.value,
            baseUrl = store.searchBaseUrlFlow.value,
            maxResults = store.searchMaxResultsFlow.value
        )
    }

    private fun modelSupportsVision(modelName: String): Boolean {
        val lower = modelName.lowercase()
        return visionModels.any { lower.contains(it) }
    }

    private fun buildToolDefinitions(): List<Map<String, Any>> {
        val tools = mutableListOf<Map<String, Any>>()

        if (_uiState.value.enableSearch) {
            tools.add(
                mapOf(
                    "type" to "function",
                    "function" to mapOf(
                        "name" to "web_search",
                        "description" to "搜索互联网获取最新信息、新闻、事实等",
                        "parameters" to mapOf(
                            "type" to "object",
                            "properties" to mapOf(
                                "query" to mapOf(
                                    "type" to "string",
                                    "description" to "搜索关键词或问题"
                                )
                            ),
                            "required" to listOf("query")
                        )
                    )
                )
            )
        }

        if (_uiState.value.enableKnowledge) {
            tools.add(
                mapOf(
                    "type" to "function",
                    "function" to mapOf(
                        "name" to "query_knowledge_base",
                        "description" to "查询用户知识库中的文档和资料",
                        "parameters" to mapOf(
                            "type" to "object",
                            "properties" to mapOf(
                                "query" to mapOf(
                                    "type" to "string",
                                    "description" to "知识库查询关键词"
                                )
                            ),
                            "required" to listOf("query")
                        )
                    )
                )
            )
        }

        if (_uiState.value.enableMcp) {
            val mcpTools = mcpManager.getAllTools()
            mcpTools.flatMap { (serverId, serverTools) ->
                serverTools.map { tool ->
                    mapOf(
                        "type" to "function",
                        "function" to mapOf(
                            "name" to "${serverId}:${tool.name}",
                            "description" to tool.description,
                            "parameters" to (tool.inputSchema ?: JSONObject())
                        )
                    )
                }
            }.also { tools.addAll(it) }
        }

        return tools
    }

    private suspend fun buildConversationMessages(
        currentMessage: Message,
        searchResults: List<SearchResult>
    ): List<Map<String, Any>> {
        val messages = mutableListOf<Map<String, Any>>()

        try {
            val history = repository.getConversation(_uiState.value.currentConversationId).firstOrNull() ?: emptyList()
            history.forEach { dbMsg ->
                messages.add(mapOf("role" to dbMsg.role, "content" to dbMsg.content))
            }
        } catch (_: Exception) { }

        if (searchResults.isNotEmpty()) {
            val contextBuilder = StringBuilder()
            contextBuilder.appendLine("以下是与用户问题相关的网络搜索结果：")
            searchResults.forEachIndexed { index, result ->
                contextBuilder.appendLine("${index + 1}. ${result.title}")
                contextBuilder.appendLine("   URL: ${result.url}")
                contextBuilder.appendLine("   摘要: ${result.snippet}")
            }
            contextBuilder.appendLine("\n请基于以上搜索结果回答用户的问题。")
            messages.add(mapOf("role" to "system", "content" to contextBuilder.toString()))
        }

        if (_uiState.value.enableKnowledge) {
            val knowledgeContext = queryKnowledgeBase(currentMessage.content)
            if (knowledgeContext != null) {
                messages.add(mapOf("role" to "system", "content" to knowledgeContext))
            }
        }

        val attachedDoc = _uiState.value.attachedDocument
        if (attachedDoc != null) {
            try {
                val uri = Uri.parse(attachedDoc)
                val docInfo = documentManager.loadDocument(uri)
                val docContext = StringBuilder()
                docContext.appendLine("用户附加了文档：${docInfo.name}")
                docContext.appendLine("文档内容：")
                docContext.appendLine(docInfo.content)
                messages.add(mapOf("role" to "system", "content" to docContext.toString()))
            } catch (_: Exception) { }
        }

        val attachedImg = _uiState.value.attachedImageUri
        if (attachedImg != null) {
            try {
                val uri = Uri.parse(attachedImg)
                if (modelSupportsVision(_uiState.value.currentModel)) {
                    val base64 = imageUriToBase64(uri)
                    if (base64.isNotEmpty()) {
                        messages.add(
                            mapOf(
                                "role" to "user",
                                "content" to listOf(
                                    mapOf("type" to "text", "text" to "用户附加了图片"),
                                    mapOf(
                                        "type" to "image_url",
                                        "image_url" to mapOf("url" to "data:image/jpeg;base64,$base64")
                                    )
                                )
                            )
                        )
                    }
                } else {
                    val ocrText = ocrManager.recognizeText(uri)
                    if (ocrText.isNotEmpty()) {
                        val imgContext = StringBuilder()
                        imgContext.appendLine("用户附加了图片，OCR 识别结果：")
                        imgContext.appendLine(ocrText)
                        messages.add(mapOf("role" to "system", "content" to imgContext.toString()))
                    }
                }
            } catch (_: Exception) { }
        }

        messages.add(mapOf("role" to "user", "content" to currentMessage.content))

        return messages
    }

    private suspend fun queryKnowledgeBase(query: String): String? {
        return try {
            val docs = repository.searchKnowledgeDocuments(query).firstOrNull() ?: return null
            if (docs.isEmpty()) return null
            val sb = StringBuilder()
            sb.appendLine("以下是知识库中相关的参考资料：")
            docs.take(5).forEach { doc ->
                sb.appendLine("【${doc.title}】")
                sb.appendLine(doc.content.take(500))
                sb.appendLine()
            }
            sb.toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun imageUriToBase64(uri: Uri): String {
        return try {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap == null) return ""
            val resized = if (bitmap.width > 1024 || bitmap.height > 1024) {
                val ratio = minOf(1024f / bitmap.width, 1024f / bitmap.height)
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
            } else bitmap
            val baos = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val bytes = baos.toByteArray()
            if (resized != bitmap) resized.recycle()
            bitmap.recycle()
            Base64.getEncoder().encodeToString(bytes)
        } catch (_: Exception) {
            ""
        }
    }

    private suspend fun processStandardToolCalls(
        toolCalls: List<Triple<String, String, String>>,
        aiMessageIndex: Int,
        originalMessages: List<Map<String, Any>>,
        config: ModelConfig
    ) {
        val toolCallInfos = mutableListOf<McpToolCallInfo>()
        val agentSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
        val updatedMessages = originalMessages.toMutableList()
        var toolSearchResults = emptyList<SearchResult>()

        for ((callId, fullName, argumentsStr) in toolCalls) {
            val colonIdx = fullName.indexOf(':')
            val serverId = if (colonIdx > 0) fullName.substring(0, colonIdx) else ""
            val toolName = if (colonIdx > 0) fullName.substring(colonIdx + 1) else fullName

            val toolCallInfo = McpToolCallInfo(toolName = toolName, status = "calling")
            toolCallInfos.add(toolCallInfo)

            val stepId = "tool-${System.currentTimeMillis()}-${toolCallInfos.size}"
            val agentStep = AgentStep(
                id = stepId,
                type = AgentStepType.TOOL_CALL,
                title = "Tool call · $toolName",
                content = argumentsStr,
                status = AgentStepStatus.RUNNING
            )
            agentSteps.add(agentStep)
            updateMessageWithAgentSteps(aiMessageIndex, agentSteps, toolCallInfos)

            try {
                val arguments = JSONObject(argumentsStr)
                val result = when (toolName) {
                    "web_search" -> {
                        val query = arguments.optString("query", "")
                        if (query.isNotEmpty()) {
                            val results = searchEngine.search(query, currentSearchConfig())
                            if (results.isNotEmpty()) {
                                toolSearchResults = results
                                results.joinToString("\n") { "${it.title}: ${it.snippet}" }
                            } else {
                                "未找到相关搜索结果"
                            }
                        } else {
                            "搜索关键词为空"
                        }
                    }
                    "query_knowledge_base" -> {
                        val query = arguments.optString("query", "")
                        if (query.isNotEmpty()) {
                            val knowledgeResult = queryKnowledgeBase(query)
                            knowledgeResult ?: "知识库中未找到相关内容"
                        } else {
                            "查询关键词为空"
                        }
                    }
                    else -> {
                        mcpManager.callTool(serverId, toolName, arguments)
                    }
                }

                val completedInfo = toolCallInfo.copy(status = "completed", result = result)
                toolCallInfos[toolCallInfos.indexOf(toolCallInfo)] = completedInfo

                val stepIndex = agentSteps.indexOfFirst { it.id == stepId }
                if (stepIndex >= 0) {
                    agentSteps[stepIndex] = agentStep.copy(
                        title = "Tool call · $toolName · completed",
                        status = AgentStepStatus.COMPLETED,
                        details = result
                    )
                }

                updatedMessages.add(
                    mapOf(
                        "role" to "assistant",
                        "content" to "",
                        "tool_calls" to listOf(
                            mapOf(
                                "id" to callId,
                                "type" to "function",
                                "function" to mapOf(
                                    "name" to fullName,
                                    "arguments" to argumentsStr
                                )
                            )
                        )
                    )
                )
                updatedMessages.add(
                    mapOf(
                        "role" to "tool",
                        "tool_call_id" to callId,
                        "name" to fullName,
                        "content" to "工具 $toolName 返回结果：\n$result"
                    )
                )
            } catch (e: Exception) {
                val errorInfo = toolCallInfo.copy(status = "error", result = "调用失败: ${e.message}")
                toolCallInfos[toolCallInfos.indexOf(toolCallInfo)] = errorInfo

                val stepIndex = agentSteps.indexOfFirst { it.id == stepId }
                if (stepIndex >= 0) {
                    agentSteps[stepIndex] = agentStep.copy(
                        title = "Tool call · $toolName · error",
                        status = AgentStepStatus.ERROR,
                        details = "调用失败: ${e.message}"
                    )
                }
            }
            updateMessageWithAgentSteps(aiMessageIndex, agentSteps, toolCallInfos)
        }

        updateMessageWithAgentSteps(aiMessageIndex, agentSteps, toolCallInfos)

        if (toolSearchResults.isNotEmpty()) {
            val messages = _uiState.value.messages.toMutableList()
            if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
                messages[aiMessageIndex] = messages[aiMessageIndex].copy(
                    searchResults = toolSearchResults
                )
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }

        var continueResponse = ""
        aiManager.chatStream(config, updatedMessages, _uiState.value.enableSearch, _uiState.value.enableMcp, buildToolDefinitions().ifEmpty { null }).collect { event ->
            when (event) {
                is StreamEvent.Text -> {
                    continueResponse += event.content
                    val currentMessages = _uiState.value.messages.toMutableList()
                    if (aiMessageIndex >= 0 && aiMessageIndex < currentMessages.size) {
                        currentMessages[aiMessageIndex] = currentMessages[aiMessageIndex].copy(
                            content = continueResponse,
                            searchResults = toolSearchResults,
                            mcpToolCalls = toolCallInfos.toList(),
                            agentSteps = agentSteps.toList()
                        )
                        _uiState.value = _uiState.value.copy(messages = currentMessages)
                    }
                }
                is StreamEvent.ToolCall -> {}
                is StreamEvent.Reasoning -> {}
                is StreamEvent.Done -> {}
            }
        }

        _uiState.value = _uiState.value.copy(isSending = false)
    }

    private fun updateMessageWithAgentSteps(
        aiMessageIndex: Int,
        agentSteps: List<AgentStep>,
        toolCallInfos: List<McpToolCallInfo>
    ) {
        val messages = _uiState.value.messages.toMutableList()
        if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
            messages[aiMessageIndex] = messages[aiMessageIndex].copy(
                mcpToolCalls = toolCallInfos,
                agentSteps = agentSteps
            )
            _uiState.value = _uiState.value.copy(messages = messages)
        }
    }

    private suspend fun processMcpToolCalls(
        responseText: String,
        aiMessageIndex: Int,
        originalMessages: List<Map<String, Any>>,
        config: ModelConfig
    ) {
        val toolCallPattern = Regex("\\[MCP_TOOL_CALL:([^:]+):([^:]+):(.+?)\\]")
        val matches = toolCallPattern.findAll(responseText).toList()

        if (matches.isEmpty()) {
            _uiState.value = _uiState.value.copy(isSending = false)
            return
        }

        val toolCallInfos = mutableListOf<McpToolCallInfo>()
        val agentSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
        val updatedMessages = originalMessages.toMutableList()

        for (match in matches) {
            val serverId = match.groupValues[1]
            val toolName = match.groupValues[2]
            val argumentsStr = match.groupValues[3]

            val toolCallInfo = McpToolCallInfo(toolName = toolName, status = "calling")
            toolCallInfos.add(toolCallInfo)

            val stepId = "mcp-tool-${System.currentTimeMillis()}-${toolCallInfos.size}"
            val agentStep = AgentStep(
                id = stepId,
                type = AgentStepType.TOOL_CALL,
                title = "Tool call · $toolName",
                content = argumentsStr,
                status = AgentStepStatus.RUNNING
            )
            agentSteps.add(agentStep)
            updateMessageWithAgentSteps(aiMessageIndex, agentSteps, toolCallInfos)

            try {
                val arguments = JSONObject(argumentsStr)
                val result = mcpManager.callTool(serverId, toolName, arguments)

                val completedInfo = toolCallInfo.copy(status = "completed", result = result)
                toolCallInfos[toolCallInfos.indexOf(toolCallInfo)] = completedInfo

                val stepIndex = agentSteps.indexOfFirst { it.id == stepId }
                if (stepIndex >= 0) {
                    agentSteps[stepIndex] = agentStep.copy(
                        title = "Tool call · $toolName · completed",
                        status = AgentStepStatus.COMPLETED,
                        details = result
                    )
                }

                updatedMessages.add(mapOf("role" to "tool", "content" to "工具 $toolName 返回结果：\n$result"))
            } catch (e: Exception) {
                val errorInfo = toolCallInfo.copy(status = "error", result = "调用失败: ${e.message}")
                toolCallInfos[toolCallInfos.indexOf(toolCallInfo)] = errorInfo

                val stepIndex = agentSteps.indexOfFirst { it.id == stepId }
                if (stepIndex >= 0) {
                    agentSteps[stepIndex] = agentStep.copy(
                        title = "Tool call · $toolName · error",
                        status = AgentStepStatus.ERROR,
                        details = "调用失败: ${e.message}"
                    )
                }
            }
            updateMessageWithAgentSteps(aiMessageIndex, agentSteps, toolCallInfos)
        }

        val cleanedResponse = responseText.replace(toolCallPattern, "").trim()
        updateMessageWithAgentSteps(aiMessageIndex, agentSteps, toolCallInfos)
        val messages = _uiState.value.messages.toMutableList()
        if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
            messages[aiMessageIndex] = messages[aiMessageIndex].copy(
                content = cleanedResponse,
                mcpToolCalls = toolCallInfos.toList(),
                agentSteps = agentSteps.toList()
            )
            _uiState.value = _uiState.value.copy(messages = messages)
        }

        val continueBuilder = StringBuilder()
        aiManager.chatStream(config, updatedMessages, _uiState.value.enableSearch, _uiState.value.enableMcp).collect { event ->
            when (event) {
                is StreamEvent.Text -> {
                    continueBuilder.append(event.content)
                    val currentMessages = _uiState.value.messages.toMutableList()
                    if (aiMessageIndex >= 0 && aiMessageIndex < currentMessages.size) {
                        currentMessages[aiMessageIndex] = currentMessages[aiMessageIndex].copy(
                            content = cleanedResponse + "\n" + continueBuilder.toString(),
                            searchResults = emptyList(),
                            mcpToolCalls = toolCallInfos.toList(),
                            agentSteps = agentSteps.toList()
                        )
                        _uiState.value = _uiState.value.copy(messages = currentMessages)
                    }
                }
                is StreamEvent.ToolCall -> {}
                is StreamEvent.Reasoning -> {}
                is StreamEvent.Done -> {}
            }
        }

        _uiState.value = _uiState.value.copy(isSending = false)
    }

    private fun calculateCost(promptTokens: Int, completionTokens: Int): Double {
        return (promptTokens * 0.00001) + (completionTokens * 0.00003)
    }

    private data class ParsedAgentResponse(
        val thinkContent: String,
        val normalContent: String
    )

    private fun parseAgentResponse(response: String): ParsedAgentResponse {
        val thinkPattern = Regex("<think>([\\s\\S]*?)</think>")
        val thinkMatches = thinkPattern.findAll(response).toList()
        val thinkContent = if (thinkMatches.isNotEmpty()) {
            thinkMatches.joinToString("\n") { it.groupValues[1].trim() }
        } else ""
        val normalContent = response.replace(thinkPattern, "").trim()
        return ParsedAgentResponse(thinkContent, normalContent)
    }
}
