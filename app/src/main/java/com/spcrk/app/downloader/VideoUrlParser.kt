package com.spcrk.app.downloader

/**
 * 影片網址純邏輯解析工具（與網路無關，可單元測試）。
 * 集中處理各平台網址的 video id 提取與短連結判斷。
 */
object VideoUrlParser {

    /** 從 B 站網頁端連結提取 BV 號；若無（如 b23.tv 短連結）返回 null。 */
    fun extractBilibiliVideoId(url: String): String? {
        val regex = Regex("BV[A-Za-z0-9]+")
        return regex.find(url)?.value
    }

    /** 判斷是否為 b23.tv 短連結（手機 APP 分享的連結，不含 BV 號，需 302 展開）。 */
    fun needsBilibiliRedirect(url: String): Boolean {
        return url.contains("b23.tv", ignoreCase = true)
    }

    /** 從各種 YouTube 連結格式提取 video id；無法識別返回 null。 */
    fun extractYouTubeVideoId(url: String): String? {
        val regex = Regex("(?:v=|/v/|youtu\\.be/|/embed/|/shorts/)([\\w-]{11})")
        return regex.find(url)?.groupValues?.get(1)
    }
}
