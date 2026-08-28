package com.spcrk.app.ai.api

/**
 * McpTool 和 McpToolInvocation 信息放在 api 包，供 McpService 接口使用。
 */
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: org.json.JSONObject?
)

data class McpToolInvocation(
    val serverId: String,
    val toolName: String,
    val arguments: org.json.JSONObject
)
