package com.spcrk.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spcrk.app.getAppContainer
import com.spcrk.app.data.model.ModelConfig
import com.spcrk.app.ai.PresetModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class ModelManageUiState(
    val configs: List<ModelConfig> = emptyList(),
    val defaultModelId: String = "",
    val showAddDialog: Boolean = false,
    val editingConfig: ModelConfig? = null,
    val testResult: Map<String, String> = emptyMap(),
    val isTesting: Boolean = false,
    val ollamaModels: List<OllamaModelInfo> = emptyList(),
    val showOllamaDialog: Boolean = false,
    val isDiscovering: Boolean = false
)

data class OllamaModelInfo(
    val name: String,
    val size: Long,
    val digest: String
)

class ModelManageViewModel(application: Application) : AndroidViewModel(application) {
    private val container = getAppContainer(application)
    private val store = container.modelConfigStore
    private val _uiState = MutableStateFlow(ModelManageUiState())
    val uiState: StateFlow<ModelManageUiState> = _uiState.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        _uiState.value = _uiState.value.copy(configs = store.loadConfigs())
    }

    fun addConfig(config: ModelConfig) {
        store.addConfig(config)
        _uiState.value = _uiState.value.copy(configs = store.loadConfigs(), showAddDialog = false)
    }

    fun updateConfig(config: ModelConfig) {
        store.updateConfig(config)
        _uiState.value = _uiState.value.copy(configs = store.loadConfigs(), editingConfig = null)
    }

    fun removeConfig(id: String) {
        store.removeConfig(id)
        _uiState.value = _uiState.value.copy(configs = store.loadConfigs())
    }

    fun setDefault(id: String) {
        store.setDefault(id)
        _uiState.value = _uiState.value.copy(configs = store.loadConfigs(), defaultModelId = id)
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, editingConfig = null)
    }

    fun editConfig(config: ModelConfig) {
        _uiState.value = _uiState.value.copy(editingConfig = config)
    }

    fun testConnection(config: ModelConfig) {
        _uiState.value = _uiState.value.copy(
            isTesting = true,
            testResult = _uiState.value.testResult + (config.id to "")
        )
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                performConnectionTest(config)
            }
            _uiState.value = _uiState.value.copy(
                testResult = _uiState.value.testResult + (config.id to result),
                isTesting = false
            )
        }
    }

    private fun performConnectionTest(config: ModelConfig): String {
        return try {
            val url = when (config.provider) {
                "ollama" -> config.baseUrl.removeSuffix("/v1")
                else -> config.baseUrl
            }

            val endpoint = when (config.provider) {
                "ollama" -> "$url/api/tags"
                "anthropic" -> "$url/messages"
                "gemini" -> "$url/models?key=${config.apiKey}"
                else -> "$url/chat/completions"
            }

            val requestBuilder = when (config.provider) {
                "anthropic" -> {
                    val body = """{"model":"${config.modelName}","max_tokens":1,"messages":[{"role":"user","content":"hi"}]}"""
                    Request.Builder()
                        .url(endpoint)
                        .addHeader("x-api-key", config.apiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .post(body.toRequestBody("application/json".toMediaType()))
                }
                "gemini" -> {
                    Request.Builder()
                        .url(endpoint)
                        .get()
                }
                "ollama" -> {
                    Request.Builder()
                        .url(endpoint)
                        .get()
                }
                else -> {
                    val body = """{"model":"${config.modelName}","messages":[{"role":"user","content":"hi"}],"max_tokens":1}"""
                    Request.Builder()
                        .url(endpoint)
                        .addHeader("Authorization", "Bearer ${config.apiKey}")
                        .post(body.toRequestBody("application/json".toMediaType()))
                }
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                "连接成功"
            } else {
                val body = response.body?.string()?.take(200) ?: ""
                "连接失败: HTTP ${response.code}"
            }
        } catch (e: Exception) {
            "连接失败: ${e.message ?: "未知错误"}"
        }
    }

    fun discoverOllamaModels(baseUrl: String) {
        _uiState.value = _uiState.value.copy(isDiscovering = true, ollamaModels = emptyList())
        viewModelScope.launch {
            val models = withContext(Dispatchers.IO) {
                performOllamaDiscovery(baseUrl)
            }
            _uiState.value = _uiState.value.copy(
                ollamaModels = models,
                isDiscovering = false,
                showOllamaDialog = true
            )
        }
    }

    private fun performOllamaDiscovery(baseUrl: String): List<OllamaModelInfo> {
        return try {
            val cleanUrl = baseUrl.removeSuffix("/v1").removeSuffix("/")
            val request = Request.Builder()
                .url("$cleanUrl/api/tags")
                .get()
                .build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return emptyList()
                val models = mutableListOf<OllamaModelInfo>()
                val jsonModelsRegex = """"name"\s*:\s*"([^"]+)"""".toRegex()
                val jsonSizeRegex = """"size"\s*:\s*(\d+)""".toRegex()
                val jsonDigestRegex = """"digest"\s*:\s*"([^"]+)"""".toRegex()

                val names = jsonModelsRegex.findAll(body).map { it.groupValues[1] }.toList()
                val sizes = jsonSizeRegex.findAll(body).map { it.groupValues[1].toLongOrNull() ?: 0L }.toList()
                val digests = jsonDigestRegex.findAll(body).map { it.groupValues[1] }.toList()

                for (i in names.indices) {
                    models.add(
                        OllamaModelInfo(
                            name = names[i],
                            size = sizes.getOrNull(i) ?: 0L,
                            digest = digests.getOrNull(i) ?: ""
                        )
                    )
                }
                models
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("[ModelManageViewModel] fetch ollama models failed: ${e.message}")
            emptyList()
        }
    }

    fun hideOllamaDialog() {
        _uiState.value = _uiState.value.copy(showOllamaDialog = false)
    }

    fun addOllamaModel(modelName: String, baseUrl: String) {
        val config = ModelConfig(
            name = "Ollama-$modelName",
            provider = "ollama",
            baseUrl = baseUrl,
            modelName = modelName
        )
        addConfig(config)
        hideOllamaDialog()
    }

    fun addPresetModel(presetKey: String) {
        val preset = PresetModels.presets[presetKey] ?: return
        val config = preset.copy(id = java.util.UUID.randomUUID().toString())
        addConfig(config)
    }
}
