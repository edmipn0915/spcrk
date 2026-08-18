package com.spcrk.app.ai

import com.spcrk.app.data.McpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: JSONObject?
)

data class McpToolInvocation(
    val serverId: String,
    val toolName: String,
    val arguments: JSONObject
)

class McpManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val servers = ConcurrentHashMap<String, McpServer>()
    private val tools = ConcurrentHashMap<String, List<McpTool>>()
    private val connectionStates = ConcurrentHashMap<String, ConnectionState>()

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }

    fun addServer(server: McpServer) {
        servers[server.id.toString()] = server
        if (server.isEnabled) {
            connectServer(server)
        }
    }

    fun removeServer(id: String) {
        servers.remove(id)
        tools.remove(id)
        connectionStates.remove(id)
    }

    fun updateServer(server: McpServer) {
        servers[server.id.toString()] = server
        if (server.isEnabled) {
            connectServer(server)
        } else {
            tools.remove(server.id.toString())
            connectionStates[server.id.toString()] = ConnectionState.DISCONNECTED
        }
    }

    fun getServers(): List<McpServer> = servers.values.toList()

    fun getServer(id: String): McpServer? = servers[id]

    fun getConnectionState(id: String): ConnectionState = connectionStates[id] ?: ConnectionState.DISCONNECTED

    fun connectServer(server: McpServer) {
        connectionStates[server.id.toString()] = ConnectionState.CONNECTING
        when (server.connectionType) {
            "sse" -> connectSse(server)
            "stdio" -> {
                connectionStates[server.id.toString()] = ConnectionState.CONNECTED
                loadTools(server)
            }
            "streamablehttp" -> connectStreamableHttp(server)
        }
    }

    private fun connectSse(server: McpServer) {
        try {
            val request = Request.Builder()
                .url(server.url)
                .addHeader("Accept", "text/event-stream")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    connectionStates[server.id.toString()] = ConnectionState.CONNECTED
                    parseToolsFromSseResponse(response, server.id.toString())
                } else {
                    connectionStates[server.id.toString()] = ConnectionState.ERROR
                    tools[server.id.toString()] = emptyList()
                }
            }
        } catch (e: Exception) {
            connectionStates[server.id.toString()] = ConnectionState.ERROR
            tools[server.id.toString()] = emptyList()
        }
    }

    private fun connectStreamableHttp(server: McpServer) {
        try {
            val jsonRpcRequest = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", System.currentTimeMillis().toString())
                put("method", "tools/list")
                put("params", JSONObject())
            }

            val requestBuilder = Request.Builder()
                .url(server.url)
                .post(jsonRpcRequest.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Accept", "application/json, text/event-stream")

            if (server.headers.isNotEmpty()) {
                try {
                    val headersJson = JSONObject(server.headers)
                    headersJson.keys().forEach { key ->
                        requestBuilder.addHeader(key, headersJson.getString(key))
                    }
                } catch (_: Exception) {
                }
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    connectionStates[server.id.toString()] = ConnectionState.CONNECTED
                    parseToolsFromResponse(response.body?.string() ?: "", server.id.toString())
                } else {
                    connectionStates[server.id.toString()] = ConnectionState.ERROR
                    tools[server.id.toString()] = emptyList()
                }
            }
        } catch (e: Exception) {
            connectionStates[server.id.toString()] = ConnectionState.ERROR
            tools[server.id.toString()] = emptyList()
        }
    }

    private fun parseToolsFromResponse(responseBody: String, serverId: String) {
        val toolList = mutableListOf<McpTool>()
        try {
            val json = JSONObject(responseBody)
            val toolsArray = json.optJSONObject("result")?.optJSONArray("tools")
                ?: json.optJSONArray("tools")
            toolsArray?.let {
                for (i in 0 until it.length()) {
                    val tool = it.getJSONObject(i)
                    toolList.add(
                        McpTool(
                            name = tool.optString("name", ""),
                            description = tool.optString("description", ""),
                            inputSchema = tool.optJSONObject("inputSchema")
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        tools[serverId] = toolList
    }

    private fun parseToolsFromSseResponse(response: okhttp3.Response, serverId: String) {
        val toolList = mutableListOf<McpTool>()
        try {
            val reader = response.body?.byteStream()?.bufferedReader()
            reader?.useLines { lines ->
                lines.forEach { line ->
                    if (line.startsWith("data: ")) {
                        val jsonData = line.removePrefix("data: ").trim()
                        if (jsonData.isNotEmpty() && jsonData != "[DONE]") {
                            try {
                                val json = JSONObject(jsonData)
                                val method = json.optString("method", "")
                                if (method == "tools/list" || json.has("tools")) {
                                    val toolsArray = json.optJSONArray("tools") ?: return@forEach
                                    for (i in 0 until toolsArray.length()) {
                                        val tool = toolsArray.getJSONObject(i)
                                        toolList.add(
                                            McpTool(
                                                name = tool.optString("name", ""),
                                                description = tool.optString("description", ""),
                                                inputSchema = tool.optJSONObject("inputSchema")
                                            )
                                        )
                                    }
                                }
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
        tools[serverId] = toolList
    }

    private fun loadTools(server: McpServer) {
        val toolList = mutableListOf<McpTool>()
        try {
            val jsonRpcRequest = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", System.currentTimeMillis().toString())
                put("method", "tools/list")
                put("params", JSONObject())
            }

            when (server.connectionType) {
                "sse" -> {
                    val body = jsonRpcRequest.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(server.url)
                        .post(body)
                        .addHeader("Accept", "application/json, text/event-stream")
                        .build()

                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string() ?: return
                        val json = JSONObject(responseBody)
                        val toolsArray = json.optJSONObject("result")?.optJSONArray("tools")
                            ?: json.optJSONArray("tools")
                        toolsArray?.let {
                            for (i in 0 until it.length()) {
                                val tool = it.getJSONObject(i)
                                toolList.add(
                                    McpTool(
                                        name = tool.optString("name", ""),
                                        description = tool.optString("description", ""),
                                        inputSchema = tool.optJSONObject("inputSchema")
                                    )
                                )
                            }
                        }
                    }
                }
                "stdio" -> {
                    val processBuilder = ProcessBuilder(server.command, *server.args.split(" ").toTypedArray())
                    val process = processBuilder.start()

                    val outputStream = process.outputStream
                    outputStream.write((jsonRpcRequest.toString() + "\n").toByteArray())
                    outputStream.flush()

                    val result = process.inputStream.bufferedReader().readText()
                    process.destroy()

                    val json = JSONObject(result)
                    val toolsArray = json.optJSONObject("result")?.optJSONArray("tools")
                        ?: json.optJSONArray("tools")
                    toolsArray?.let {
                        for (i in 0 until it.length()) {
                            val tool = it.getJSONObject(i)
                            toolList.add(
                                McpTool(
                                    name = tool.optString("name", ""),
                                    description = tool.optString("description", ""),
                                    inputSchema = tool.optJSONObject("inputSchema")
                                )
                            )
                        }
                    }
                }
                "streamablehttp" -> {
                    val requestBuilder = Request.Builder()
                        .url(server.url)
                        .post(jsonRpcRequest.toString().toRequestBody("application/json".toMediaType()))
                        .addHeader("Accept", "application/json, text/event-stream")

                    if (server.headers.isNotEmpty()) {
                        try {
                            val headersJson = JSONObject(server.headers)
                            headersJson.keys().forEach { key ->
                                requestBuilder.addHeader(key, headersJson.getString(key))
                            }
                        } catch (_: Exception) {
                        }
                    }

                    client.newCall(requestBuilder.build()).execute().use { response ->
                        val responseBody = response.body?.string() ?: return
                        val json = JSONObject(responseBody)
                        val toolsArray = json.optJSONObject("result")?.optJSONArray("tools")
                            ?: json.optJSONArray("tools")
                        toolsArray?.let {
                            for (i in 0 until it.length()) {
                                val tool = it.getJSONObject(i)
                                toolList.add(
                                    McpTool(
                                        name = tool.optString("name", ""),
                                        description = tool.optString("description", ""),
                                        inputSchema = tool.optJSONObject("inputSchema")
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
        tools[server.id.toString()] = toolList
    }

    fun getToolsForServer(serverId: String): List<McpTool> {
        return tools[serverId] ?: emptyList()
    }

    fun getAllTools(): Map<String, List<McpTool>> {
        return tools.toMap()
    }

    suspend fun callTool(serverId: String, toolName: String, arguments: JSONObject): String =
        withContext(Dispatchers.IO) {
            try {
                val server = servers[serverId] ?: return@withContext "服务器不存在"

                when (server.connectionType) {
                    "stdio" -> callToolStdio(server, toolName, arguments)
                    "sse" -> callToolSse(server, toolName, arguments)
                    "streamablehttp" -> callToolStreamableHttp(server, toolName, arguments)
                    else -> "不支持的连接类型"
                }
            } catch (e: Exception) {
                "工具调用失败: ${e.message}"
            }
        }

    private suspend fun callToolStdio(
        server: McpServer,
        toolName: String,
        arguments: JSONObject
    ): String = withContext(Dispatchers.IO) {
        try {
            val jsonRpcRequest = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", System.currentTimeMillis().toString())
                put("method", "tools/call")
                put(
                    "params", JSONObject().apply {
                        put("name", toolName)
                        put("arguments", arguments)
                    }
                )
            }

            val processBuilder = ProcessBuilder(server.command, *server.args.split(" ").toTypedArray())
            val process = processBuilder.start()

            val outputStream = process.outputStream
            outputStream.write((jsonRpcRequest.toString() + "\n").toByteArray())
            outputStream.flush()

            val result = process.inputStream.bufferedReader().readText()
            process.destroy()

            val json = JSONObject(result)
            val resultObj = json.optJSONObject("result")
            if (resultObj != null) {
                val content = resultObj.optJSONArray("content")
                if (content != null) {
                    val sb = StringBuilder()
                    for (i in 0 until content.length()) {
                        val item = content.getJSONObject(i)
                        if (item.optString("type") == "text") {
                            sb.append(item.optString("text", ""))
                        }
                    }
                    return@withContext sb.toString()
                }
                return@withContext resultObj.toString()
            }
            json.optJSONObject("error")?.let {
                return@withContext "错误: ${it.optString("message", "未知错误")}"
            }
            return@withContext result
        } catch (e: Exception) {
            "工具调用失败: ${e.message}"
        }
    }

    private suspend fun callToolSse(
        server: McpServer,
        toolName: String,
        arguments: JSONObject
    ): String = withContext(Dispatchers.IO) {
        try {
            val jsonRpcRequest = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", System.currentTimeMillis().toString())
                put("method", "tools/call")
                put(
                    "params", JSONObject().apply {
                        put("name", toolName)
                        put("arguments", arguments)
                    }
                )
            }

            val body = jsonRpcRequest.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(server.url)
                .post(body)
                .addHeader("Accept", "application/json, text/event-stream")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: return@withContext "空响应"
                try {
                    val json = JSONObject(responseBody)
                    val resultObj = json.optJSONObject("result")
                    if (resultObj != null) {
                        val content = resultObj.optJSONArray("content")
                        if (content != null) {
                            val sb = StringBuilder()
                            for (i in 0 until content.length()) {
                                val item = content.getJSONObject(i)
                                if (item.optString("type") == "text") {
                                    sb.append(item.optString("text", ""))
                                }
                            }
                            return@withContext sb.toString()
                        }
                        return@withContext resultObj.toString()
                    }
                    json.optJSONObject("error")?.let {
                        return@withContext "错误: ${it.optString("message", "未知错误")}"
                    }
                    return@withContext responseBody
                } catch (_: Exception) {
                    return@withContext responseBody
                }
            }
        } catch (e: Exception) {
            "工具调用失败: ${e.message}"
        }
    }

    private suspend fun callToolStreamableHttp(
        server: McpServer,
        toolName: String,
        arguments: JSONObject
    ): String = withContext(Dispatchers.IO) {
        try {
            val jsonRpcRequest = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", System.currentTimeMillis().toString())
                put("method", "tools/call")
                put(
                    "params", JSONObject().apply {
                        put("name", toolName)
                        put("arguments", arguments)
                    }
                )
            }

            val requestBuilder = Request.Builder()
                .url(server.url)
                .post(jsonRpcRequest.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Accept", "application/json, text/event-stream")

            if (server.headers.isNotEmpty()) {
                try {
                    val headersJson = JSONObject(server.headers)
                    headersJson.keys().forEach { key ->
                        requestBuilder.addHeader(key, headersJson.getString(key))
                    }
                } catch (_: Exception) {
                }
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseBody = response.body?.string() ?: return@withContext "空响应"
                try {
                    val json = JSONObject(responseBody)
                    val resultObj = json.optJSONObject("result")
                    if (resultObj != null) {
                        val content = resultObj.optJSONArray("content")
                        if (content != null) {
                            val sb = StringBuilder()
                            for (i in 0 until content.length()) {
                                val item = content.getJSONObject(i)
                                if (item.optString("type") == "text") {
                                    sb.append(item.optString("text", ""))
                                }
                            }
                            return@withContext sb.toString()
                        }
                        return@withContext resultObj.toString()
                    }
                    json.optJSONObject("error")?.let {
                        return@withContext "错误: ${it.optString("message", "未知错误")}"
                    }
                    return@withContext responseBody
                } catch (_: Exception) {
                    return@withContext responseBody
                }
            }
        } catch (e: Exception) {
            "工具调用失败: ${e.message}"
        }
    }

    suspend fun executeToolCallLoop(
        toolCalls: List<McpToolInvocation>,
        onResult: suspend (McpToolInvocation, String) -> Unit
    ) {
        for (call in toolCalls) {
            val result = callTool(call.serverId, call.toolName, call.arguments)
            onResult(call, result)
        }
    }

    fun disconnectServer(id: String) {
        connectionStates[id] = ConnectionState.DISCONNECTED
    }

    fun reconnectServer(id: String) {
        servers[id]?.let { server ->
            if (server.isEnabled) {
                connectServer(server)
            }
        }
    }

    fun parseDxtFile(file: File): McpServer? {
        try {
            val zipInputStream = ZipInputStream(file.inputStream())
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (entry.name == "mcp.json" || entry.name.endsWith("/mcp.json")) {
                    val content = zipInputStream.bufferedReader().readText()
                    zipInputStream.closeEntry()
                    zipInputStream.close()
                    return parseMcpJson(JSONObject(content), "dxt")
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
            zipInputStream.close()
        } catch (_: Exception) {
        }
        return null
    }

    fun parseMcpbFile(file: File): McpServer? {
        try {
            val randomAccessFile = RandomAccessFile(file, "r")
            val magic = ByteArray(4)
            randomAccessFile.read(magic)
            if (String(magic) != "MCPB") {
                randomAccessFile.close()
                return null
            }

            val lengthBytes = ByteArray(4)
            randomAccessFile.read(lengthBytes)
            val jsonLength = (lengthBytes[0].toInt() and 0xFF) or
                    ((lengthBytes[1].toInt() and 0xFF) shl 8) or
                    ((lengthBytes[2].toInt() and 0xFF) shl 16) or
                    ((lengthBytes[3].toInt() and 0xFF) shl 24)

            val jsonBytes = ByteArray(jsonLength)
            randomAccessFile.read(jsonBytes)
            randomAccessFile.close()

            val jsonString = String(jsonBytes, Charsets.UTF_8)
            return parseMcpJson(JSONObject(jsonString), "mcpb")
        } catch (_: Exception) {
        }
        return null
    }

    fun parseJsonConfig(jsonString: String, source: String = "json"): McpServer? {
        return try {
            parseMcpJson(JSONObject(jsonString), source)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseMcpJson(json: JSONObject, source: String): McpServer? {
        return try {
            val name = json.optString("name", "")
            if (name.isEmpty()) return null

            val type = json.optString("type", "stdio")
            val connectionType = when (type) {
                "stdio", "sse", "streamablehttp" -> type
                else -> "stdio"
            }

            val command = json.optString("command", "")
            val argsArray = json.optJSONArray("args")
            val args = if (argsArray != null) {
                val sb = StringBuilder()
                for (i in 0 until argsArray.length()) {
                    if (i > 0) sb.append(" ")
                    sb.append(argsArray.getString(i))
                }
                sb.toString()
            } else {
                json.optString("args", "")
            }

            val url = json.optString("url", "")
            val headers = if (json.has("headers")) {
                json.optJSONObject("headers")?.toString() ?: ""
            } else {
                ""
            }

            McpServer(
                name = name,
                connectionType = connectionType,
                command = command,
                args = args,
                url = url,
                headers = headers,
                isEnabled = true,
                source = source
            )
        } catch (_: Exception) {
            null
        }
    }
}
