package com.spcrk.app.ai.api

import com.spcrk.app.data.McpServer
import org.json.JSONObject
import java.io.File

interface McpService {
    fun addServer(server: McpServer)
    fun removeServer(id: String)
    fun updateServer(server: McpServer)
    fun getServers(): List<McpServer>
    fun getServer(id: String): McpServer?
    fun getConnectionState(id: String): McpConnectionState
    fun getToolsForServer(serverId: String): List<McpTool>
    fun getAllTools(): Map<String, List<McpTool>>
    suspend fun callTool(serverId: String, toolName: String, arguments: JSONObject): String
    suspend fun executeToolCallLoop(
        toolCalls: List<McpToolInvocation>,
        onResult: suspend (McpToolInvocation, String) -> Unit
    )
    fun disconnectServer(id: String)
    fun reconnectServer(id: String)
    fun parseDxtFile(file: File): McpServer?
    fun parseMcpbFile(file: File): McpServer?
    fun parseJsonConfig(jsonString: String, source: String = "json"): McpServer?
}
