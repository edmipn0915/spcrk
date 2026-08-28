package com.spcrk.app.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spcrk.app.getAppContainer
import com.spcrk.app.data.ModelConfigStore
import com.spcrk.app.data.model.ModelConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CodeUiState(
    val question: String = "",
    val answer: String = "",
    val isAsking: Boolean = false,
    val errorMessage: String? = null
)

class CodeAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val container = getAppContainer(application)
    private val modelStore = container.modelConfigStore
    private val settingsStore = container.settingsStore
    private val aiManager = container.aiManager
    private val _uiState = MutableStateFlow(CodeUiState())
    val uiState: StateFlow<CodeUiState> = _uiState.asStateFlow()

    fun updateQuestion(q: String) {
        _uiState.value = _uiState.value.copy(question = q)
    }

    fun selectedConfig(): ModelConfig? = modelStore.resolveSelectedConfig(settingsStore.defaultModelIdFlow.value)

    fun ask() {
        val question = _uiState.value.question.trim()
        if (question.isEmpty()) return

        val config = selectedConfig()
        if (config == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "请先配置AI模型")
            return
        }

        _uiState.value = _uiState.value.copy(isAsking = true, errorMessage = null, answer = "")

        val systemPrompt = "你是一个专业的编程助手。请用简洁、准确的方式回答编程问题。回答中使用 Markdown 格式化代码块。"
        val messages = listOf(
            mapOf("role" to "system", "content" to systemPrompt),
            mapOf("role" to "user", "content" to question)
        )

        viewModelScope.launch {
            try {
                val builder = StringBuilder()
                aiManager.chatStream(config, messages).collect { token ->
                    builder.append(token)
                    _uiState.value = _uiState.value.copy(answer = builder.toString())
                }
                _uiState.value = _uiState.value.copy(isAsking = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAsking = false,
                    errorMessage = e.message ?: "请求失败"
                )
            }
        }
    }
}
