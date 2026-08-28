package com.spcrk.app.data

/** 统一的默认值常量，避免 ModelConfig 和 ModelConfigStore 重复硬编码。 */
object DefaultValues {
    const val DEFAULT_BASE_URL = "https://api.openai.com/v1"
    const val DEFAULT_MODEL_NAME = "gpt-4o-mini"
    const val DEFAULT_TEMPERATURE = 0.7f
    const val DEFAULT_MAX_TOKENS = 4096
    const val DEFAULT_TIMEOUT = 60
}
