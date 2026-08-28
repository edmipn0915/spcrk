package com.spcrk.app.ai

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spcrk.app.data.Repository
import com.spcrk.app.data.ChatMessage
import com.spcrk.app.data.Skill
import com.spcrk.app.data.UsageRecord
import com.spcrk.app.data.model.ModelConfig
import com.spcrk.app.getAppContainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONObject

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

data class SkillDisplayItem(
    val trigger: String,
    val name: String,
    val description: String
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val container = getAppContainer(application)
    private val repository = container.repository
    private val modelStore = container.modelConfigStore
    private val settingsStore = container.settingsStore
    private val aiManager = AIManager()
    private val streamProcessor = ChatStreamProcessor(aiManager)
    private val searchEngine = SearchEngine()
    private val mcpService = container.mcpService
    private val conversationBuilder = ConversationBuilder(
        getUiState = { _uiState.value },
        repository = repository,
        documentService = container.documentService,
        ocrService = container.ocrService,
        appProvider = { getApplication() }
    )
    private val toolCallHandler = ToolCallHandler(
        getUiState = { _uiState.value },
        setUiState = { _uiState.value = it },
        mcpService = mcpService,
        searchEngine = searchEngine,
        streamProcessor = streamProcessor,
        searchConfig = { currentSearchConfig() },
        buildToolDefs = { buildToolDefinitions() }
    )
    private val skillService = container.skillService
    private val documentService = container.documentService
    private val ocrService = container.ocrService

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var sendJob: Job? = null
    private var messageIdCounter = 0L

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
            handleSlashCommand(input)
            return
        }

        val userMessage = Message(id = ++messageIdCounter, role = "user", content = input)
        val isAgentMode = _uiState.value.enableMcp || _uiState.value.enableSearch || _uiState.value.enableKnowledge
        val loadingMessage = createLoadingMessage(isAgentMode)

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage + loadingMessage,
            isSending = true,
            currentInput = "",
            errorMessage = null
        )

        persistUserMessage(input)

        sendJob = viewModelScope.launch {
            try {
                runAiRequest(userMessage) { showConfigError() }
            } catch (e: Exception) {
                handleSendError(e)
            }
        }
    }

    private fun handleSlashCommand(input: String) {
        val parts = input.substring(1).split(" ", limit = 2)
        val trigger = parts[0]
        val args = if (parts.size > 1) parts[1] else ""
        triggerSkill(trigger, args)
        _uiState.value = _uiState.value.copy(currentInput = "")
    }

    private fun createLoadingMessage(isAgentMode: Boolean): Message {
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
        return Message(
            id = ++messageIdCounter,
            role = "assistant",
            content = "",
            isAgentMode = isAgentMode,
            agentSteps = initialSteps
        )
    }

    private fun persistUserMessage(input: String) {
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
            } catch (e: Exception) {
                println("[ChatViewModel] save user message failed: ${e.message}")
            }
        }
    }

    private suspend fun runAiRequest(
        lastUserMessage: Message,
        onConfigMissing: () -> Unit
    ) {
        val config = currentConfig()
        if (config == null) {
            onConfigMissing()
            return
        }

        val searchResults = performSearchIfEnabled(lastUserMessage.content)
        val conversationMessages = conversationBuilder.buildConversationMessages(lastUserMessage, searchResults)
        val tools = buildToolDefinitions()
        val aiMessageIndex = _uiState.value.messages.lastIndex
        val fullResponse = mutableListOf<String>()
        val pendingToolCalls = mutableListOf<Triple<String, String, String>>()

        collectChatStream(
            config,
            conversationMessages,
            tools,
            fullResponse,
            pendingToolCalls,
            aiMessageIndex
        )

        finalizeAiResponse(aiMessageIndex, fullResponse, pendingToolCalls, conversationMessages, config)
    }

    private suspend fun performSearchIfEnabled(query: String): List<SearchResult> {
        if (!_uiState.value.enableSearch) return emptyList()
        val results = searchEngine.search(query, currentSearchConfig())
        _uiState.value = _uiState.value.copy(searchResults = results)
        return results
    }

    private suspend fun finalizeAiResponse(
        aiMessageIndex: Int,
        fullResponse: MutableList<String>,
        pendingToolCalls: MutableList<Triple<String, String, String>>,
        conversationMessages: List<Map<String, Any>>,
        config: ModelConfig
    ) {
        val finalParsed = streamProcessor.parseAgentResponse(fullResponse.joinToString(""))
        updateThinkStep(aiMessageIndex, finalParsed)

        if (pendingToolCalls.isNotEmpty()) {
            toolCallHandler.processStandardToolCalls(pendingToolCalls, aiMessageIndex, conversationMessages, config)
        } else if (_uiState.value.enableMcp && fullResponse.joinToString("").contains("[MCP_TOOL_CALL:")) {
            toolCallHandler.processMcpToolCalls(fullResponse.joinToString(""), aiMessageIndex, conversationMessages, config)
        } else {
            saveAiResponseToConversation(fullResponse.joinToString(""))
            _uiState.value = _uiState.value.copy(isSending = false)
        }
    }

    private fun updateThinkStep(aiMessageIndex: Int, parsed: ParsedAgentResponse) {
        val finalSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
        val thinkIdx = finalSteps.indexOfFirst { it.type == AgentStepType.THINK }
        if (thinkIdx < 0) return
        val existingThink = finalSteps[thinkIdx]
        val thinkContent = if (parsed.thinkContent.isNotEmpty()) parsed.thinkContent else existingThink.content
        finalSteps[thinkIdx] = existingThink.copy(
            title = if (thinkContent.isNotEmpty()) "思考完毕" else "思考完成",
            content = thinkContent,
            status = AgentStepStatus.COMPLETED
        )
        val msgs = _uiState.value.messages.toMutableList()
        if (aiMessageIndex >= 0 && aiMessageIndex < msgs.size) {
            msgs[aiMessageIndex] = msgs[aiMessageIndex].copy(
                content = parsed.normalContent,
                agentSteps = finalSteps.toList()
            )
            _uiState.value = _uiState.value.copy(messages = msgs)
        }
    }

    private fun showConfigError() {
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
    }

    private fun handleSendError(e: Exception) {
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
            } catch (e: Exception) {
                println("[ChatViewModel] save AI response failed: ${e.message}")
            }
        }
    }

    private suspend fun collectChatStream(
        config: ModelConfig,
        conversationMessages: List<Map<String, Any>>,
        tools: List<Map<String, Any>>,
        fullResponseRef: MutableList<String>,
        pendingToolCallsRef: MutableList<Triple<String, String, String>>,
        aiMessageIndex: Int
    ) {
        streamProcessor.processStream(
            config = config,
            conversationMessages = conversationMessages,
            tools = tools.ifEmpty { null },
            enableSearch = _uiState.value.enableSearch,
            enableMcp = _uiState.value.enableMcp,
            onText = { normalContent, _ ->
                fullResponseRef += normalContent
                updateStreamingText(aiMessageIndex, normalContent)
            },
            onToolCall = { id, name, args ->
                pendingToolCallsRef.add(Triple(id, name, args))
            },
            onReasoning = { content ->
                updateStreamingReasoning(aiMessageIndex, content)
            }
        )
    }

    private fun updateStreamingText(aiMessageIndex: Int, normalContent: String) {
        val currentSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
        val thinkIndex = currentSteps.indexOfFirst { it.type == AgentStepType.THINK }
        if (thinkIndex >= 0) {
            val existingThink = currentSteps[thinkIndex]
            currentSteps[thinkIndex] = existingThink.copy(
                title = "思考中...",
                content = existingThink.content,
                status = AgentStepStatus.RUNNING
            )
        }
        val messages = _uiState.value.messages.toMutableList()
        if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
            messages[aiMessageIndex] = messages[aiMessageIndex].copy(
                content = normalContent,
                agentSteps = currentSteps
            )
        }
        _uiState.value = _uiState.value.copy(messages = messages)
    }

    private fun updateStreamingReasoning(aiMessageIndex: Int, content: String) {
        val currentSteps = _uiState.value.messages[aiMessageIndex].agentSteps.toMutableList()
        val thinkIndex = currentSteps.indexOfFirst { it.type == AgentStepType.THINK }
        if (thinkIndex >= 0) {
            val existing = currentSteps[thinkIndex]
            currentSteps[thinkIndex] = existing.copy(
                content = existing.content + content,
                status = AgentStepStatus.RUNNING,
                title = "思考中..."
            )
        } else {
            currentSteps.add(
                AgentStep(
                    id = "think-${System.currentTimeMillis()}",
                    type = AgentStepType.THINK,
                    title = "思考中...",
                    content = content,
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

    fun regenerateLastResponse() {
        val messages = _uiState.value.messages
        if (messages.size < 2) return

        val lastUserMessage = messages.dropLast(1).lastOrNull { it.role == "user" } ?: return
        val updatedMessages = messages.filterIndexed { index, _ -> index < messages.size - 2 }

        val isAgentMode = _uiState.value.enableMcp || _uiState.value.enableSearch || _uiState.value.enableKnowledge
        val loadingMessage = createLoadingMessage(isAgentMode)

        _uiState.value = _uiState.value.copy(
            messages = updatedMessages + loadingMessage,
            isSending = true,
            errorMessage = null
        )

        sendJob = viewModelScope.launch {
            try {
                runAiRequest(lastUserMessage) {
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
            val result = skillService.triggerSkill(trigger, input)
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
        return modelStore.loadConfigs().filter { it.isEnabled }.map {
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
            ?: modelStore.loadConfigs().filter { it.isEnabled }.firstOrNull()
    }

    private fun settingsDefaultConfig(all: List<ModelConfig>): ModelConfig? {
        val id = settingsStore.defaultModelIdFlow.value
        if (id.isBlank()) return null
        return all.find { it.id == id }?.takeIf { it.isEnabled }
    }

    val skillDisplayItems = skillService.getAllSkills().map { skillList ->
        skillList.map { skill ->
            SkillDisplayItem(skill.trigger, skill.name, skill.description)
        }
    }

    fun startNewConversation() {
        val newId = java.util.UUID.randomUUID().toString()
        val all = modelStore.loadConfigs()
        val def = settingsDefaultConfig(all)
            ?: modelStore.getDefault()?.takeIf { it.isEnabled }
            ?: modelStore.loadConfigs().filter { it.isEnabled }.firstOrNull()
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
            } catch (e: Exception) {
                println("[ChatViewModel] load conversation messages failed: ${e.message}")
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
            } catch (e: Exception) {
                println("[ChatViewModel] delete conversation failed: ${e.message}")
            }
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
                        } catch (e: Exception) {
                            println("[ChatViewModel] build conversation summary failed: ${e.message}")
                            null
                        }
                    }.sortedByDescending { it.lastTimestamp }
                    _uiState.value = _uiState.value.copy(conversations = summaries)
                }
            } catch (e: Exception) {
                println("[ChatViewModel] load conversations failed: ${e.message}")
            }
        }
    }

    private fun currentSearchConfig(): SearchConfig {
        val store = settingsStore
        return SearchConfig(
            engine = store.searchEngineFlow.value,
            apiKey = store.searchApiKeyFlow.value,
            baseUrl = store.searchBaseUrlFlow.value,
            maxResults = store.searchMaxResultsFlow.value
        )
    }

    private fun buildToolDefinitions(): List<Map<String, Any>> {
        val tools = mutableListOf<Map<String, Any>>()
        if (_uiState.value.enableSearch) tools.add(webSearchToolDefinition())
        if (_uiState.value.enableKnowledge) tools.add(knowledgeBaseToolDefinition())
        if (_uiState.value.enableMcp) tools.addAll(mcpToolDefinitions())
        return tools
    }

    private fun webSearchToolDefinition(): Map<String, Any> =
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

    private fun knowledgeBaseToolDefinition(): Map<String, Any> =
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

    private fun mcpToolDefinitions(): List<Map<String, Any>> =
        mcpService.getAllTools().flatMap { (serverId, serverTools) ->
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
        }

    private fun calculateCost(promptTokens: Int, completionTokens: Int): Double {
        return (promptTokens * 0.00001) + (completionTokens * 0.00003)
    }
}
