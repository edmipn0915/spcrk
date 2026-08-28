package com.spcrk.app.ai

import com.spcrk.app.ai.api.McpService
import com.spcrk.app.ai.SearchResult
import com.spcrk.app.data.model.ModelConfig
import org.json.JSONObject

class ToolCallHandler(
    private val getUiState: () -> ChatUiState,
    private val setUiState: (ChatUiState) -> Unit,
    private val mcpService: McpService,
    private val searchEngine: SearchEngine,
    private val streamProcessor: ChatStreamProcessor,
    private val searchConfig: () -> SearchConfig,
    private val buildToolDefs: () -> List<Map<String, Any>>
) {

    suspend fun processStandardToolCalls(
        toolCalls: List<Triple<String, String, String>>,
        aiMessageIndex: Int,
        originalMessages: List<Map<String, Any>>,
        config: ModelConfig
    ) {
        val toolCallInfos = mutableListOf<McpToolCallInfo>()
        val agentSteps = getUiState().messages[aiMessageIndex].agentSteps.toMutableList()
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
            updateMessage(aiMessageIndex, agentSteps, toolCallInfos)

            try {
                val arguments = JSONObject(argumentsStr)
                val result = when (toolName) {
                    "web_search" -> {
                        val query = arguments.optString("query", "")
                        if (query.isNotEmpty()) {
                            val results = searchEngine.search(query, searchConfig())
                            if (results.isNotEmpty()) {
                                toolSearchResults = results
                                results.joinToString("\n") { "${it.title}: ${it.snippet}" }
                            } else "未找到相关搜索结果"
                        } else "搜索关键词为空"
                    }
                    "query_knowledge_base" -> {
                        val query = arguments.optString("query", "")
                        "知识库功能暂未接入，请检查配置"
                    }
                    else -> mcpService.callTool(serverId, toolName, arguments)
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

                updatedMessages.add(mapOf("role" to "tool", "content" to result))
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
            updateMessage(aiMessageIndex, agentSteps, toolCallInfos)
        }

        updateMessage(aiMessageIndex, agentSteps, toolCallInfos)

        if (toolSearchResults.isNotEmpty()) {
            val messages = getUiState().messages.toMutableList()
            if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
                messages[aiMessageIndex] = messages[aiMessageIndex].copy(searchResults = toolSearchResults)
                setUiState(getUiState().copy(messages = messages))
            }
        }

        var continueResponse = ""
        streamProcessor.processStream(
            config = config,
            conversationMessages = updatedMessages,
            tools = buildToolDefs().ifEmpty { null },
            enableSearch = getUiState().enableSearch,
            enableMcp = getUiState().enableMcp,
            onText = { text, _ ->
                continueResponse += text
                val messages = getUiState().messages.toMutableList()
                if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
                    messages[aiMessageIndex] = messages[aiMessageIndex].copy(
                        content = continueResponse,
                        searchResults = toolSearchResults,
                        mcpToolCalls = toolCallInfos.toList(),
                        agentSteps = agentSteps.toList()
                    )
                    setUiState(getUiState().copy(messages = messages))
                }
            }
        )
        setUiState(getUiState().copy(isSending = false))
    }

    suspend fun processMcpToolCalls(
        responseText: String,
        aiMessageIndex: Int,
        originalMessages: List<Map<String, Any>>,
        config: ModelConfig
    ) {
        val toolCallPattern = Regex("\\[MCP_TOOL_CALL:([^:]+):([^:]+):(.+?)\\]")
        val matches = toolCallPattern.findAll(responseText).toList()

        if (matches.isEmpty()) {
            setUiState(getUiState().copy(isSending = false))
            return
        }

        val toolCallInfos = mutableListOf<McpToolCallInfo>()
        val agentSteps = getUiState().messages[aiMessageIndex].agentSteps.toMutableList()
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
            updateMessage(aiMessageIndex, agentSteps, toolCallInfos)

            try {
                val arguments = JSONObject(argumentsStr)
                val result = mcpService.callTool(serverId, toolName, arguments)

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
            updateMessage(aiMessageIndex, agentSteps, toolCallInfos)
        }

        val cleanedResponse = responseText.replace(toolCallPattern, "").trim()
        updateMessage(aiMessageIndex, agentSteps, toolCallInfos)
        val messages = getUiState().messages.toMutableList()
        if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
            messages[aiMessageIndex] = messages[aiMessageIndex].copy(
                content = cleanedResponse,
                mcpToolCalls = toolCallInfos.toList(),
                agentSteps = agentSteps.toList()
            )
            setUiState(getUiState().copy(messages = messages))
        }

        var continueResponse = ""
        streamProcessor.processStream(
            config = config,
            conversationMessages = updatedMessages,
            tools = null,
            enableSearch = getUiState().enableSearch,
            enableMcp = getUiState().enableMcp,
            onText = { text, _ ->
                continueResponse += text
                val currentMessages = getUiState().messages.toMutableList()
                if (aiMessageIndex >= 0 && aiMessageIndex < currentMessages.size) {
                    currentMessages[aiMessageIndex] = currentMessages[aiMessageIndex].copy(
                        content = cleanedResponse + "\n" + continueResponse,
                        searchResults = emptyList(),
                        mcpToolCalls = toolCallInfos.toList(),
                        agentSteps = agentSteps.toList()
                    )
                    setUiState(getUiState().copy(messages = currentMessages))
                }
            }
        )
        setUiState(getUiState().copy(isSending = false))
    }

    private fun updateMessage(
        aiMessageIndex: Int,
        agentSteps: List<AgentStep>,
        toolCallInfos: List<McpToolCallInfo>
    ) {
        val messages = getUiState().messages.toMutableList()
        if (aiMessageIndex >= 0 && aiMessageIndex < messages.size) {
            messages[aiMessageIndex] = messages[aiMessageIndex].copy(
                mcpToolCalls = toolCallInfos,
                agentSteps = agentSteps
            )
            setUiState(getUiState().copy(messages = messages))
        }
    }
}
