package com.spcrk.app.downloader

import com.spcrk.app.model.VideoInfo
import com.spcrk.app.model.VideoQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
        val videoId = extractBilibiliVideoId(url)

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

        // Get play URL
        val playUrl = "https://api.bilibili.com/x/player/playurl?bvid=$videoId&cid=$cid&qn=32&fnval=1"
        val playResponse = fetchPage(playUrl, "https://www.bilibili.com/video/$videoId")
        val playJson = JSONObject(playResponse)

        if (playJson.getInt("code") != 0) {
            throw Exception("获取B站播放地址失败: ${playJson.optString("message", "未知错误")}")
        }

        val playData = playJson.getJSONObject("data")
        val durl = playData.optJSONArray("durl")
        
        var videoUrl: String? = null
        if (durl != null && durl.length() > 0) {
            videoUrl = durl.getJSONObject(0).getString("url")
        }

        if (videoUrl.isNullOrEmpty()) {
            throw Exception("无法获取视频下载地址，可能需要登录或该视频不可用")
        }

        return VideoInfo(
            title = title,
            url = url,
            platform = "B站",
            thumbnailUrl = pic,
            duration = duration,
            videoUrl = videoUrl,
            qualities = listOf(
                VideoQuality("高清", videoUrl, "480P")
            )
        )
    }

    private fun extractBilibiliVideoId(url: String): String {
        // 手機 APP 分享的 b23.tv 短連結不含 BV 號，先 302 展開成完整網頁網址再提取
        val effectiveUrl = if (VideoUrlParser.needsBilibiliRedirect(url)) {
            resolveFinalUrl(url)
        } else {
            url
        }
        return VideoUrlParser.extractBilibiliVideoId(effectiveUrl)
            ?: throw Exception("无法解析B站视频ID，请使用网页端链接（https://www.bilibili.com/video/BVxxx）")
    }
}

class YouTubeParser : BaseVideoParser() {

    override suspend fun parse(url: String): VideoInfo {
        val videoId = VideoUrlParser.extractYouTubeVideoId(url)
            ?: throw Exception("无法解析YouTube视频ID，请检查链接格式")

        // 使用 java-youtube-downloader 解析器（比 Regex 抓取更穩定）
        return withContext(Dispatchers.IO) {
            val downloader = com.github.kiulian.downloader.YoutubeDownloader()
            val response = downloader.getVideoInfo(
                com.github.kiulian.downloader.downloader.request.RequestVideoInfo(videoId)
            )
            if (!response.ok()) {
                throw Exception("解析YouTube视频失败: ${response.error()?.message ?: "未知错误"}")
            }
            val info = response.data() ?: throw Exception("解析YouTube视频失败：返回为空")
            val details = info.details()

            // 挑選最佳的含音軌畫質（720p 以上優先，避免音訊分離問題）
            val formats = info.videoWithAudioFormats()
            if (formats.isEmpty()) {
                throw Exception("无法获取YouTube下载地址，视频可能受版权保护或不可用")
            }
            val format = formats.maxByOrNull { it.height() ?: 0 }
            val videoUrl = format?.url()

            VideoInfo(
                title = details.title().replace(" - YouTube", ""),
                url = url,
                platform = "YouTube",
                thumbnailUrl = details.thumbnails().firstOrNull(),
                duration = details.lengthSeconds().toLong(),
                videoUrl = videoUrl,
                qualities = formats.mapNotNull { f ->
                    val h = f.height()
                    val label = when {
                        h == null -> "标准"
                        h >= 1080 -> "超清"
                        h >= 720 -> "高清"
                        else -> "标清"
                    }
                    VideoQuality(label, f.url() ?: "", "${h ?: 360}P")
                }
            )
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
            qualities = listOf(
                VideoQuality("高清", videoUrl ?: url),
                VideoQuality("标清", videoUrl ?: url)
            )
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
            qualities = listOf(
                VideoQuality("高清", videoUrl ?: url),
                VideoQuality("标清", videoUrl ?: url)
            )
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
            qualities = listOf(
                VideoQuality("高清", videoUrl ?: url),
                VideoQuality("标清", videoUrl ?: url)
            )
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
            qualities = listOf(
                VideoQuality("高清", videoUrl ?: url),
                VideoQuality("标清", videoUrl ?: url)
            )
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

open class BaseParser : BaseVideoParser() {
    override suspend fun parse(url: String): VideoInfo {
        throw UnsupportedPlatformException("暂不支持该平台")
    }
}
