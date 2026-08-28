package com.spcrk.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ProviderCatalog 供應商目錄測試：
 * - 目錄應包含全部 60 家供應商（使用者回報「60幾家廠商沒有了」）
 * - 每家都有顯示名稱
 * - id 不重複
 * - 常見供應商的 baseUrl 正確
 */
class ProviderCatalogTest {

    @Test
    fun `catalog contains at least 60 providers`() {
        assertTrue(
            "供應商目錄應至少 60 家，實際 ${ProviderCatalog.all.size}",
            ProviderCatalog.all.size >= 60
        )
    }

    @Test
    fun `every provider has a non-blank display name`() {
        ProviderCatalog.all.forEach { p ->
            assertTrue(
                "供應商 ${p.id} 缺少顯示名稱",
                p.displayName.isNotBlank()
            )
        }
    }

    @Test
    fun `provider ids are unique`() {
        val ids = ProviderCatalog.all.map { it.id }
        assertEquals(
            "供應商 id 有重複",
            ids.size,
            ids.toSet().size
        )
    }

    @Test
    fun `common providers are present`() {
        val ids = ProviderCatalog.all.map { it.id }.toSet()
        listOf("openai", "anthropic", "gemini", "deepseek", "agnes-ai", "custom").forEach { id ->
            assertTrue("缺少供應商 $id", id in ids)
        }
    }

    @Test
    fun `openai base url is correct`() {
        assertEquals(
            "https://api.openai.com/v1",
            ProviderCatalog.baseUrl("openai")
        )
    }

    @Test
    fun `unknown provider base url falls back to blank`() {
        assertEquals("", ProviderCatalog.baseUrl("no-such-provider"))
    }

    @Test
    fun `display name lookup works`() {
        assertEquals("OpenAI", ProviderCatalog.displayName("openai"))
        assertEquals("自定义", ProviderCatalog.displayName("custom"))
    }
}
