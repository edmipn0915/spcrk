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

    private fun searchDuckDuckGo(query: String, maxResults: Int): List<SearchResult> {
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

    private fun parseDuckDuckGoResults(html: String, maxResults: Int): List<SearchResult> {
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
        val request = Request.Builder()
            .url("https://open.bigmodel.cn/api/paas/v4/tools")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", config.apiKey)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val responseBody = response.body?.string() ?: return emptyList()

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
                                if (results.size >= config.maxResults) break
                                val item = searchResults.getJSONObject(j)
                                val title = item.optString("title", "")
                                val url = item.optString("link", item.optString("url", ""))
                                val snippet = item.optString("content", item.optString("snippet", ""))
                                if (title.isNotEmpty() || url.isNotEmpty()) {
                                    results.add(SearchResult(title, url, snippet))
                                }
                            }
                        }
                    } catch (_: Exception) { }
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
