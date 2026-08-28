package com.spcrk.app.data

import com.spcrk.app.data.model.ModelConfig
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * ModelConfigStore 序列化邏輯（純 JVM，用 Map 模擬 SharedPreferences）。
 * 覆蓋三項已知 bug：apiKey 不持久化、loadConfigs 9 倍膨脹、removeConfig 刪除後復活。
 */
class ModelConfigSerializerTest {

    private fun roundTrip(configs: List<ModelConfig>): List<ModelConfig> {
        val prefs: Map<String, Any?> = ModelConfigSerializer.encode(configs)
        return ModelConfigSerializer.storedIds(prefs).map { id ->
            ModelConfigSerializer.decode(id, prefs)
        }
    }

    @Test
    fun `round trip preserves apiKey`() {
        val config = ModelConfig(name = "gpt", provider = "openai", apiKey = "sk-secret", modelName = "gpt-4")
        val restored = roundTrip(listOf(config))
        assertEquals(1, restored.size)
        assertEquals("sk-secret", restored[0].apiKey)
    }

    @Test
    fun `round trip yields exactly one entry per config`() {
        val configs = (1..3).map { ModelConfig(name = "model$it") }
        assertEquals(3, roundTrip(configs).size)
    }

    @Test
    fun `legacy scan does not inflate entries from field keys`() {
        val prefs = mapOf<String, Any?>(
            "model_abc" to "gpt",
            "model_abc_provider" to "openai",
            "model_abc_apiKey" to "key",
            "model_xyz" to "claude"
        )
        assertEquals(listOf("abc", "xyz"), ModelConfigSerializer.storedIds(prefs))
    }

    @Test
    fun `legacy scan ignores id list key itself`() {
        val prefs = mapOf<String, Any?>(
            "model_ids" to "",
            "model_a" to "gpt"
        )
        assertEquals(listOf("a"), ModelConfigSerializer.storedIds(prefs))
    }

    @Test
    fun `rewriting after removal does not revive removed config`() {
        val configs = (1..3).map { ModelConfig(name = "model$it") }
        val kept = configs.filterIndexed { i, _ -> i != 1 }
        val restored = roundTrip(kept)
        assertEquals(2, restored.size)
        assertEquals(configs[0].id, restored[0].id)
        assertEquals(configs[2].id, restored[1].id)
    }

    @Test
    fun `decode falls back to defaults for missing fields`() {
        val config = ModelConfigSerializer.decode("missing", emptyMap<String, Any?>())
        assertEquals("missing", config.id)
        assertEquals("", config.apiKey)
        assertEquals("custom", config.provider)
    }

    @Test
    fun `encode writes apiKey and id list keys`() {
        val config = ModelConfig(name = "gpt", provider = "openai", apiKey = "sk-secret")
        val encoded = ModelConfigSerializer.encode(listOf(config))
        assertEquals("sk-secret", encoded["model_${config.id}_apiKey"])
        assertEquals(config.id, encoded["model_ids"])
    }
}
