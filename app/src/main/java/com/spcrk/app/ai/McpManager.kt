package com.spcrk.app.ai

import com.spcrk.app.ai.api.McpConnectionState
import com.spcrk.app.ai.api.McpService
import com.spcrk.app.ai.api.McpTool
import com.spcrk.app.ai.api.McpToolInvocation
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

/**
 * 把伺服器的 headers JSON 解析成 key-value 對應。
 * 修復 callToolHttp 直接把整個 headers JSON 字串當成 header 值送出的 bug。
 */
internal fun headersFromJson(headersJson: String): Map<String, String> {
    if (headersJson.isBlank()) return emptyMap()
    return try {
        val obj = JSONObject(headersJson)
        obj.keys().asSequence().associateWith { obj.getString(it) }
    } catch (e: Exception) {
        emptyMap()
    }
}

class McpManager : McpService {

    companion object {
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 120L

        private fun buildJsonRpcRequest(method: String, params: JSONObject = JSONObject()): JSONObject =
            JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", System.currentTimeMillis().toString())
                put("method", method)
                put("params", params)
            }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .build()

    private val servers = ConcurrentHashMap<String, McpServer>()
    private val tools = ConcurrentHashMap<String, List<McpTool>>()
    private val connectionStates = ConcurrentHashMap<String, McpConnectionState>()

    // ─── Server CRUD ───────────────────────────────────────────────────────────

    override fun addServer(server: McpServer) {
        servers[server.id.toString()] = server
        if (server.isEnabled) connectServer(server)
    }

    override fun removeServer(id: String) {
        servers.remove(id); tools.remove(id); connectionStates.remove(id)
    }

    override fun updateServer(server: McpServer) {
        servers[server.id.toString()] = server
        if (server.isEnabled) connectServer(server)
        else {
            tools.remove(server.id.toString())
            connectionStates[server.id.toString()] = McpConnectionState.DISCONNECTED
        }
    }

    override fun getServers(): List<McpServer> = servers.values.toList()
    override fun getServer(id: String): McpServer? = servers[id]
    override fun getConnectionState(id: String) = connectionStates[id] ?: McpConnectionState.DISCONNECTED

    override fun disconnectServer(id: String) { connectionStates[id] = McpConnectionState.DISCONNECTED }
    override fun reconnectServer(id: String) { servers[id]?.takeIf { it.isEnabled }?.let { connectServer(it) } }

    // ─── Protocol dispatch ─────────────────────────────────────────────────────

    fun connectServer(server: McpServer) {
        connectionStates[server.id.toString()] = McpConnectionState.CONNECTING
        when (server.connectionType) {
            "stdio" -> loadToolsFromStdio(server)
            else    -> connectHttp(server)
        }
    }

    override fun getToolsForServer(serverId: String): List<McpTool> = tools[serverId] ?: emptyList()
    override fun getAllTools(): Map<String, List<McpTool>> = tools.toMap()

    // ─── loadTools (2 functions: HTTP / stdio) ─────────────────────────────────

    private fun connectHttp(server: McpServer) {
        try {
            val request = buildHttpRequest(server, buildJsonRpcRequest("tools/list"))
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    connectionStates[server.id.toString()] = McpConnectionState.CONNECTED
                    parseToolsFromJson(response.body?.string() ?: "", server.id.toString())
                } else {
                    setDisconnected(server.id.toString())
                }
            }
        } catch (e: Exception) {
            setDisconnected(server.id.toString())
        }
    }

    private fun loadToolsFromStdio(server: McpServer) {
        try {
            val process = ProcessBuilder(server.command, *server.args.split(" ").toTypedArray()).start()
            process.outputStream.use { it.write((buildJsonRpcRequest("tools/list").toString() + "\n").toByteArray()) }
            val result = process.inputStream.bufferedReader().readText()
            process.destroy()
            parseToolsFromJson(result, server.id.toString())
        } catch (e: Exception) {
            println("[McpManager] stdio load tools failed: ${e.message}")
            setDisconnected(server.id.toString())
        }
    }

    // ─── callTool (2 functions: HTTP / stdio) ──────────────────────────────────

    override suspend fun callTool(serverId: String, toolName: String, arguments: JSONObject): String =
        withContext(Dispatchers.IO) {
            val server = servers[serverId] ?: return@withContext "服务器不存在"
            val params = JSONObject().apply { put("name", toolName); put("arguments", arguments) }
            try {
                when (server.connectionType) {
                    "stdio"    -> callToolStdio(server, buildJsonRpcRequest("tools/call", params))
                    else       -> callToolHttp(server, buildJsonRpcRequest("tools/call", params))
                }
            } catch (e: Exception) { "工具调用失败: ${e.message}" }
        }

    override suspend fun executeToolCallLoop(
        toolCalls: List<McpToolInvocation>,
        onResult: suspend (McpToolInvocation, String) -> Unit
    ) {
        for (call in toolCalls) {
            val result = callTool(call.serverId, call.toolName, call.arguments)
            onResult(call, result)
        }
    }

    private suspend fun callToolHttp(server: McpServer, request: JSONObject): String = withContext(Dispatchers.IO) {
        try {
            val httpBuilder = Request.Builder()
                .url(server.url)
                .post(request.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Accept", "application/json, text/event-stream")
            if (server.headers.isNotEmpty()) {
                try {
                    headersFromJson(server.headers).forEach { (k, v) -> httpBuilder.addHeader(k, v) }
                } catch (e: Exception) {
                    println("[McpManager] invalid headers JSON: ${e.message}")
                }
            }
            client.newCall(httpBuilder.build()).execute().use { response ->
                parseToolResult(response.body?.string() ?: return@withContext "空响应")
            }
        } catch (e: Exception) { "工具调用失败: ${e.message}" }
    }

    private suspend fun callToolStdio(server: McpServer, request: JSONObject): String = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder(server.command, *server.args.split(" ").toTypedArray()).start()
            process.outputStream.use { it.write((request.toString() + "\n").toByteArray()) }
            val result = process.inputStream.bufferedReader().readText()
            process.destroy()
            parseToolResult(result)
        } catch (e: Exception) { "工具调用失败: ${e.message}" }
    }

    // ─── Parse helpers ─────────────────────────────────────────────────────────

    private fun parseToolsFromJson(body: String, serverId: String) {
        try {
            val json = JSONObject(body)
            val toolsArray = json.optJSONObject("result")?.optJSONArray("tools")
                ?: json.optJSONArray("tools")
            toolsArray?.let {
                tools[serverId] = (0 until it.length()).map { i ->
                    val t = it.getJSONObject(i)
                    McpTool(t.optString("name", ""), t.optString("description", ""), t.optJSONObject("inputSchema"))
                }
            }
        } catch (e: Exception) {
            println("[McpManager] parse tools failed: ${e.message}")
        }
    }

    private fun parseToolResult(body: String): String = try {
        val json = JSONObject(body)
        val result = json.optJSONObject("result")
        if (result != null) {
            val contentArray = result.optJSONArray("content")
            var foundText = ""
            if (contentArray != null) {
                for (i in 0 until contentArray.length()) {
                    val item = contentArray.getJSONObject(i)
                    if ("text" == item.optString("type")) {
                        foundText += item.optString("text", "")
                    }
                }
            }
            if (foundText.isNotEmpty()) foundText else result.toString()
        } else {
            json.optJSONObject("error")?.let { "错误: ${it.optString("message", "未知错误")}" }
                ?: body
        }
    } catch (e: Exception) {
        println("[McpManager] parse tool result failed: ${e.message}")
        body
    }

    private fun setDisconnected(serverId: String) {
        connectionStates[serverId] = McpConnectionState.ERROR
        tools[serverId] = emptyList()
    }

    // ─── Build HTTP request ────────────────────────────────────────────────────

    private fun buildHttpRequest(server: McpServer, requestJson: JSONObject): Request {
        val builder = Request.Builder()
            .url(server.url)
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Accept", "application/json, text/event-stream")
        if (server.headers.isNotEmpty()) {
            headersFromJson(server.headers).forEach { (k, v) -> builder.addHeader(k, v) }
        }
        return builder.build()
    }

    // ─── File parsing ──────────────────────────────────────────────────────────

    override fun parseDxtFile(file: File): McpServer? = try {
        ZipInputStream(file.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "mcp.json" || entry.name.endsWith("/mcp.json")) {
                    val content = zis.bufferedReader().readText()
                    return parseMcpJson(JSONObject(content), "dxt")
                }
                zis.closeEntry(); entry = zis.nextEntry
            }
        }
        null
    } catch (e: Exception) {
        println("[McpManager] parse dxt file failed: ${e.message}")
        null
    }

    override fun parseMcpbFile(file: File): McpServer? = try {
        RandomAccessFile(file, "r").use { raf ->
            val magic = ByteArray(4); raf.read(magic)
            if (String(magic) != "MCPB") return null
            val lenBytes = ByteArray(4); raf.read(lenBytes)
            val jsonLen = lenBytes[0].toInt() and 0xFF or
                    ((lenBytes[1].toInt() and 0xFF) shl 8) or
                    ((lenBytes[2].toInt() and 0xFF) shl 16) or
                    ((lenBytes[3].toInt() and 0xFF) shl 24)
            val jsonBytes = ByteArray(jsonLen); raf.read(jsonBytes)
            parseMcpJson(JSONObject(String(jsonBytes, Charsets.UTF_8)), "mcpb")
        }
    } catch (e: Exception) {
        println("[McpManager] parse mcpb file failed: ${e.message}")
        null
    }

    override fun parseJsonConfig(jsonString: String, source: String): McpServer? =
        try { parseMcpJson(JSONObject(jsonString), source) } catch (e: Exception) {
            println("[McpManager] parse json config failed: ${e.message}")
            null
        }

    private fun parseMcpJson(json: JSONObject, source: String): McpServer? {
        return try {
            val name = json.optString("name", "")
            if (name.isEmpty()) return null
            val type = json.optString("type", "stdio")
            val connectionType = when (type) { "stdio" -> type; "sse" -> type; "streamablehttp" -> type; else -> "stdio" }
            val command = json.optString("command", "")
            val args = json.optJSONArray("args")?.let { argsArr ->
                StringBuilder().also {
                    for (i in 0 until argsArr.length()) { if (i > 0) it.append(" "); it.append(argsArr.getString(i)) }
                }.toString()
            } ?: json.optString("args", "")
            val url = json.optString("url", "")
            val headers = if (json.has("headers")) json.optJSONObject("headers")?.toString() ?: "" else ""
            McpServer(name = name, connectionType = connectionType, command = command, args = args, url = url, headers = headers, isEnabled = true, source = source)
        } catch (e: Exception) {
            println("[McpManager] parse MCP json failed: ${e.message}")
            null
        }
    }
}
