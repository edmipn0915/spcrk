package com.spcrk.app.data

import android.content.Context
import com.spcrk.app.ai.ModelConfig
import com.spcrk.app.ai.PresetModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ModelConfigStore(context: Context) {
    private val prefs = context.getSharedPreferences("model_configs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "configs"
    private val defaultKey = "default_config_id"

    fun saveConfigs(configs: List<ModelConfig>) {
        prefs.edit().putString(key, gson.toJson(configs)).apply()
    }

    fun loadConfigs(): List<ModelConfig> {
        val json = prefs.getString(key, null) ?: return getDefaultConfigs()
        val saved = try {
            val type = object : TypeToken<List<ModelConfig>>() {}.type
            gson.fromJson<List<ModelConfig>>(json, type) ?: return getDefaultConfigs()
        } catch (_: Exception) {
            return getDefaultConfigs()
        }
        // 合并：保留用户已有配置，并补充新增的内置厂商（默认关闭）
        val savedProviders = saved.map { it.provider }.toSet()
        val missingDefaults = PresetModels.presets.values
            .filter { it.provider !in savedProviders }
            .map { preset ->
                preset.copy(id = java.util.UUID.randomUUID().toString(), isEnabled = false)
            }
        return if (missingDefaults.isEmpty()) saved else saved + missingDefaults
    }

    fun addConfig(config: ModelConfig) {
        val configs = loadConfigs().toMutableList()
        configs.add(config)
        saveConfigs(configs)
    }

    fun removeConfig(id: String) {
        val configs = loadConfigs().filter { it.id != id }
        saveConfigs(configs)
        if (prefs.getString(defaultKey, null) == id) {
            prefs.edit().remove(defaultKey).apply()
        }
    }

    fun updateConfig(config: ModelConfig) {
        val configs = loadConfigs().map { if (it.id == config.id) config else it }
        saveConfigs(configs)
    }

    fun getEnabledConfigs(): List<ModelConfig> = loadConfigs().filter { it.isEnabled }

    fun setDefault(id: String) {
        val configs = loadConfigs().map { it.copy(isDefault = it.id == id) }
        saveConfigs(configs)
        prefs.edit().putString(defaultKey, id).apply()
    }

    fun getDefault(): ModelConfig? {
        val configs = loadConfigs()
        val defaultId = prefs.getString(defaultKey, null)
        return configs.find { it.id == defaultId } ?: configs.firstOrNull { it.isDefault }
    }

    /**
     * 按厂商类型分组：返回已配置的厂商类型列表（去重、保序）。
     */
    fun getProviders(): List<String> {
        return loadConfigs().map { it.provider }.distinct()
    }

    /**
     * 返回指定厂商下的全部模型配置。
     */
    fun getModelsByProvider(provider: String): List<ModelConfig> {
        return loadConfigs().filter { it.provider == provider }
    }

    /**
     * 为某厂商批量添加模型条目（每个模型名生成一条 ModelConfig）。
     * 已存在相同 provider + modelName 的条目会被跳过。
     * 新模型的启用状态跟随厂商组当前状态（默认关闭，用户打开厂商后才启用）。
     */
    fun addModelsForProvider(provider: String, baseUrl: String, apiKey: String, modelNames: List<String>) {
        val configs = loadConfigs().toMutableList()
        val existing = configs.map { "${it.provider}|${it.modelName}" }.toSet()
        val providerEnabled = configs.firstOrNull { it.provider == provider }?.isEnabled ?: false
        modelNames.forEach { modelName ->
            val key = "$provider|$modelName"
            if (key !in existing) {
                configs.add(
                    ModelConfig(
                        name = modelName,
                        provider = provider,
                        apiKey = apiKey,
                        baseUrl = baseUrl,
                        modelName = modelName,
                        isEnabled = providerEnabled
                    )
                )
                existing.plus(key)
            }
        }
        saveConfigs(configs)
    }

    /**
     * 批量更新某厂商下所有配置的 baseUrl / apiKey。
     */
    fun updateProvider(provider: String, baseUrl: String, apiKey: String) {
        val configs = loadConfigs().map {
            if (it.provider == provider) it.copy(baseUrl = baseUrl, apiKey = apiKey) else it
        }
        saveConfigs(configs)
    }

    /**
     * 从 OpenAI 兼容 API（GET {baseUrl}/models）或 Ollama（GET {baseUrl}/api/tags）拉取模型列表。
     * @throws Exception 网络或解析失败时抛出带错误信息的异常。
     */
    suspend fun fetchModelsFromApi(provider: String, baseUrl: String, apiKey: String): List<String> =
        withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            val cleanBase = baseUrl.trim().trimEnd('/')

            if (provider == "ollama") {
                val tagsUrl = cleanBase.removeSuffix("/v1").removeSuffix("/") + "/api/tags"
                val request = Request.Builder().url(tagsUrl).get().build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("获取模型失败: HTTP ${response.code}")
                    }
                    val body = response.body?.string() ?: return@use emptyList()
                    val regex = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"")
                    return@use regex.findAll(body).map { it.groupValues[1] }.distinct().toList()
                }
            } else {
                val request = Request.Builder()
                    .url("$cleanBase/models")
                    .header("Authorization", "Bearer $apiKey")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("获取模型失败: HTTP ${response.code}")
                    }
                    val body = response.body?.string() ?: return@use emptyList()
                    val json = try {
                        JSONObject(body)
                    } catch (e: Exception) {
                        throw Exception("响应解析失败: ${e.message}")
                    }
                    val arr = json.optJSONArray("data") ?: return@use emptyList()
                    (0 until arr.length()).mapNotNull { i ->
                        arr.optJSONObject(i)?.optString("id")?.takeIf { it.isNotBlank() }
                    }
                }
            }
        }

    fun importFromJson(json: String): Boolean {
        return try {
            val type = object : TypeToken<List<ModelConfig>>() {}.type
            val imported: List<ModelConfig> = gson.fromJson(json, type) ?: return false
            if (imported.isNotEmpty()) {
                saveConfigs(imported)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun exportToJson(): String {
        return gson.toJson(loadConfigs())
    }

    private fun getDefaultConfigs(): List<ModelConfig> {
        // 默认全部关闭，用户需要时再到设置中手动打开对应厂商
        return PresetModels.presets.values.map { preset ->
            preset.copy(id = java.util.UUID.randomUUID().toString(), isEnabled = false)
        }
    }
}
