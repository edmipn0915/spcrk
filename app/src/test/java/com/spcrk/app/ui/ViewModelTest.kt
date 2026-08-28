package com.spcrk.app.ui

import com.spcrk.app.ai.McpManager
import com.spcrk.app.ai.api.DocumentService
import com.spcrk.app.ai.api.LocalModelService
import com.spcrk.app.ai.api.McpService
import com.spcrk.app.ai.api.OcrService
import com.spcrk.app.ai.api.SkillService
import com.spcrk.app.data.model.ModelConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureTest {

    @Test
    fun `McpManager implements McpService`() {
        val manager = McpManager()
        val service: McpService = manager
        assertNotNull(service)
    }

    @Test
    fun `SkillService is an interface`() {
        assertTrue(SkillService::class.java.isInterface)
    }

    @Test
    fun `DocumentService is an interface`() {
        assertTrue(DocumentService::class.java.isInterface)
    }

    @Test
    fun `OcrService is an interface`() {
        assertTrue(OcrService::class.java.isInterface)
    }

    @Test
    fun `LocalModelService is an interface`() {
        assertTrue(LocalModelService::class.java.isInterface)
    }

    @Test
    fun `ModelConfig has correct defaults`() {
        val config = ModelConfig(name = "gpt-4", provider = "openai", baseUrl = "https://api.openai.com/v1")
        assertEquals("gpt-4", config.name)
        assertEquals("openai", config.provider)
        assertEquals("https://api.openai.com/v1", config.baseUrl)
    }

    @Test
    fun `ModelConfig minimal constructor sets sensible defaults`() {
        val config = ModelConfig(name = "test")
        assertEquals("test", config.name)
        assertTrue(config.isEnabled)
        assertEquals("", config.apiKey)
    }
}
