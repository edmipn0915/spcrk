package com.spcrk.app.downloader

import org.json.JSONArray
import org.json.JSONObject

/**
 * 單一含音軌的 YouTube 格式（progressive，可直接下載播放）。
 */
data class YouTubeFormat(
    val itag: Int,
    val mimeType: String,
    val qualityLabel: String?,
    val height: Int?,
    val url: String
)

/**
 * InnerTube player 回應解析結果。
 *
 * [progressiveFormats]：含音軌的單一格式（formats），可直接下載，但通常只有 360P。
 * [adaptiveVideoFormats]：純影像格式（adaptiveFormats，無聲音），高畫質需搭配 [adaptiveAudioFormats] 合併。
 * [adaptiveAudioFormats]：純音軌格式。
 */
data class YouTubePlayerResult(
    val title: String,
    val thumbnailUrl: String?,
    val duration: Long,
    val progressiveFormats: List<YouTubeFormat>,
    val adaptiveVideoFormats: List<YouTubeFormat> = emptyList(),
    val adaptiveAudioFormats: List<YouTubeFormat> = emptyList()
)

/**
 * YouTube InnerTube 解析器：直接以 ANDROID client 呼叫 youtubei/v1/player。
 *
 * 背景：java-youtube-downloader 使用過舊的 ANDROID client 版本（19.x），2025 起被
 * YouTube bot-check 攔截（回應不含 streamingData）。改用現役 app 版本號後，
 * YouTube 回傳直連 url（含 signatureCipher 的格式一律略過，需 JS 解密不可靠）。
 *
 * clientVersion 需隨 YouTube app 版本更新而調整（APKMirror 可查）。
 */
object YouTubeInnerTube {
    /** 現役 YouTube Android app 版本號（2026-08，APKMirror 查得 21.34.243）。 */
    const val CLIENT_VERSION = "21.34.243"
    /** YouTube Android app 專用 API key。 */
    const val ANDROID_API_KEY = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w"
    const val PLAYER_URL = "https://www.youtube.com/youtubei/v1/player?key=$ANDROID_API_KEY"

    /** 建構 ANDROID client 的 player 請求 body。 */
    fun buildRequestBody(videoId: String, clientVersion: String = CLIENT_VERSION): String {
        val client = JSONObject()
        client.put("clientName", "ANDROID")
        client.put("clientVersion", clientVersion)
        client.put("androidSdkVersion", 30)
        client.put("osName", "Android")
        client.put("osVersion", "12")
        client.put("hl", "en")
        client.put("gl", "US")
        client.put("timeZone", "UTC")
        client.put("utcOffsetMinutes", 0)
        client.put("clientScreen", "WATCH")
        client.put("userAgent", "com.google.android.youtube/$clientVersion (Linux; U; Android 12) gzip")

        val context = JSONObject().put("client", client)
        return JSONObject()
            .put("context", context)
            .put("videoId", videoId)
            .put("contentCheckOk", true)
            .put("racyCheckOk", true)
            .put("androidId", "4d9d2b8f6a3c0e71")
            .toString()
    }

    /**
     * 解析 player 回應。不可播放或缺 streamingData 時拋例外；
     * 只保留含直連 url 的 progressive 格式（signatureCipher 需 JS 解密，略過）。
     */
    fun parsePlayerResponse(json: String): YouTubePlayerResult {
        val root = JSONObject(json)

        val playability = root.optJSONObject("playabilityStatus")
        val status = playability?.optString("status") ?: "OK"
        if (status != "OK") {
            val reason = playability?.optString("reason")
                ?: playability?.optJSONObject("errorScreen")
                    ?.optJSONObject("playerErrorMessageRenderer")?.optString("reason")
                ?: status
            throw Exception("视频不可播放: $reason")
        }

        val streamingData = root.optJSONObject("streamingData")
            ?: throw Exception("streamingData not found")

        val videoDetails = root.optJSONObject("videoDetails")
        val title = videoDetails?.optString("title") ?: "未知标题"
        val thumbnailUrl = videoDetails?.optJSONObject("thumbnail")
            ?.optJSONArray("thumbnails")?.optJSONObject(0)?.optString("url")
        val duration = videoDetails?.optLong("lengthSeconds", 0L) ?: 0L

        val formats = mutableListOf<YouTubeFormat>()
        val jsonFormats: JSONArray = streamingData.optJSONArray("formats") ?: JSONArray()
        for (i in 0 until jsonFormats.length()) {
            val f = jsonFormats.optJSONObject(i) ?: continue
            // 只取直連 url；signatureCipher 格式需要 JS 解密，跳過
            if (f.has("signatureCipher")) continue
            val url = f.optString("url")
            if (url.isEmpty()) continue
            val height = f.optInt("height", 0).takeIf { it > 0 }
            formats.add(
                YouTubeFormat(
                    itag = f.optInt("itag", 0),
                    mimeType = f.optString("mimeType"),
                    qualityLabel = f.optString("qualityLabel").ifEmpty { null },
                    height = height,
                    url = url
                )
            )
        }

        // adaptiveFormats：純影像 / 純音軌（只保留直連 url）
        val adaptiveVideos = mutableListOf<YouTubeFormat>()
        val adaptiveAudios = mutableListOf<YouTubeFormat>()
        val jsonAdaptive: JSONArray = streamingData.optJSONArray("adaptiveFormats") ?: JSONArray()
        for (i in 0 until jsonAdaptive.length()) {
            val f = jsonAdaptive.optJSONObject(i) ?: continue
            if (f.has("signatureCipher")) continue
            val url = f.optString("url")
            if (url.isEmpty()) continue
            val mime = f.optString("mimeType")
            val height = f.optInt("height", 0).takeIf { it > 0 }
            val fmt = YouTubeFormat(
                itag = f.optInt("itag", 0),
                mimeType = mime,
                qualityLabel = f.optString("qualityLabel").ifEmpty { null },
                height = height,
                url = url
            )
            if (mime.startsWith("video/")) adaptiveVideos.add(fmt)
            else if (mime.startsWith("audio/")) adaptiveAudios.add(fmt)
        }

        if (formats.isEmpty() && adaptiveVideos.isEmpty()) {
            throw Exception("无法获取YouTube下载地址，视频可能受版权保护或不可用")
        }
        return YouTubePlayerResult(title, thumbnailUrl, duration, formats, adaptiveVideos, adaptiveAudios)
    }

    /** 挑選畫質最高的含音軌格式。 */
    fun pickBestProgressive(formats: List<YouTubeFormat>): YouTubeFormat? =
        formats.maxByOrNull { it.height ?: 0 }
}
