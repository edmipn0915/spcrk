package com.spcrk.app.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spcrk.app.data.ModelConfigStore
import com.spcrk.app.data.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TranslateUiState(
    val sourceText: String = "",
    val translatedText: String = "",
    val targetLang: String = "中文",
    val isTranslating: Boolean = false,
    val errorMessage: String? = null
)

class TranslateViewModel(application: Application) : AndroidViewModel(application) {
    private val modelStore = ModelConfigStore(application)
    private val aiManager = AIManager()
    private val _uiState = MutableStateFlow(TranslateUiState())
    val uiState: StateFlow<TranslateUiState> = _uiState.asStateFlow()

    val languages = listOf("中文", "English", "日本語", "한국어", "Français", "Deutsch", "Español", "Русский", "العربية")

    /** 优先使用设置中选定的翻译模型，其次默认模型，最后第一条启用的配置。 */
    private fun selectedConfig(): ModelConfig? {
        val enabled = modelStore.getEnabledConfigs()
        val selectedId = SettingsStore.getInstance().translateModelIdFlow.value
        return enabled.find { it.id == selectedId }
            ?: modelStore.getDefault()
            ?: enabled.firstOrNull()
    }

    fun updateSourceText(text: String) {
        _uiState.value = _uiState.value.copy(sourceText = text)
    }

    fun setTargetLang(lang: String) {
        _uiState.value = _uiState.value.copy(targetLang = lang)
    }

    fun translate() {
        val text = _uiState.value.sourceText.trim()
        if (text.isEmpty()) return

        val config = selectedConfig()
        if (config == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "请先配置AI模型")
            return
        }

        _uiState.value = _uiState.value.copy(isTranslating = true, errorMessage = null, translatedText = "")

        val prompt = "请将以下文本翻译成${_uiState.value.targetLang}，只返回翻译结果，不要解释：\n\n$text"
        val messages = listOf(mapOf("role" to "user", "content" to prompt))

        viewModelScope.launch {
            try {
                val builder = StringBuilder()
                aiManager.chatStream(config, messages).collect { token ->
                    builder.append(token)
                    _uiState.value = _uiState.value.copy(translatedText = builder.toString())
                }
                _uiState.value = _uiState.value.copy(isTranslating = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    errorMessage = e.message ?: "翻译失败"
                )
            }
        }
    }
}
