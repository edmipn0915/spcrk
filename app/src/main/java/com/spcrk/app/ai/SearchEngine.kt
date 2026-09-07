package com.spcrk.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String
)

fun providerDisplayName(provider: String): String = when (provider) {
    "openai" -> "OpenAI"
    "anthropic" -> "Anthropic"
    "gemini" -> "Google Gemini"
    "deepseek" -> "DeepSeek"
    "grok" -> "xAI Grok"
    "mistral" -> "Mistral"
    "cerebras" -> "Cerebras"
    "mimo" -> "Xiaomi MiMo"
    "zhipu" -> "智谱 GLM"
    "moonshot" -> "Moonshot"
    "baichuan" -> "百川 AI"
    "dashscope" -> "通义千问"
    "stepfun" -> "阶跃星辰"
    "doubao" -> "豆包"
    "minimax" -> "MiniMax"
    "perplexity" -> "Perplexity"
    "nvidia" -> "NVIDIA"
    "groq" -> "Groq"
    "together" -> "Together"
    "fireworks" -> "Fireworks"
    "huggingface" -> "Hugging Face"
    "jina" -> "Jina"
    "voyageai" -> "VoyageAI"
    "azure-openai" -> "Azure OpenAI"
    "vertexai" -> "VertexAI"
    "aws-bedrock" -> "AWS Bedrock"
    "github" -> "GitHub Models"
    "copilot" -> "GitHub Copilot"
    "agnes-ai" -> "Agnes AI"
    "custom" -> "自定义"
    else -> provider
}

val providerBaseUrls: Map<String, String> = mapOf(
    "openai" to "https://api.openai.com/v1",
    "anthropic" to "https://api.anthropic.com/v1",
    "gemini" to "https://generativelanguage.googleapis.com/v1beta",
    "deepseek" to "https://api.deepseek.com/v1",
    "grok" to "https://api.x.ai/v1",
    "mistral" to "https://api.mistral.ai/v1",
    "cerebras" to "https://api.cerebras.ai/v1",
    "mimo" to "https://api.xiaomi.com/v1",
    "zhipu" to "https://open.bigmodel.cn/api/paas/v4",
    "moonshot" to "https://api.moonshot.cn/v1",
    "baichuan" to "https://api.baichuan-ai.com/v1",
    "dashscope" to "https://dashscope.aliyuncs.com/compatible-mode/v1",
    "stepfun" to "https://api.stepfun.com/v1",
    "doubao" to "https://ark.cn-beijing.volces.com/api/v3",
    "minimax" to "https://api.minimax.chat/v1",
    "perplexity" to "https://api.perplexity.ai",
    "nvidia" to "https://integrate.api.nvidia.com/v1",
    "groq" to "https://api.groq.com/openai/v1",
    "together" to "https://api.together.xyz/v1",
    "fireworks" to "https://api.fireworks.ai/inference/v1",
    "huggingface" to "https://api-inference.huggingface.co/v1",
    "jina" to "https://api.jina.ai/v1",
    "voyageai" to "https://api.voyageai.com/v1",
    "azure-openai" to "https://openai.azure.com/openai/deployments",
    "vertexai" to "https://us-central1-aiplatform.googleapis.com/v1",
    "aws-bedrock" to "https://bedrock-runtime.us-east-1.amazonaws.com",
    "github" to "https://models.github.ai/inference",
    "copilot" to "https://api.githubcopilot.com",
    "ollama" to "http://localhost:11434/v1",
    "lmstudio" to "http://localhost:1234/v1",
    "agnes-ai" to "https://apihub.agnes-ai.com/v1",
    "custom" to ""
)

val presetModelsByProvider: Map<String, List<String>> = mapOf(
    "agnes-ai" to listOf("agnes-2.5-flash", "agnes-2.0-flash"),
    "openai" to listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-4", "gpt-3.5-turbo"),
    "anthropic" to listOf("claude-3-5-sonnet-20241022", "claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307"),
    "gemini" to listOf("gemini-2.0-flash", "gemini-1.5-pro", "gemini-1.5-flash", "gemini-1.0-pro"),
    "deepseek" to listOf("deepseek-chat", "deepseek-coder", "deepseek-reasoner"),
    "grok" to listOf("grok-2", "grok-2-vision", "grok-2-beta"),
    "mistral" to listOf("mistral-large-latest", "mistral-medium-latest", "mistral-small-latest", "mistral-nemo"),
    "cerebras" to listOf("llama-3.1-70b", "llama-3.1-8b", "llama-3.1-405b"),
    "zhipu" to listOf("glm-4", "glm-4-plus", "glm-4-air", "glm-4-flash"),
    "moonshot" to listOf("moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"),
    "baichuan" to listOf("Baichuan4", "Baichuan3-Turbo"),
    "dashscope" to listOf("qwen-max", "qwen-plus", "qwen-turbo", "qwen-long"),
    "stepfun" to listOf("step-1-8k", "step-1-32k"),
    "doubao" to listOf("doubao-1.5-pro-256k", "doubao-1.5-flash-256k"),
    "minimax" to listOf("minimax-01", "minimax-text-01"),
    "perplexity" to listOf("sonar", "sonar-pro"),
    "nvidia" to listOf("mistralai/mistral-7b-instruct-v0.2", "meta/llama-3-70b-instruct"),
    "groq" to listOf("llama-3.1-70b-versatile", "llama-3.1-8b-instant", "llama3-70b-8192", "llama3-8b-8192"),
    "together" to listOf("meta-llama/Meta-Llama-3.1-70B-Instruct-Turbo", "meta-llama/Meta-Llama-3.1-8B-Instruct-Turbo"),
    "fireworks" to listOf("firefly-ultra-v2"),
    "ollama" to listOf("llama3.2", "llama3.1", "qwen2.5", "mistral", "codellama"),
    "lmstudio" to listOf("auto"),
    "custom" to listOf("custom")
)

val providerTags: Map<String, List<String>> = mapOf(
    "agnes-ai" to listOf("recommended", "paid"),
    "openai" to listOf("recommended", "paid"),
    "anthropic" to listOf("recommended", "paid"),
    "gemini" to listOf("free", "recommended"),
    "deepseek" to listOf("free", "cheap"),
    "ollama" to listOf("local", "free"),
    "lmstudio" to listOf("local", "free")
)

val providerNotes: Map<String, String?> = mapOf(
    "agnes-ai" to "OpenAI 相容聚合 API，官方文档 agnes-ai.com",
    "openai" to "最稳定的 API，模型丰富",
    "anthropic" to "Claude 系列，推理能力强",
    "gemini" to "Google Gemini，有免费额度",
    "deepseek" to "国内可用，性价比高",
    "ollama" to "本地运行，无需网络",
    "lmstudio" to "本地运行，支持多种模型",
    "silicon" to "国际站请改 https://api.siliconflow.com/v1",
    "dmxapi" to "默认中国站 .cn；国际站请改 https://www.dmxapi.com/v1，须与帐号地区配对",
    "ocoolai" to "默认香港；美国 api.ocoolai.com，马来 my.ocoolai.com",
    "302ai" to "默认国际节点；大陆节点请改 https://api.302ai.cn/v1",
    "baidu-cloud" to "百度千帆 V2 接口，key 需用 bce-v3- 开头（AK/SK 签发）",
    "tokenhub" to "默认腾讯 TokenHub 境外站；境内请改 https://tokenhub.tencentmaas.com/v1",
    "new-api" to "自托管：填你的 New API 伺服器位址 + /v1（Docker 预设 3000 端口）",
    "gpustack" to "自托管：填你的 GPUStack 伺服器位址 + /v1",
    "ovms" to "自托管：OpenVINO Model Server，OpenAI 相容端点为 http://伺服器:port/v3",
    "xirang" to "息壤无固定端点，请从你帐号的服务详情页取得 Base URL",
    "radeon-cloud" to "AMD GPU Cloud 为 GPU droplet，无固定端点，以实例 vLLM 位址为准",
    "opencode" to "若为 OpenCode Zen 官方闸道请填 https://opencode.ai/zen/v1（名称有歧义故未预设）",
    "grok-cli" to "Grok CLI 为终端 agent 非 API 供应商，底层请用 grok (xAI)"
)

object PresetModels {
    val presets: Map<String, com.spcrk.app.data.model.ModelConfig> = mapOf(
        "openai" to com.spcrk.app.data.model.ModelConfig(name = "OpenAI", provider = "openai", baseUrl = "https://api.openai.com/v1", modelName = "gpt-4o-mini", isEnabled = false),
        "anthropic" to com.spcrk.app.data.model.ModelConfig(name = "Anthropic", provider = "anthropic", baseUrl = "https://api.anthropic.com/v1", modelName = "claude-3-5-sonnet-20241022", isEnabled = false),
        "gemini" to com.spcrk.app.data.model.ModelConfig(name = "Gemini", provider = "gemini", baseUrl = "https://generativelanguage.googleapis.com/v1beta", modelName = "gemini-2.0-flash", isEnabled = false),
        "deepseek" to com.spcrk.app.data.model.ModelConfig(name = "DeepSeek", provider = "deepseek", baseUrl = "https://api.deepseek.com/v1", modelName = "deepseek-chat", isEnabled = false),
        "grok" to com.spcrk.app.data.model.ModelConfig(name = "Grok", provider = "grok", baseUrl = "https://api.x.ai/v1", modelName = "grok-2", isEnabled = false),
        "mistral" to com.spcrk.app.data.model.ModelConfig(name = "Mistral", provider = "mistral", baseUrl = "https://api.mistral.ai/v1", modelName = "mistral-large-latest", isEnabled = false),
        "ollama" to com.spcrk.app.data.model.ModelConfig(name = "Ollama", provider = "ollama", baseUrl = "http://localhost:11434/v1", modelName = "llama3.2", isEnabled = false),
        "lmstudio" to com.spcrk.app.data.model.ModelConfig(name = "LM Studio", provider = "lmstudio", baseUrl = "http://localhost:1234/v1", modelName = "auto", isEnabled = false)
    )
}

data class SearchConfig(
    val engine: String = "duckduckgo",
    val apiKey: String = "",
    val baseUrl: String = "",
    val maxResults: Int = 5,
    val urlContentProvider: String = "builtin"
)

class SearchEngine {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun search(query: String): List<SearchResult> {
        return search(query, SearchConfig())
    }

    suspend fun search(query: String, config: SearchConfig): List<SearchResult> = withContext(Dispatchers.IO) {
        try {
            when (config.engine) {
                "duckduckgo" -> searchDuckDuckGo(query, config.maxResults)
                "bing" -> searchBing(query, config.maxResults)
                "tavily" -> searchTavily(query, config)
                "searxng" -> searchSearxng(query, config)
                "exa" -> searchExa(query, config)
                "exa-mcp" -> searchExa(query, config)
                "bocha" -> searchBocha(query, config)
                "jina" -> searchJina(query, config)
                "zhipu" -> searchZhipu(query, config)
                "firecrawl" -> searchFirecrawl(query, config)
                else -> searchDuckDuckGo(query, config.maxResults)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchUrlContent(url: String, provider: String = "builtin"): String = withContext(Dispatchers.IO) {
        try {
            when (provider) {
                "jina" -> fetchUrlContentJina(url)
                "firecrawl" -> fetchUrlContentFirecrawl(url)
                else -> fetchUrlContentBuiltin(url)
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun searchDuckDuckGo(query: String, maxResults: Int): List<SearchResult> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://html.duckduckgo.com/html/?q=$encodedQuery"

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val body = response.body?.string() ?: return emptyList()
        return parseDuckDuckGoResults(body, maxResults)
    }

    internal fun parseDuckDuckGoResults(html: String, maxResults: Int): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val doc: Document = Jsoup.parse(html)

        val resultElements = doc.select("div.result, .links_main, .links_deep")

        if (resultElements.isEmpty()) {
            return parseAlternativeFormat(doc, maxResults)
        }

        for (element in resultElements) {
            if (results.size >= maxResults) break

            val titleEl = element.selectFirst("a.result__a, a.large, h2 a")
            val snippetEl = element.selectFirst("a.result__snippet, .result__snippet, .snippet")
            val urlEl = element.selectFirst("a.result__url, a.large")

            val title = titleEl?.text()?.trim() ?: ""
            var href = titleEl?.attr("href") ?: urlEl?.attr("href") ?: ""
            val snippet = snippetEl?.text()?.trim() ?: element.selectFirst(".result__body")?.text()?.trim() ?: ""

            if (href.startsWith("//duckduckgo.com/l/")) {
                val uddgPattern = Regex("[?&]uddg=([^&]+)")
                val match = uddgPattern.find(href)
                if (match != null) {
                    href = try {
                        java.net.URLDecoder.decode(match.groupValues[1], "UTF-8")
                    } catch (e: Exception) {
                        href
                    }
                }
            }

            if (title.isNotEmpty() && href.isNotEmpty() && !href.contains("duckduckgo.com/y.js")) {
                results.add(SearchResult(title, href, snippet))
            }
        }

        if (results.isEmpty()) {
            return parseAlternativeFormat(doc, maxResults)
        }

        return results
    }

    private fun parseAlternativeFormat(doc: Document, maxResults: Int): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        doc.select("#links .result, .web-result").forEach { element ->
            if (results.size >= maxResults) return@forEach

            val linkEl = element.selectFirst("a")
            val title = linkEl?.text()?.trim() ?: ""
            var href = linkEl?.attr("abs:href")?.trim() ?: ""
            val snippet = element.selectFirst(".result__snippet, .snippet, .st")?.text()?.trim() ?: ""

            if (href.isNotEmpty() && title.isNotEmpty()) {
                results.add(SearchResult(title, href, snippet))
            }
        }

        if (results.isEmpty()) {
            doc.select("a[href]").forEach { link ->
                if (results.size >= maxResults) return@forEach

                val href = link.attr("abs:href")
                val title = link.text().trim()

                if (href.isNotEmpty() && title.length > 10 &&
                    !href.contains("duckduckgo.com") &&
                    !href.contains("javascript:") &&
                    (href.startsWith("http://") || href.startsWith("https://"))) {
                    results.add(SearchResult(title, href, ""))
                }
            }
        }

        return results
    }

    private fun searchBing(query: String, maxResults: Int): List<SearchResult> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://www.bing.com/search?q=$encodedQuery&count=$maxResults"

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val body = response.body?.string() ?: return emptyList()
        return parseBingResults(body, maxResults)
    }

    private fun parseBingResults(html: String, maxResults: Int): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val doc = Jsoup.parse(html)

        doc.select("li.b_algo").forEach { element ->
            if (results.size >= maxResults) return@forEach

            val titleEl = element.selectFirst("h2 a")
            val snippetEl = element.selectFirst(".b_caption p, .b_algoSlug")

            val title = titleEl?.text()?.trim() ?: ""
            val href = titleEl?.attr("abs:href")?.trim() ?: ""
            val snippet = snippetEl?.text()?.trim() ?: ""

            if (title.isNotEmpty() && href.isNotEmpty()) {
                results.add(SearchResult(title, href, snippet))
            }
        }

        if (results.isEmpty()) {
            doc.select(".b_results li").forEach { element ->
                if (results.size >= maxResults) return@forEach
                val linkEl = element.selectFirst("a")
                val title = linkEl?.text()?.trim() ?: ""
                val href = linkEl?.attr("abs:href")?.trim() ?: ""
                if (title.isNotEmpty() && href.isNotEmpty() && href.startsWith("http")) {
                    results.add(SearchResult(title, href, ""))
                }
            }
        }

        return results
    }

    private fun searchTavily(query: String, config: SearchConfig): List<SearchResult> {
        if (config.apiKey.isEmpty()) return emptyList()

        val json = JSONObject().apply {
            put("api_key", config.apiKey)
            put("query", query)
            put("max_results", config.maxResults)
            put("search_depth", "basic")
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.tavily.com/search")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val responseBody = response.body?.string() ?: return emptyList()
        val jsonResponse = JSONObject(responseBody)
        val resultsArray = jsonResponse.optJSONArray("results") ?: return emptyList()

        val results = mutableListOf<SearchResult>()
        for (i in 0 until resultsArray.length()) {
            if (results.size >= config.maxResults) break
            val item = resultsArray.getJSONObject(i)
            val title = item.optString("title", "")
            val url = item.optString("url", "")
            val snippet = item.optString("content", "")
            if (title.isNotEmpty() || url.isNotEmpty()) {
                results.add(SearchResult(title, url, snippet))
            }
        }
        return results
    }

    private fun searchSearxng(query: String, config: SearchConfig): List<SearchResult> {
        val baseUrl = config.baseUrl.trimEnd('/')
        if (baseUrl.isEmpty()) return emptyList()

        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "$baseUrl/search?q=$encodedQuery&format=json"

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .addHeader("Accept", "application/json")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val responseBody = response.body?.string() ?: return emptyList()
        val jsonResponse = JSONObject(responseBody)
        val resultsArray = jsonResponse.optJSONArray("results") ?: return emptyList()

        val results = mutableListOf<SearchResult>()
        for (i in 0 until resultsArray.length()) {
            if (results.size >= config.maxResults) break
            val item = resultsArray.getJSONObject(i)
            val title = item.optString("title", "")
            val url = item.optString("url", "")
            val snippet = item.optString("content", item.optString("snippet", ""))
            if (title.isNotEmpty() || url.isNotEmpty()) {
                results.add(SearchResult(title, url, snippet))
            }
        }
        return results
    }

    private fun searchExa(query: String, config: SearchConfig): List<SearchResult> {
        if (config.apiKey.isEmpty()) return emptyList()

        val json = JSONObject().apply {
            put("query", query)
            put("numResults", config.maxResults)
            put("type", "neural")
            put("contents", JSONObject().apply {
                put("text", JSONObject().apply {
                    put("maxCharacters", 300)
                })
            })
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.exa.ai/search")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-api-key", config.apiKey)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val responseBody = response.body?.string() ?: return emptyList()
        val jsonResponse = JSONObject(responseBody)
        val resultsArray = jsonResponse.optJSONArray("results") ?: return emptyList()

        val results = mutableListOf<SearchResult>()
        for (i in 0 until resultsArray.length()) {
            if (results.size >= config.maxResults) break
            val item = resultsArray.getJSONObject(i)
            val title = item.optString("title", "")
            val url = item.optString("url", "")
            val snippet = item.optString("text", "")
            if (title.isNotEmpty() || url.isNotEmpty()) {
                results.add(SearchResult(title, url, snippet))
            }
        }
        return results
    }

    private fun searchBocha(query: String, config: SearchConfig): List<SearchResult> {
        if (config.apiKey.isEmpty()) return emptyList()

        val json = JSONObject().apply {
            put("query", query)
            put("count", config.maxResults)
            put("freshness", "oneYear")
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.bocha.cn/v1/web-search")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val responseBody = response.body?.string() ?: return emptyList()
        val jsonResponse = JSONObject(responseBody)
        val dataObj = jsonResponse.optJSONObject("data") ?: return emptyList()
        val webPages = dataObj.optJSONObject("webPages") ?: return emptyList()
        val resultsArray = webPages.optJSONArray("value") ?: return emptyList()

        val results = mutableListOf<SearchResult>()
        for (i in 0 until resultsArray.length()) {
            if (results.size >= config.maxResults) break
            val item = resultsArray.getJSONObject(i)
            val title = item.optString("name", item.optString("title", ""))
            val url = item.optString("url", "")
            val snippet = item.optString("snippet", item.optString("description", ""))
            if (title.isNotEmpty() || url.isNotEmpty()) {
                results.add(SearchResult(title, url, snippet))
            }
        }
        return results
    }

    private fun searchJina(query: String, config: SearchConfig): List<SearchResult> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://s.jina.ai/$encodedQuery"

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .addHeader("Accept", "application/json")
            .addHeader("X-Return-Format", "text")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val responseBody = response.body?.string() ?: return emptyList()

        return try {
            val jsonResponse = JSONObject(responseBody)
            val resultsArray = jsonResponse.optJSONArray("data") ?: return emptyList()
            val results = mutableListOf<SearchResult>()
            for (i in 0 until resultsArray.length()) {
                if (results.size >= config.maxResults) break
                val item = resultsArray.getJSONObject(i)
                val title = item.optString("title", "")
                val url = item.optString("url", item.optString("link", ""))
                val snippet = item.optString("description", item.optString("snippet", ""))
                if (title.isNotEmpty() || url.isNotEmpty()) {
                    results.add(SearchResult(title, url, snippet))
                }
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun searchZhipu(query: String, config: SearchConfig): List<SearchResult> {
        if (config.apiKey.isEmpty()) return emptyList()

        val request = buildZhipuRequest(query, config.apiKey)
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val responseBody = response.body?.string() ?: return emptyList()
        return parseZhipuResponse(responseBody, config.maxResults)
    }

    private fun buildZhipuRequest(query: String, apiKey: String): Request {
        val json = JSONObject().apply {
            put("request_id", System.currentTimeMillis().toString())
            put("tool", "web-search-pro")
            put("stream", false)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", query)
                })
            })
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        return Request.Builder()
            .url("https://open.bigmodel.cn/api/paas/v4/tools")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", apiKey)
            .build()
    }

    private fun parseZhipuResponse(responseBody: String, maxResults: Int): List<SearchResult> {
        return try {
            val jsonResponse = JSONObject(responseBody)
            val choices = jsonResponse.optJSONArray("choices") ?: return emptyList()
            if (choices.length() == 0) return emptyList()
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.optJSONObject("message") ?: return emptyList()
            val toolCalls = message.optJSONArray("tool_calls") ?: return emptyList()

            val results = mutableListOf<SearchResult>()
            for (i in 0 until toolCalls.length()) {
                val toolCall = toolCalls.getJSONObject(i)
                val function = toolCall.optJSONObject("function") ?: continue
                val argumentsStr = function.optString("arguments", "")
                if (argumentsStr.isNotEmpty()) {
                    try {
                        val args = JSONObject(argumentsStr)
                        val searchResults = args.optJSONArray("search_result") ?: args.optJSONArray("results")
                        if (searchResults != null) {
                            for (j in 0 until searchResults.length()) {
                                if (results.size >= maxResults) break
                                val item = searchResults.getJSONObject(j)
                                val title = item.optString("title", "")
                                val url = item.optString("link", item.optString("url", ""))
                                val snippet = item.optString("content", item.optString("snippet", ""))
                                if (title.isNotEmpty() || url.isNotEmpty()) {
                                    results.add(SearchResult(title, url, snippet))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("[SearchEngine] search error: ${e.message}")
                    }
                }
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun searchFirecrawl(query: String, config: SearchConfig): List<SearchResult> {
        if (config.apiKey.isEmpty()) return emptyList()

        val json = JSONObject().apply {
            put("url", "https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}&num=${config.maxResults}")
            put("formats", JSONArray().put("markdown"))
            put("onlyMainContent", true)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.firecrawl.dev/v1/scrape")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val responseBody = response.body?.string() ?: return emptyList()

        return try {
            val jsonResponse = JSONObject(responseBody)
            val success = jsonResponse.optBoolean("success", false)
            if (!success) return emptyList()
            val data = jsonResponse.optJSONObject("data") ?: return emptyList()
            val metadata = data.optJSONObject("metadata") ?: return emptyList()
            val markdown = data.optString("markdown", "")
            val sourceUrl = metadata.optString("sourceURL", "")

            if (markdown.isNotEmpty()) {
                listOf(SearchResult(
                    title = metadata.optString("title", "Search Results"),
                    url = sourceUrl,
                    snippet = markdown.take(500)
                ))
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun fetchUrlContentJina(url: String): String {
        val encodedUrl = URLEncoder.encode(url, "UTF-8")
        val requestUrl = "https://r.jina.ai/$encodedUrl"

        val request = Request.Builder()
            .url(requestUrl)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .addHeader("Accept", "text/plain")
            .addHeader("X-Return-Format", "text")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return ""
        return response.body?.string() ?: ""
    }

    private fun fetchUrlContentFirecrawl(url: String): String {
        val json = JSONObject().apply {
            put("url", url)
            put("formats", JSONArray().put("markdown"))
            put("onlyMainContent", true)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.firecrawl.dev/v1/scrape")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${SearchConfig().apiKey}")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return ""

        val responseBody = response.body?.string() ?: return ""
        return try {
            val jsonResponse = JSONObject(responseBody)
            val success = jsonResponse.optBoolean("success", false)
            if (!success) return ""
            val data = jsonResponse.optJSONObject("data") ?: return ""
            data.optString("markdown", "")
        } catch (e: Exception) {
            ""
        }
    }

    private fun fetchUrlContentBuiltin(url: String): String {
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return ""

        val body = response.body?.string() ?: return ""
        val doc = Jsoup.parse(body)

        doc.select("script, style, nav, footer, header, aside").remove()

        val article = doc.selectFirst("article, main, .content, .post, .article, #content, .entry-content")
        if (article != null) {
            return article.text().trim()
        }

        val paragraphs = doc.select("p")
        if (paragraphs.isNotEmpty()) {
            return paragraphs.joinToString("\n\n") { it.text().trim() }
        }

        return doc.body()?.text()?.trim() ?: ""
    }
}
