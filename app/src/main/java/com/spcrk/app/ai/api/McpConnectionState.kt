package com.spcrk.app.ai.api

/** MCP 服务器连接状态。移至 api 层以避免 McpService 反向依赖 McpManager。 */
enum class McpConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    ERROR
}
