package com.spcrk.app.data.model

import com.spcrk.app.data.DefaultValues

/**
 * AI 模型配置数据类。
 * 存放于 data.model 包，数据层不依赖 ai 层。
 */
data class ModelConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val provider: String = "custom",
    val apiKey: String = "",
    val baseUrl: String = DefaultValues.DEFAULT_BASE_URL,
    val modelName: String = DefaultValues.DEFAULT_MODEL_NAME,
    val temperature: Float = DefaultValues.DEFAULT_TEMPERATURE,
    val maxTokens: Int = DefaultValues.DEFAULT_MAX_TOKENS,
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false,
    val timeout: Int = DefaultValues.DEFAULT_TIMEOUT,
    val createdAt: Long = System.currentTimeMillis()
)
