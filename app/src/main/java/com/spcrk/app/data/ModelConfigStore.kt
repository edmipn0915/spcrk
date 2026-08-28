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
        modelConfigs.forEach { config ->
            editor.putString("model_${config.id}", config.name)
            editor.putString("model_${config.id}_provider", config.provider)
            editor.putString("model_${config.id}_baseUrl", config.baseUrl)
            editor.putString("model_${config.id}_modelName", config.modelName)
            editor.putFloat("model_${config.id}_temperature", config.temperature)
            editor.putInt("model_${config.id}_maxTokens", config.maxTokens)
            editor.putInt("model_${config.id}_timeout", config.timeout)
            editor.putBoolean("model_${config.id}_enabled", config.isEnabled)
            editor.putBoolean("model_${config.id}_default", config.isDefault)
        }
        editor.putString("default_model_id", _defaultModelId.value)
        editor.apply()
    }

    fun loadConfigs(): List<ModelConfig> {
        val configs = mutableListOf<ModelConfig>()
        for (i in 0 until prefs.all.size) {
            val key = prefs.all.keys.toList()[i]
            if (key.startsWith("model_")) {
                val id = key.removePrefix("model_")
                val name = prefs.getString("model_${id}", "") ?: ""
                val provider = prefs.getString("model_${id}_provider", "custom") ?: "custom"
                val baseUrl = prefs.getString("model_${id}_baseUrl", DefaultValues.DEFAULT_BASE_URL) ?: DefaultValues.DEFAULT_BASE_URL
                val modelName = prefs.getString("model_${id}_modelName", DefaultValues.DEFAULT_MODEL_NAME) ?: DefaultValues.DEFAULT_MODEL_NAME
                val temperature = prefs.getFloat("model_${id}_temperature", 0.7f)
                val maxTokens = prefs.getInt("model_${id}_maxTokens", 4096)
                val timeout = prefs.getInt("model_${id}_timeout", 60)
                val isEnabled = prefs.getBoolean("model_${id}_enabled", true)
                val isDefault = prefs.getBoolean("model_${id}_default", false)
                configs.add(ModelConfig(id, name, provider, "", baseUrl, modelName, temperature, maxTokens, isEnabled, isDefault, timeout))
            }
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
