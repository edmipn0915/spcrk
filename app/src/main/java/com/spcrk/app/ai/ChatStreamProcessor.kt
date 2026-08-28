package com.spcrk.app.ai

import com.spcrk.app.data.model.ModelConfig
import kotlinx.coroutines.flow.collect

data class ParsedAgentResponse(
    val thinkContent: String,
    val normalContent: String
)

data class StreamResult(
    val text: String = "",
    val toolCalls: List<Triple<String, String, String>> = emptyList(),
    val reasoning: String = ""
)

class ChatStreamProcessor(private val aiManager: AIManager) {

    suspend fun processStream(
        config: ModelConfig,
        conversationMessages: List<Map<String, Any>>,
        tools: List<Map<String, Any>>?,
        enableSearch: Boolean,
        enableMcp: Boolean,
        onText: (String, String) -> Unit = { _, _ -> },
        onToolCall: (String, String, String) -> Unit = { _, _, _ -> },
        onReasoning: (String) -> Unit = {}
    ): StreamResult {
        var text = ""
        var toolCalls = emptyList<Triple<String, String, String>>()
        var reasoning = ""

        aiManager.chatStream(config, conversationMessages, enableSearch, enableMcp, tools).collect { event ->
            when (event) {
                is StreamEvent.Text -> {
                    text += event.content
                    onText(text, reasoning)
                }
                is StreamEvent.ToolCall -> {
                    toolCalls = toolCalls + Triple(event.id, event.name, event.arguments)
                    onToolCall(event.id, event.name, event.arguments)
                }
                is StreamEvent.Reasoning -> {
                    reasoning += event.content
                    onReasoning(reasoning)
                }
                is StreamEvent.Done -> {}
            }
        }

        return StreamResult(text = text, toolCalls = toolCalls, reasoning = reasoning)
    }

    fun parseAgentResponse(response: String): ParsedAgentResponse {
        val thinkPattern = Regex("<thinking>([\\s\\S]*?)</thinking>")
        val thinkMatches = thinkPattern.findAll(response).toList()
        val thinkContent = if (thinkMatches.isNotEmpty()) {
            thinkMatches.joinToString("\n") { it.groupValues[1].trim() }
        } else ""
        val normalContent = response.replace(thinkPattern, "").trim()
        return ParsedAgentResponse(thinkContent, normalContent)
    }
}
