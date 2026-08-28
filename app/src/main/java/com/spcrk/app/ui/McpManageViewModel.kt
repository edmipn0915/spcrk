package com.spcrk.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spcrk.app.AppContainer
import com.spcrk.app.getAppContainer
import com.spcrk.app.data.McpServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

data class McpManageUiState(
    val servers: List<McpServer> = emptyList(),
    val isLoading: Boolean = true
)

class McpManageViewModel(application: Application) : AndroidViewModel(application) {

    private val container: AppContainer = getAppContainer(application)
    private val repository = container.repository
    private val mcpService = container.mcpService

    private val _uiState = MutableStateFlow(McpManageUiState())
    val uiState: StateFlow<McpManageUiState> = _uiState.asStateFlow()

    init { loadServers() }

    private fun loadServers() {
        viewModelScope.launch {
            repository.getAllMcpServers().collect { servers ->
                _uiState.value = McpManageUiState(servers = servers, isLoading = false)
                syncWithMcpManager(servers)
            }
        }
    }

    private fun syncWithMcpManager(servers: List<McpServer>) {
        servers.forEach { server ->
            if (mcpService.getServer(server.id.toString()) == null) {
                mcpService.addServer(server)
            }
        }
    }

    fun toggleServer(server: McpServer, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateMcpServer(server.copy(isEnabled = enabled))
            if (enabled) mcpService.addServer(server.copy(isEnabled = true))
            else mcpService.disconnectServer(server.id.toString())
        }
    }

    fun deleteServer(server: McpServer) {
        viewModelScope.launch {
            repository.deleteMcpServer(server)
            mcpService.removeServer(server.id.toString())
        }
    }

    fun addServer(server: McpServer) {
        viewModelScope.launch {
            repository.addMcpServer(server)
            mcpService.addServer(server)
        }
    }

    fun reconnectServer(server: McpServer) { mcpService.reconnectServer(server.id.toString()) }
    fun getConnectionState(serverId: String) = mcpService.getConnectionState(serverId)
    fun getToolsForServer(serverId: String) = mcpService.getToolsForServer(serverId)

    suspend fun callTool(serverId: String, toolName: String, arguments: JSONObject): String =
        mcpService.callTool(serverId, toolName, arguments)

    fun parseDxtFile(file: File) = mcpService.parseDxtFile(file)
    fun parseMcpbFile(file: File) = mcpService.parseMcpbFile(file)
    fun parseJsonConfig(jsonString: String, source: String) =
        mcpService.parseJsonConfig(jsonString, source)
}

class McpManageViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(McpManageViewModel::class.java))
            return McpManageViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
