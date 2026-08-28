package com.spcrk.app.data

import android.content.Context
import com.spcrk.app.data.DefaultValues
import com.spcrk.app.data.model.ModelConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModelConfigStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("model_config_prefs", Context.MODE_PRIVATE)

    private val _configs = MutableStateFlow<List<ModelConfig>>(emptyList())
    val configs: StateFlow<List<ModelConfig>> = _configs.asStateFlow()

    private val _defaultModelId = MutableStateFlow("")
    val defaultModelIdFlow: StateFlow<String> = _defaultModelId.asStateFlow()

    private val _translateModelId = MutableStateFlow("")
    val translateModelIdFlow: StateFlow<String> = _translateModelId.asStateFlow()

    fun saveConfigs(modelConfigs: List<ModelConfig>) {
        _configs.value = modelConfigs
        val editor = prefs.edit()
        // 清除所有舊配置 key，避免刪除的配置殘留復活
        ModelConfigSerializer.storedIds(prefs.all).forEach { id ->
            ModelConfigSerializer.configKeys(id).forEach { key -> editor.remove(key) }
        }
        editor.remove(ModelConfigSerializer.IDS_KEY)
        ModelConfigSerializer.encode(modelConfigs).forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Float -> editor.putFloat(key, value)
                is Boolean -> editor.putBoolean(key, value)
            }
        }
        editor.putString("default_model_id", _defaultModelId.value)
        editor.apply()
    }

    fun loadConfigs(): List<ModelConfig> {
        val prefsSnapshot = prefs.all
        val configs = ModelConfigSerializer.storedIds(prefsSnapshot).map { id ->
            ModelConfigSerializer.decode(id, prefsSnapshot)
        }
        _defaultModelId.value = prefs.getString("default_model_id", "") ?: ""
        _translateModelId.value = prefs.getString("translate_model_id", "") ?: ""
        return configs
    }

    fun setDefaultModelId(modelId: String) {
        _defaultModelId.value = modelId
        prefs.edit().putString("default_model_id", modelId).apply()
    }

    fun setTranslateModelId(modelId: String) {
        _translateModelId.value = modelId
        prefs.edit().putString("translate_model_id", modelId).apply()
    }

    fun getDefault(): ModelConfig? {
        val id = prefs.getString("default_model_id", "") ?: ""
        if (id.isBlank()) return null
        return loadConfigs().find { it.id == id }?.takeIf { it.isEnabled }
    }

    /** Resolves the selected config by priority: explicitly selected → default → first enabled. */
    fun resolveSelectedConfig(selectedId: String, allConfigs: List<ModelConfig> = loadConfigs()): ModelConfig? {
        val enabled = allConfigs.filter { it.isEnabled }
        return enabled.find { it.id == selectedId }
            ?: getDefault()
            ?: enabled.firstOrNull()
    }

    fun getProviders(): List<String> = loadConfigs().map { it.provider }.distinct()

    fun getModelsByProvider(provider: String): List<ModelConfig> = loadConfigs().filter { it.provider == provider }

    fun updateConfig(config: ModelConfig) {
        val configs = loadConfigs().map { if (it.id == config.id) config else it }
        saveConfigs(configs)
    }

    fun addConfig(config: ModelConfig) {
        val configs = loadConfigs() + config
        saveConfigs(configs)
    }

    fun removeConfig(id: String) {
        val configs = loadConfigs().filter { it.id != id }
        saveConfigs(configs)
    }

    fun setDefault(modelId: String) {
        val configs = loadConfigs().map { it.copy(isDefault = it.id == modelId) }
        saveConfigs(configs)
        setDefaultModelId(modelId)
    }

    fun addModelsForProvider(provider: String, baseUrl: String, apiKey: String, modelNames: List<String>) {
        val existing = loadConfigs().filter { it.provider == provider }.map { it.modelName }.toSet()
        val newModels = modelNames.filter { it !in existing }.map { name ->
            ModelConfig(
                name = name,
                provider = provider,
                baseUrl = baseUrl,
                apiKey = apiKey,
                modelName = name,
                isEnabled = false
            )
        }
        if (newModels.isNotEmpty()) {
            saveConfigs(loadConfigs() + newModels)
        }
    }

    suspend fun fetchModelsFromApi(provider: String, baseUrl: String, apiKey: String): List<String> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder().connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS).readTimeout(10, java.util.concurrent.TimeUnit.SECONDS).build()
                val url = when (provider) {
                    "openai", "anthropic", "gemini", "deepseek", "mistral", "groq", "cerebras", "moonshot",
                    "baichuan", "dashscope", "stepfun", "doubao", "minimax", "perplexity", "nvidia",
                    "together", "fireworks", "huggingface", "jina", "voyageai", "ollama", "lmstudio" ->
                        "$baseUrl/models"
                    else -> "$baseUrl/v1/models"
                }
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $apiKey")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: ""
                val json = org.json.JSONObject(body)
                val list = json.optJSONArray("data") ?: return@withContext emptyList()
                (0 until list.length()).map { list.optJSONObject(it)?.optString("id") ?: "" }.filter { it.isNotEmpty() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun updateProvider(provider: String, baseUrl: String, apiKey: String) {
        val configs = loadConfigs().map {
            if (it.provider == provider) it.copy(baseUrl = baseUrl, apiKey = apiKey) else it
        }
        saveConfigs(configs)
    }
}
