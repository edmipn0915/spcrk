package com.spcrk.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

sealed class StreamEvent {
    data class Text(val content: String) : StreamEvent()
    data class ToolCall(val id: String, val name: String, val arguments: String) : StreamEvent()
    data class Reasoning(val content: String) : StreamEvent()
    data class Done(val promptTokens: Int = 0, val completionTokens: Int = 0) : StreamEvent()
}

class AIManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    var onUsageRecord: ((promptTokens: Int, completionTokens: Int) -> Unit)? = null

    fun chatStream(
        config: ModelConfig,
        messages: List<Map<String, Any>>,
        enableSearch: Boolean = false,
        enableMcp: Boolean = false,
        tools: List<Map<String, Any>>? = null
    ): Flow<StreamEvent> = flow {
        val jsonMessages = JSONArray()
        messages.forEach { msg ->
            val obj = JSONObject()
            obj.put("role", msg["role"] ?: "")
            when (val content = msg["content"]) {
                is String -> obj.put("content", content)
                is List<*> -> {
                    val contentArray = JSONArray()
                    @Suppress("UNCHECKED_CAST")
                    (content as List<Map<String, Any>>).forEach { part ->
                        val partObj = JSONObject()
                        partObj.put("type", part["type"] ?: "")
                        when (part["type"]) {
                            "text" -> partObj.put("text", part["text"] ?: "")
                            "image_url" -> {
                                val imageUrl = JSONObject()
                                imageUrl.put("url", (part["image_url"] as? Map<*, *>)?.get("url") ?: "")
                                partObj.put("image_url", imageUrl)
                            }
                        }
                        contentArray.put(partObj)
                    }
                    obj.put("content", contentArray)
                }
                else -> obj.put("content", "")
            }
            msg["tool_calls"]?.let { tc ->
                @Suppress("UNCHECKED_CAST")
                val tcList = tc as List<Map<String, Any>>
                val tcArray = JSONArray()
                tcList.forEach { tcItem ->
                    val tcObj = JSONObject()
                    tcObj.put("id", tcItem["id"] ?: "")
                    tcObj.put("type", tcItem["type"] ?: "function")
                    val function = JSONObject()
                    function.put("name", (tcItem["function"] as? Map<*, *>)?.get("name") ?: "")
                    function.put("arguments", (tcItem["function"] as? Map<*, *>)?.get("arguments") ?: "")
                    tcObj.put("function", function)
                    tcArray.put(tcObj)
                }
                obj.put("tool_calls", tcArray)
            }
            msg["tool_call_id"]?.let { obj.put("tool_call_id", it) }
            msg["name"]?.let { obj.put("name", it) }
            jsonMessages.put(obj)
        }

        val body = JSONObject().apply {
            put("model", config.modelName)
            put("messages", jsonMessages)
            put("stream", true)
            put("temperature", config.temperature)
            put("max_tokens", config.maxTokens)
            if (!tools.isNullOrEmpty()) {
                val toolsArray = JSONArray()
                tools.forEach { tool ->
                    val toolObj = JSONObject()
                    toolObj.put("type", tool["type"] ?: "function")
                    val function = JSONObject()
                    function.put("name", (tool["function"] as? Map<*, *>)?.get("name") ?: "")
                    function.put("description", (tool["function"] as? Map<*, *>)?.get("description") ?: "")
                    val params = (tool["function"] as? Map<*, *>)?.get("parameters")
                    if (params != null) {
                        function.put("parameters", params)
                    }
                    toolObj.put("function", function)
                    toolsArray.put(toolObj)
                }
                put("tools", toolsArray)
                put("tool_choice", "auto")
            }
        }

        val requestBody = body.toString().toRequestBody("application/json".toMediaType())

        val requestBuilder = Request.Builder()
            .url("${config.baseUrl}/chat/completions")
            .post(requestBody)

        if (config.apiKey.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey}")
        }
        requestBuilder.addHeader("Content-Type", "application/json")

        val response = client.newCall(requestBuilder.build()).execute()
        if (!response.isSuccessful) {
            emit(StreamEvent.Text("[错误: HTTP ${response.code}]"))
            return@flow
        }

        var promptTokens = 0
        var completionTokens = 0

        val toolCallArgs = mutableMapOf<Int, StringBuilder>()
        val toolCallNames = mutableMapOf<Int, String>()
        val toolCallIds = mutableMapOf<Int, String>()

        val reader = BufferedReader(InputStreamReader(response.body?.byteStream()))
        reader.useLines { lines ->
            lines.forEach { line ->
                if (line.startsWith("data: ") && line != "data: [DONE]") {
                    val jsonData = line.removePrefix("data: ").trim()
                    if (jsonData.isNotEmpty()) {
                        try {
                            val json = JSONObject(jsonData)
                            val usage = json.optJSONObject("usage")
                            if (usage != null) {
                                promptTokens = usage.optInt("prompt_tokens", 0)
                                completionTokens = usage.optInt("completion_tokens", 0)
                            }
                            val choices = json.optJSONArray("choices")
                            if (choices != null && choices.length() > 0) {
                                val choice = choices.getJSONObject(0)
                                val delta = choice.optJSONObject("delta")
                                if (delta != null) {
                                    val content = delta.optString("content") ?: ""
                                    if (content.isNotEmpty()) {
                                        emit(StreamEvent.Text(content))
                                    }
                                    val reasoning = delta.optString("reasoning_content") ?: ""
                                    if (reasoning.isNotEmpty()) {
                                        emit(StreamEvent.Reasoning(reasoning))
                                    }
                                    val toolCalls = delta.optJSONArray("tool_calls")
                                    if (toolCalls != null) {
                                        for (i in 0 until toolCalls.length()) {
                                            val tc = toolCalls.getJSONObject(i)
                                            val index = tc.optInt("index", 0)
                                            tc.optString("id").takeIf { it.isNotEmpty() }?.let {
                                                toolCallIds[index] = it
                                            }
                                            val function = tc.optJSONObject("function")
                                            if (function != null) {
                                                function.optString("name").takeIf { it.isNotEmpty() }?.let {
                                                    toolCallNames[index] = it
                                                }
                                                val argsFragment = function.optString("arguments")
                                                if (argsFragment.isNotEmpty()) {
                                                    toolCallArgs.getOrPut(index) { StringBuilder() }.append(argsFragment)
                                                }
                                            }
                                        }
                                    }
                                }
                                val message = choice.optJSONObject("message")
                                if (message != null) {
                                    val mReasoning = message.optString("reasoning_content") ?: ""
                                    if (mReasoning.isNotEmpty()) {
                                        emit(StreamEvent.Reasoning(mReasoning))
                                    }
                                    val mContent = message.optString("content") ?: ""
                                    if (mContent.isNotEmpty()) {
                                        emit(StreamEvent.Text(mContent))
                                    }
                                    val mToolCalls = message.optJSONArray("tool_calls")
                                    if (mToolCalls != null) {
                                        for (i in 0 until mToolCalls.length()) {
                                            val tc = mToolCalls.getJSONObject(i)
                                            val id = tc.optString("id") ?: "call_$i"
                                            val function = tc.optJSONObject("function")
                                            if (function != null) {
                                                val name = function.optString("name") ?: ""
                                                val args = function.optString("arguments") ?: "{}"
                                                if (name.isNotEmpty()) {
                                                    emit(StreamEvent.ToolCall(id, name, args))
                                                }
                                            }
                                        }
                                    }
                                }
                                val finishReason = choice.optString("finish_reason")
                                if (finishReason == "tool_calls" || finishReason == "stop") {
                                    toolCallIds.forEach { (index, id) ->
                                        val name = toolCallNames[index] ?: return@forEach
                                        val args = toolCallArgs[index]?.toString() ?: "{}"
                                        emit(StreamEvent.ToolCall(id, name, args))
                                    }
                                    toolCallArgs.clear()
                                    toolCallNames.clear()
                                    toolCallIds.clear()
                                }
                            }
                            val topReasoning = json.optString("reasoning_content") ?: ""
                            if (topReasoning.isNotEmpty()) {
                                emit(StreamEvent.Reasoning(topReasoning))
                            }
                        } catch (_: Exception) { }
                    }
                }
            }
        }

        if (completionTokens > 0) {
            onUsageRecord?.invoke(promptTokens, completionTokens)
        }
        emit(StreamEvent.Done(promptTokens, completionTokens))
    }.flowOn(Dispatchers.IO)
}
