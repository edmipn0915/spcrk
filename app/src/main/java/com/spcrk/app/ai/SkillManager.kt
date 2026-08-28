package com.spcrk.app.ai

import android.app.Application
import com.spcrk.app.ai.api.SkillService
import com.spcrk.app.data.Skill
import com.spcrk.app.data.Repository
import com.spcrk.app.getAppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class SkillManager(private val application: Application) : SkillService {
    private val repository = getAppContainer(application).repository
    private val searchEngine = SearchEngine()
    private val skillTriggers = mutableMapOf<String, suspend (String) -> String>()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        registerBuiltInSkills()
    }

    override fun getAllSkills(): Flow<List<Skill>> = repository.getAllSkills()

    override suspend fun installSkill(skill: Skill) {
        repository.addSkill(skill)
        registerSkillTrigger(skill)
    }

    override suspend fun uninstallSkill(id: Long) {
        val skillList: List<Skill> = repository.getAllSkills().first() ?: emptyList()
        val skill = skillList.find { it.id == id }
        if (skill != null) {
            repository.deleteSkill(skill)
            skillTriggers.remove(skill.trigger)
        }
    }

    override suspend fun enableSkill(id: Long, enabled: Boolean) {
        val skillList: List<Skill> = repository.getAllSkills().first() ?: emptyList()
        val skill = skillList.find { it.id == id }
        if (skill != null) {
            repository.updateSkill(skill.copy(isEnabled = enabled))
        }
    }

    override suspend fun initializeBuiltInSkills() {
        val builtInSkills = listOf(
            Skill(
                name = "天气查询",
                description = "查询指定城市的天气信息",
                trigger = "weather",
                config = JSONObject().apply {
                    put("action", "weather")
                }.toString()
            ),
            Skill(
                name = "新闻摘要",
                description = "获取最新新闻摘要",
                trigger = "news",
                config = JSONObject().apply {
                    put("action", "news")
                }.toString()
            ),
            Skill(
                name = "网页摘要",
                description = "获取指定网页的内容摘要",
                trigger = "web",
                config = JSONObject().apply {
                    put("action", "web_summary")
                }.toString()
            )
        )

        builtInSkills.forEach { skill ->
            val existing = repository.findSkillByTrigger(skill.trigger)
            if (existing == null) {
                installSkill(skill)
            }
        }
    }

    private fun registerBuiltInSkills() {
        skillTriggers["weather"] = { input -> executeWeatherSkill(input) }
        skillTriggers["news"] = { input -> executeNewsSkill(input) }
        skillTriggers["web"] = { input -> executeWebSummarySkill(input) }
    }

    private fun registerSkillTrigger(skill: Skill) {
        skillTriggers[skill.trigger] = { input ->
            executeCustomSkill(skill, input)
        }
    }

    private fun executeCustomSkill(skill: Skill, input: String): String {
        return try {
            val config = JSONObject(skill.config)
            val action = config.optString("action", "")
            when (action) {
                "weather" -> executeWeatherSkill(input)
                "news" -> executeNewsSkill(input)
                "web_summary" -> executeWebSummarySkill(input)
                "http_request" -> executeHttpRequest(config, input)
                "text_transform" -> executeTextTransform(config, input)
                else -> "未知的 Skill 动作: $action"
            }
        } catch (e: Exception) {
            "Skill 执行失败: ${e.message}"
        }
    }

    private fun executeWeatherSkill(input: String): String {
        return try {
            val city = input.ifBlank { "Beijing" }
            val encodedCity = URLEncoder.encode(city, "UTF-8")
            val url = "https://wttr.in/$encodedCity?format=j1"
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "curl/7.68.0")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return "天气查询失败: HTTP ${response.code}"

            val body = response.body?.string() ?: return "天气查询失败: 空响应"
            val json = JSONObject(body)
            val current = json.getJSONArray("current_condition").getJSONObject(0)
            val weatherDesc = current.getJSONArray("weatherDesc").getJSONObject(0).getString("value")
            val tempC = current.getString("temp_C")
            val humidity = current.getString("humidity")
            val windSpeed = current.getString("windspeedKmph")
            val feelsLike = current.getString("FeelsLikeC")

            val nearestArea = json.getJSONArray("nearest_area").getJSONObject(0)
            val areaName = nearestArea.getJSONArray("areaName").getJSONObject(0).getString("value")
            val region = nearestArea.getJSONArray("region").getJSONObject(0).getString("value")
            val country = nearestArea.getJSONArray("country").getJSONObject(0).getString("value")

            buildString {
                appendLine("📍 $areaName, $region, $country")
                appendLine("🌡️ 温度: ${tempC}°C (体感 ${feelsLike}°C)")
                appendLine("☁️ 天气: $weatherDesc")
                appendLine("💧 湿度: $humidity%")
                appendLine("💨 风速: ${windSpeed} km/h")
            }.trim()
        } catch (e: Exception) {
            "天气查询失败: ${e.message}"
        }
    }

    private fun executeNewsSkill(input: String): String {
        return try {
            val query = input.ifBlank { "latest news" }
            val results = searchEngine.searchDuckDuckGo("$query news", 5)

            if (results.isEmpty()) return "未找到相关新闻"

            buildString {
                appendLine("📰 新闻摘要:")
                appendLine()
                results.take(5).forEachIndexed { index: Int, result: SearchResult ->
                    appendLine("${index + 1}. ${result.title}")
                    appendLine("   ${result.snippet}")
                    appendLine("   🔗 ${result.url}")
                    appendLine()
                }
            }.trim()
        } catch (e: Exception) {
            "新闻查询失败: ${e.message}"
        }
    }

    private fun executeWebSummarySkill(input: String): String {
        return try {
            if (input.isBlank()) return "请提供要摘要的网页 URL"
            val url = if (input.startsWith("http")) input else "https://$input"
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return "网页访问失败: HTTP ${response.code}"

            val body = response.body?.string() ?: return "网页访问失败: 空响应"

            val titleRegex = Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE)
            val title = titleRegex.find(body)?.groupValues?.get(1)?.trim() ?: "无标题"

            val cleanText = body
                .replace(Regex("<script.*?</script>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<style.*?</style>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<[^>]+>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

            val summary = if (cleanText.length > 500) cleanText.substring(0, 500) + "..." else cleanText

            buildString {
                appendLine("🌐 网页摘要")
                appendLine("标题: $title")
                appendLine("URL: $input")
                appendLine()
                appendLine("内容摘要:")
                appendLine(summary)
            }.trim()
        } catch (e: Exception) {
            "网页摘要失败: ${e.message}"
        }
    }

    private fun executeHttpRequest(config: JSONObject, input: String): String {
        return try {
            val url = config.optString("url", "").replace("{input}", URLEncoder.encode(input, "UTF-8"))
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            response.body?.string() ?: "空响应"
        } catch (e: Exception) {
            "请求失败: ${e.message}"
        }
    }

    private fun executeTextTransform(config: JSONObject, input: String): String {
        val prefix = config.optString("prefix", "")
        val suffix = config.optString("suffix", "")
        return "$prefix$input$suffix"
    }

    override suspend fun triggerSkill(trigger: String, input: String): String {
        val handler = skillTriggers[trigger] ?: return "未找到 Skill: $trigger"
        return withContext(Dispatchers.IO) {
            handler(input)
        }
    }

    override fun getAllTriggers(): Map<String, suspend (String) -> String> {
        return skillTriggers.toMap()
    }
}
