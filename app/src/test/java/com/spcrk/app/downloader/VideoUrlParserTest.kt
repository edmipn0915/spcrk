package com.spcrk.app.downloader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * VideoUrlParser 純邏輯測試：
 * - B站網頁端連結（bilibili.com/video/BV...）能提取 BV 號
 * - b23.tv 短連結（手機 APP 分享）不含 BV 號，需標記為「需展開」
 * - YouTube 各種連結格式能提取 video id
 */
class VideoUrlParserTest {

    @Test
    fun `bilibili web page link extracts BV id`() {
        assertEquals(
            "BV1GJ411x7h7",
            VideoUrlParser.extractBilibiliVideoId("https://www.bilibili.com/video/BV1GJ411x7h7/")
        )
    }

    @Test
    fun `bilibili link with query params extracts BV id`() {
        assertEquals(
            "BV1xx411c7mD",
            VideoUrlParser.extractBilibiliVideoId("https://www.bilibili.com/video/BV1xx411c7mD?spm_id_from=333.337")
        )
    }

    @Test
    fun `b23 dot tv short link has no BV id`() {
        assertNull(
            VideoUrlParser.extractBilibiliVideoId("https://b23.tv/AbCdEf123")
        )
    }

    @Test
    fun `b23 dot tv short link is marked as needing redirect`() {
        assertTrue(
            VideoUrlParser.needsBilibiliRedirect("https://b23.tv/AbCdEf123")
        )
    }

    @Test
    fun `bilibili web page link does not need redirect`() {
        assertFalse(
            VideoUrlParser.needsBilibiliRedirect("https://www.bilibili.com/video/BV1GJ411x7h7/")
        )
    }

    @Test
    fun `youtube watch link extracts video id`() {
        assertEquals(
            "dQw4w9WgXcQ",
            VideoUrlParser.extractYouTubeVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        )
    }

    @Test
    fun `youtu dot be link extracts video id`() {
        assertEquals(
            "dQw4w9WgXcQ",
            VideoUrlParser.extractYouTubeVideoId("https://youtu.be/dQw4w9WgXcQ")
        )
    }

    @Test
    fun `youtube shorts link extracts video id`() {
        assertEquals(
            "dQw4w9WgXcQ",
            VideoUrlParser.extractYouTubeVideoId("https://www.youtube.com/shorts/dQw4w9WgXcQ")
        )
    }

    @Test
    fun `youtube embed link extracts video id`() {
        assertEquals(
            "dQw4w9WgXcQ",
            VideoUrlParser.extractYouTubeVideoId("https://www.youtube.com/embed/dQw4w9WgXcQ")
        )
    }

    @Test
    fun `invalid youtube url returns null`() {
        assertNull(
            VideoUrlParser.extractYouTubeVideoId("https://example.com/not-youtube")
        )
    }
}
