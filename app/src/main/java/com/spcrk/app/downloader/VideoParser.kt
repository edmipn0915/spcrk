package com.spcrk.app.downloader

import com.spcrk.app.model.VideoInfo
import com.spcrk.app.model.VideoQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

interface VideoParser {
    suspend fun parse(url: String): VideoInfo
}

abstract class BaseVideoParser : VideoParser {

    protected val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    protected fun fetchPage(url: String, referer: String? = null): String {
        val builder = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        
        referer?.let {
            builder.addHeader("Referer", it)
        }
        
        val request = builder.build()
        return client.newCall(request).execute().body?.string() ?: ""
    }

    /** 取得最終網址（追蹤 302 重定向），用於展開 b23.tv 等短連結。 */
    protected fun resolveFinalUrl(url: String): String {
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .build()
        return client.newCall(request).execute().request.url.toString()
    }

    protected fun extractTitle(html: String, selectors: List<String>): String {
        val doc = Jsoup.parse(html)
        for (selector in selectors) {
            val element = doc.selectFirst(selector)
            if (element != null) {
                return element.text().trim()
            }
        }
        return "未知标题"
    }

    protected fun extractMetaContent(html: String, property: String): String? {
        val doc = Jsoup.parse(html)
        return doc.selectFirst("meta[property=$property]")?.attr("content")
            ?: doc.selectFirst("meta[name=$property]")?.attr("content")
    }
}

class BilibiliParser : BaseVideoParser() {

    override suspend fun parse(url: String): VideoInfo {
        // b23.tv 短連結先 302 展開成完整網頁網址，後續 API 與下載 Referer 都需用 bilibili.com 網域
        val effectiveUrl = resolveBilibiliUrl(url)
        val videoId = VideoUrlParser.extractBilibiliVideoId(effectiveUrl)
            ?: throw Exception("无法解析B站视频ID，请使用网页端链接（https://www.bilibili.com/video/BVxxx）")

        // Get video info
        val infoUrl = "https://api.bilibili.com/x/web-interface/view?bvid=$videoId"
        val infoResponse = fetchPage(infoUrl, "https://www.bilibili.com")
        val infoJson = JSONObject(infoResponse)

        if (infoJson.getInt("code") != 0) {
            throw Exception("解析B站视频失败: ${infoJson.optString("message", "未知错误")}")
        }

        val data = infoJson.getJSONObject("data")
        val title = data.getString("title")
        val pic = data.getString("pic")
        val duration = data.getLong("duration")
        val cid = data.getLong("cid")

        // 主路徑：DASH 純影像/音軌（可合併出高畫質）；失敗時回退 durl 單軌
        val qualities = fetchBilibiliDashQualities(videoId, cid)
            .ifEmpty { fetchBilibiliDurlQualities(videoId, cid) }

        if (qualities.isEmpty()) {
            throw Exception("无法获取视频下载地址，可能需要登录或该视频不可用")
        }

        val videoUrl = qualities.first().url

        return VideoInfo(
            title = title,
            url = effectiveUrl,
            platform = "B站",
            thumbnailUrl = pic,
            duration = duration,
            videoUrl = videoUrl,
            qualities = qualities
        )
    }

    /** DASH 雙軌：只留 h264/mp4 純影像 + 最佳 aac 音軌，回傳需合併的畫質清單。 */
    private fun fetchBilibiliDashQualities(videoId: String, cid: Long): List<VideoQuality> {
        try {
            val playUrl = "https://api.bilibili.com/x/player/playurl?bvid=$videoId&cid=$cid&qn=80&fnval=1"
            val playJson = JSONObject(fetchPage(playUrl, "https://www.bilibili.com/video/$videoId"))
            if (playJson.getInt("code") != 0) return emptyList()
            val data = playJson.optJSONObject("data") ?: return emptyList()
            val dash = data.optJSONObject("dash") ?: return emptyList()
            val audioUrl = pickBilibiliDashAudio(dash) ?: return emptyList()

            val videoArr = dash.optJSONArray("video") ?: return emptyList()
            val result = LinkedHashMap<Int, VideoQuality>()
            for (i in 0 until videoArr.length()) {
                val v = videoArr.optJSONObject(i) ?: continue
                val mime = v.optString("mimeType", "")
                if (!mime.contains("video/mp4") || !mime.contains("avc")) continue
                if (v.optString("baseUrl").isBlank()) continue
                val qn = v.optInt("id", 0)
                if (qn <= 0) continue
                if (result.containsKey(qn)) continue
                val (resolution, label) = bilibiliQualityInfo(qn)
                result[qn] = VideoQuality(label, v.optString("baseUrl"), resolution, v.optLong("size", 0L), audioUrl)
            }
            return result.values.sortedByDescending { resolutionHeight(it.resolution) }
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun pickBilibiliDashAudio(dash: JSONObject): String? {
        val audioArr = dash.optJSONArray("audio") ?: return null
        var best: JSONObject? = null
        for (i in 0 until audioArr.length()) {
            val a = audioArr.optJSONObject(i) ?: continue
            val mime = a.optString("mimeType", "")
            if (!mime.contains("audio/mp4") && !mime.contains("mp4a")) continue
            if (a.optString("baseUrl").isBlank()) continue
            if (best == null || a.optLong("bandwidth", 0) > best.optLong("bandwidth", 0)) {
                best = a
            }
        }
        return best?.optString("baseUrl")
    }

    /** 回退：以多檔 qn 請求 durl（單一含音軌的 progressive 格式）。 */
    private fun fetchBilibiliDurlQualities(videoId: String, cid: Long): List<VideoQuality> {
        val qualityMap = LinkedHashMap<Int, VideoQuality>()
        for (qn in listOf(112, 80, 64, 32, 16)) {
            try {
                val playUrl = "https://api.bilibili.com/x/player/playurl?bvid=$videoId&cid=$cid&qn=$qn&fnval=1"
                val playJson = JSONObject(fetchPage(playUrl, "https://www.bilibili.com/video/$videoId"))
                if (playJson.getInt("code") != 0) continue
                val durl = playJson.optJSONObject("data")?.optJSONArray("durl") ?: continue
                if (durl.length() == 0) continue
                val item = durl.getJSONObject(0)
                val streamUrl = item.getString("url")
                if (streamUrl.isBlank()) continue
                val actualQn = item.optInt("quality", qn)
                if (qualityMap.containsKey(actualQn)) continue
                val (resolution, label) = bilibiliQualityInfo(actualQn)
                qualityMap[actualQn] = VideoQuality(label, streamUrl, resolution, item.optLong("size", 0L))
            } catch (_: Exception) {
                // 單一檔位失敗不影響其他檔位
            }
        }
        return qualityMap.values.sortedByDescending { resolutionHeight(it.resolution) }
    }

    private fun bilibiliQualityInfo(qn: Int): Pair<String, String> {
        return when (qn) {
            16 -> "360P" to "标清"
            32 -> "480P" to "高清"
            64 -> "720P" to "高清"
            80 -> "1080P" to "超清"
            112 -> "1080P+" to "超清"
            116 -> "1080P60" to "高帧率"
            120 -> "4K" to "超清"
            125 -> "HDR" to "超清"
            else -> "${qn}P" to "视频"
        }
    }

    /** 展開 b23.tv 短連結；非短連結就直接回傳原網址。 */
    private fun resolveBilibiliUrl(url: String): String {
        return if (VideoUrlParser.needsBilibiliRedirect(url)) {
            resolveFinalUrl(url)
        } else {
            url
        }
    }
}

class YouTubeParser : BaseVideoParser() {

    override suspend fun parse(url: String): VideoInfo {
        val videoId = VideoUrlParser.extractYouTubeVideoId(url)
            ?: throw Exception("无法解析YouTube视频ID，请检查链接格式")

        // 使用 InnerTube ANDROID client 解析（現役版本號，避免被 bot-check 攔截）
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = YouTubeInnerTube.buildRequestBody(videoId)
                val request = Request.Builder()
                    .url(YouTubeInnerTube.PLAYER_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "com.google.android.youtube/${YouTubeInnerTube.CLIENT_VERSION} (Linux; U; Android 12) gzip")
                    .addHeader("X-Youtube-Client-Name", "3")
                    .addHeader("X-Youtube-Client-Version", YouTubeInnerTube.CLIENT_VERSION)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val json = response.body?.string()
                    ?: throw Exception("解析YouTube视频失败：返回为空")
                if (!response.isSuccessful) {
                    throw Exception("解析YouTube视频失败: HTTP ${response.code}")
                }

                val result = YouTubeInnerTube.parsePlayerResponse(json)

                // 高畫質路徑：adaptive「純影像 h264/mp4 + 純音軌 aac/mp4」合併
                val audioUrl = result.adaptiveAudioFormats
                    .filter { it.mimeType.contains("audio/mp4") && it.mimeType.contains("mp4a") }
                    .maxByOrNull { it.itag }?.url
                val adaptiveVideos = result.adaptiveVideoFormats
                    .filter {
                        it.mimeType.contains("video/mp4") &&
                            (it.mimeType.contains("avc1") || it.mimeType.contains("avc3"))
                    }
                    .distinctBy { it.height }
                    .sortedByDescending { it.height ?: 0 }

                if (audioUrl != null && adaptiveVideos.isNotEmpty()) {
                    val best = adaptiveVideos.first()
                    VideoInfo(
                        title = result.title.replace(" - YouTube", ""),
                        url = url,
                        platform = "YouTube",
                        thumbnailUrl = result.thumbnailUrl,
                        duration = result.duration,
                        videoUrl = best.url,
                        qualities = adaptiveVideos.map { f ->
                            val h = f.height ?: 0
                            val label = when {
                                h >= 1080 -> "超清"
                                h >= 720 -> "高清"
                                else -> "标清"
                            }
                            VideoQuality(label, f.url, "${h}P", audioUrl = audioUrl)
                        }
                    )
                } else {
                    // 回退：progressive 單一含音軌格式（一般只有 360P）
                    val formats = result.progressiveFormats
                        .distinctBy { it.height }
                        .sortedByDescending { it.height ?: 0 }
                    val best = formats.firstOrNull()
                        ?: throw Exception("无法获取YouTube下载地址，视频可能受版权保护或不可用")

                    VideoInfo(
                        title = result.title.replace(" - YouTube", ""),
                        url = url,
                        platform = "YouTube",
                        thumbnailUrl = result.thumbnailUrl,
                        duration = result.duration,
                        videoUrl = best.url,
                        qualities = formats.map { f ->
                            val h = f.height
                            val label = when {
                                h == null -> "标准"
                                h >= 1080 -> "超清"
                                h >= 720 -> "高清"
                                else -> "标清"
                            }
                            VideoQuality(label, f.url, "${h ?: 360}P")
                        }
                    )
                }
            } catch (e: Exception) {
                throw Exception("解析YouTube视频失败: ${e.message ?: "未知错误"}")
            }
        }
    }
}

class DouyinParser : BaseVideoParser() {

    override suspend fun parse(url: String): VideoInfo {
        val html = fetchPage(url, "https://www.douyin.com")

        val title = extractMetaContent(html, "og:title") ?: "抖音视频"
        val thumbnailUrl = extractMetaContent(html, "og:image")

        val videoUrl = extractDouyinVideoUrl(html)

        return VideoInfo(
            title = title,
            url = url,
            platform = "抖音",
            thumbnailUrl = thumbnailUrl,
            videoUrl = videoUrl,
            qualities = listOfNotNull(videoUrl?.let { VideoQuality("默认", it) })
        )
    }

    private fun extractDouyinVideoUrl(html: String): String? {
        val patterns = listOf(
            Regex("\"playApi\":\"(https?://[^\"]+)\""),
            Regex("\"play_addr\":\\{\"url_list\":\\[\"([^\"]+)\""),
            Regex("\"src\":\"(https?://[^\"]+\\.mp4[^\"]*)\""),
            Regex("\"playAddr\":\"(https?://[^\"]+)\"")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(html)
            if (match != null) {
                return match.groupValues[1].replace("\\u0026", "&")
            }
        }
        return null
    }
}

class KuaishouParser : BaseVideoParser() {

    override suspend fun parse(url: String): VideoInfo {
        val html = fetchPage(url, "https://www.kuaishou.com")

        val title = extractMetaContent(html, "og:title") ?: "快手视频"
        val thumbnailUrl = extractMetaContent(html, "og:image")

        val videoUrl = extractKuaishouVideoUrl(html)

        return VideoInfo(
            title = title,
            url = url,
            platform = "快手",
            thumbnailUrl = thumbnailUrl,
            videoUrl = videoUrl,
            qualities = listOfNotNull(videoUrl?.let { VideoQuality("默认", it) })
        )
    }

    private fun extractKuaishouVideoUrl(html: String): String? {
        val patterns = listOf(
            Regex("\"srcNoMark\":\"(https?://[^\"]+)\""),
            Regex("\"src\":\"(https?://[^\"]+\\.mp4[^\"]*)\""),
            Regex("\"videoUrl\":\"(https?://[^\"]+)\"")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(html)
            if (match != null) {
                return match.groupValues[1].replace("\\u0026", "&")
            }
        }
        return null
    }
}

class YoukuParser : BaseVideoParser() {

    override suspend fun parse(url: String): VideoInfo {
        val html = fetchPage(url, "https://www.youku.com")

        val title = extractMetaContent(html, "og:title") ?: "优酷视频"
        val thumbnailUrl = extractMetaContent(html, "og:image")

        val videoUrl = extractYoukuVideoUrl(html)

        return VideoInfo(
            title = title,
            url = url,
            platform = "优酷",
            thumbnailUrl = thumbnailUrl,
            videoUrl = videoUrl,
            qualities = listOfNotNull(videoUrl?.let { VideoQuality("默认", it) })
        )
    }

    private fun extractYoukuVideoUrl(html: String): String? {
        val patterns = listOf(
            Regex("\"videoUrl\":\"(https?://[^\"]+)\""),
            Regex("\"url\":\"(https?://[^\"]+\\.mp4[^\"]*)\""),
            Regex("\"stream_url\":\"(https?://[^\"]+)\"")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(html)
            if (match != null) {
                return match.groupValues[1].replace("\\u0026", "&")
            }
        }
        return null
    }
}

class WeiboParser : BaseVideoParser() {

    override suspend fun parse(url: String): VideoInfo {
        val html = fetchPage(url, "https://weibo.com")

        val title = extractMetaContent(html, "og:title") ?: "微博视频"
        val thumbnailUrl = extractMetaContent(html, "og:image")

        val videoUrl = extractWeiboVideoUrl(html)

        return VideoInfo(
            title = title,
            url = url,
            platform = "微博",
            thumbnailUrl = thumbnailUrl,
            videoUrl = videoUrl,
            qualities = listOfNotNull(videoUrl?.let { VideoQuality("默认", it) })
        )
    }

    private fun extractWeiboVideoUrl(html: String): String? {
        val patterns = listOf(
            Regex("\"mp4_hd_mp4\":\"(https?://[^\"]+)\""),
            Regex("\"mp4_ld_mp4\":\"(https?://[^\"]+)\""),
            Regex("\"videoUrl\":\"(https?://[^\"]+)\""),
            Regex("\"url\":\"(https?://[^\"]+\\.mp4[^\"]*)\"")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(html)
            if (match != null) {
                return match.groupValues[1].replace("\\u0026", "&")
            }
        }
        return null
    }
}

/** 從 "1080P"、"4K" 等解析度字串取出代表高度；無法解析回 0。 */
private fun resolutionHeight(resolution: String?): Int {
    if (resolution.isNullOrBlank()) return 0
    val n = Regex("\\d+").find(resolution)?.value?.toIntOrNull() ?: return 0
    return if (resolution.contains("K", ignoreCase = true)) n * 1000 else n
}

open class BaseParser : BaseVideoParser() {
    override suspend fun parse(url: String): VideoInfo {
        throw UnsupportedPlatformException("暂不支持该平台")
    }
}
