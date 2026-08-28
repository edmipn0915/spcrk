package com.spcrk.app.ai.api

import com.spcrk.app.data.DefaultValues
import com.spcrk.app.data.model.ModelConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultValuesTest {

    @Test
    fun `DefaultValues has correct base URL`() {
        assertEquals("https://api.openai.com/v1", DefaultValues.DEFAULT_BASE_URL)
    }

    @Test
    fun `DefaultValues has correct model name`() {
        assertEquals("gpt-4o-mini", DefaultValues.DEFAULT_MODEL_NAME)
    }

    @Test
    fun `DefaultValues has correct temperature`() {
        assertEquals(0.7f, DefaultValues.DEFAULT_TEMPERATURE)
    }

    @Test
    fun `DefaultValues has correct max tokens`() {
        assertEquals(4096, DefaultValues.DEFAULT_MAX_TOKENS)
    }

    @Test
    fun `DefaultValues has correct timeout`() {
        assertEquals(60, DefaultValues.DEFAULT_TIMEOUT)
    }

    @Test
    fun `ModelConfig uses DefaultValues for defaults`() {
        val config = ModelConfig(name = "test-model")
        assertEquals(DefaultValues.DEFAULT_BASE_URL, config.baseUrl)
        assertEquals(DefaultValues.DEFAULT_MODEL_NAME, config.modelName)
        assertEquals(DefaultValues.DEFAULT_TEMPERATURE, config.temperature)
        assertEquals(DefaultValues.DEFAULT_MAX_TOKENS, config.maxTokens)
        assertEquals(DefaultValues.DEFAULT_TIMEOUT, config.timeout)
    }

    @Test
    fun `ModelConfig allows overriding defaults`() {
        val config = ModelConfig(
            name = "custom",
            baseUrl = "https://custom.api/v1",
            modelName = "my-model",
            temperature = 0.9f,
            maxTokens = 8192,
            timeout = 120
        )
        assertEquals("https://custom.api/v1", config.baseUrl)
        assertEquals("my-model", config.modelName)
        assertEquals(0.9f, config.temperature)
        assertEquals(8192, config.maxTokens)
        assertEquals(120, config.timeout)
    }

    @Test
    fun `ModelConfig auto-generates UUID when id not provided`() {
        val config1 = ModelConfig(name = "a")
        val config2 = ModelConfig(name = "b")
        assertEquals("a", config1.name)
        assertEquals("b", config2.name)
        assertEquals("", config1.apiKey)
        assertEquals(true, config1.isEnabled)
        assertTrue(config1.id.isNotEmpty())
        assertTrue(config2.id.isNotEmpty())
        assertEquals(config1.id, config1.id)
    }
}
