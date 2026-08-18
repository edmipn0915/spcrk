package com.spcrk.app.model

data class VideoInfo(
    val title: String,
    val url: String,
    val platform: String,
    val thumbnailUrl: String? = null,
    val duration: Long = 0L,
    val videoUrl: String? = null,
    val qualities: List<VideoQuality> = emptyList()
)

data class VideoQuality(
    val label: String,
    val url: String,
    val resolution: String? = null,
    val fileSize: Long = 0L
)

enum class Platform(val displayName: String, val domain: String) {
    BILIBILI("B站", "bilibili.com"),
    BILIBILI_SHORT("B站短链", "b23.tv"),
    YOUTUBE("YouTube", "youtube.com"),
    YOUTUBE_SHORT("YouTube短链", "youtu.be"),
    DOUYIN("抖音", "douyin.com"),
    KUAISHOU("快手", "kuaishou.com"),
    YOUKU("优酷", "youku.com"),
    IQIYI("爱奇艺", "iqiyi.com"),
    VIMEO("Vimeo", "vimeo.com"),
    DAILYMOTION("Dailymotion", "dailymotion.com"),
    TWITTER("Twitter/X", "twitter.com"),
    INSTAGRAM("Instagram", "instagram.com"),
    TIKTOK("TikTok", "tiktok.com"),
    WEIBO("微博", "weibo.com"),
    UNKNOWN("未知平台", "");

    companion object {
        fun fromUrl(url: String): Platform {
            return when {
                url.contains("bilibili.com", ignoreCase = true) -> BILIBILI
                url.contains("b23.tv", ignoreCase = true) -> BILIBILI_SHORT
                url.contains("youtube.com", ignoreCase = true) -> YOUTUBE
                url.contains("youtu.be", ignoreCase = true) -> YOUTUBE_SHORT
                url.contains("douyin.com", ignoreCase = true) -> DOUYIN
                url.contains("iesdouyin.com", ignoreCase = true) -> DOUYIN
                url.contains("kuaishou.com", ignoreCase = true) -> KUAISHOU
                url.contains("youku.com", ignoreCase = true) -> YOUKU
                url.contains("iqiyi.com", ignoreCase = true) -> IQIYI
                url.contains("vimeo.com", ignoreCase = true) -> VIMEO
                url.contains("dailymotion.com", ignoreCase = true) -> DAILYMOTION
                url.contains("twitter.com", ignoreCase = true) -> TWITTER
                url.contains("x.com", ignoreCase = true) -> TWITTER
                url.contains("instagram.com", ignoreCase = true) -> INSTAGRAM
                url.contains("tiktok.com", ignoreCase = true) -> TIKTOK
                url.contains("weibo.com", ignoreCase = true) -> WEIBO
                else -> UNKNOWN
            }
        }
    }
}

sealed class DownloadState {
    object Idle : DownloadState()
    object Parsing : DownloadState()
    object Downloading : DownloadState()
    object Completed : DownloadState()
    data class Error(val message: String) : DownloadState()
}
