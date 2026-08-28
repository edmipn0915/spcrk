package com.spcrk.app.data

import com.spcrk.app.data.model.ModelConfig

/**
 * ModelConfig 的 SharedPreferences 序列化純邏輯（無 Android 依賴，可單元測試）。
 *
 * 修復三個已知問題：
 * 1. apiKey 未持久化
 * 2. loadConfigs 遍歷所有 model_ 前綴 key，造成每筆配置 9 倍膨脹成垃圾條目
 * 3. removeConfig 不清除舊 key，刪除後下次 load 復活
 */
internal object ModelConfigSerializer {

    const val IDS_KEY = "model_ids"

    private val FIELD_SUFFIXES = listOf(
        "", "_provider", "_apiKey", "_baseUrl", "_modelName",
        "_temperature", "_maxTokens", "_timeout", "_enabled", "_default"
    )

    /** 從 prefs 快照取得已儲存配置的 id 清單（優先讀 model_ids，退化到舊版掃描）。 */
    fun storedIds(prefs: Map<String, *>): List<String> {
        val fromList = (prefs[IDS_KEY] as? String)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
        if (!fromList.isNullOrEmpty()) return fromList

        // 舊版資料：掃描 model_<id> 前綴 key，跳過欄位 key（剩餘部分含 _）與 id 清單 key 本身
        return prefs.keys
            .map { it.removePrefix("model_") }
            .filter { it.isNotEmpty() && it != "ids" && !it.contains('_') }
            .distinct()
    }

    /** 把配置編碼成 prefs 的 key-value 對應（不含 default_model_id / translate_model_id）。 */
    fun encode(configs: List<ModelConfig>): Map<String, Any> {
        val map = linkedMapOf<String, Any>()
        configs.forEach { c ->
            map["model_${c.id}"] = c.name
            map["model_${c.id}_provider"] = c.provider
            map["model_${c.id}_apiKey"] = c.apiKey
            map["model_${c.id}_baseUrl"] = c.baseUrl
            map["model_${c.id}_modelName"] = c.modelName
            map["model_${c.id}_temperature"] = c.temperature
            map["model_${c.id}_maxTokens"] = c.maxTokens
            map["model_${c.id}_timeout"] = c.timeout
            map["model_${c.id}_enabled"] = c.isEnabled
            map["model_${c.id}_default"] = c.isDefault
        }
        map[IDS_KEY] = configs.joinToString(",") { it.id }
        return map
    }

    /** 讀取單一配置（缺少欄位時回退預設值）。 */
    fun decode(id: String, prefs: Map<String, *>): ModelConfig {
        val name = (prefs["model_$id"] as? String) ?: ""
        val provider = (prefs["model_${id}_provider"] as? String) ?: "custom"
        val apiKey = (prefs["model_${id}_apiKey"] as? String) ?: ""
        val baseUrl = (prefs["model_${id}_baseUrl"] as? String) ?: DefaultValues.DEFAULT_BASE_URL
        val modelName = (prefs["model_${id}_modelName"] as? String) ?: DefaultValues.DEFAULT_MODEL_NAME
        val temperature = (prefs["model_${id}_temperature"] as? Float) ?: DefaultValues.DEFAULT_TEMPERATURE
        val maxTokens = (prefs["model_${id}_maxTokens"] as? Int) ?: DefaultValues.DEFAULT_MAX_TOKENS
        val timeout = (prefs["model_${id}_timeout"] as? Int) ?: DefaultValues.DEFAULT_TIMEOUT
        val isEnabled = (prefs["model_${id}_enabled"] as? Boolean) ?: true
        val isDefault = (prefs["model_${id}_default"] as? Boolean) ?: false
        return ModelConfig(
            id = id,
            name = name,
            provider = provider,
            apiKey = apiKey,
            baseUrl = baseUrl,
            modelName = modelName,
            temperature = temperature,
            maxTokens = maxTokens,
            isEnabled = isEnabled,
            isDefault = isDefault,
            timeout = timeout
        )
    }

    /** 給定 id 的全部 prefs key（含空 suffix），供 saveConfigs 清除舊資料用。 */
    fun configKeys(id: String): List<String> = FIELD_SUFFIXES.map { "model_$id$it" }
}
