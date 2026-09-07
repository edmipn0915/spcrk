package com.spcrk.app.downloader

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class YouTubeInnerTubeTest {

    @Test
    fun `buildRequestBody produces android client with current version`() {
        val body = JSONObject(YouTubeInnerTube.buildRequestBody("dQw4w9WgXcQ"))
        val client = body.getJSONObject("context").getJSONObject("client")
        assertEquals("ANDROID", client.getString("clientName"))
        assertEquals(YouTubeInnerTube.CLIENT_VERSION, client.getString("clientVersion"))
        assertEquals(30, client.getInt("androidSdkVersion"))
        assertEquals("dQw4w9WgXcQ", body.getString("videoId"))
    }

    @Test
    fun `parsePlayerResponse extracts title duration and progressive format url`() {
        val json = """
            {
              "playabilityStatus": {"status": "OK"},
              "videoDetails": {
                "title": "Rick Astley - Never Gonna Give You Up",
                "lengthSeconds": "212",
                "thumbnail": {"thumbnails": [{"url": "https://i.ytimg.com/vi/x/frame0.jpg"}]}
              },
              "streamingData": {
                "formats": [
                  {"itag": 18, "mimeType": "video/mp4; codecs=\"avc1.42001E, mp4a.40.2\"", "qualityLabel": "360p", "height": 360, "url": "https://rr1.googlevideo.com/videoplayback?x=1"}
                ]
              }
            }
        """.trimIndent()
        val result = YouTubeInnerTube.parsePlayerResponse(json)
        assertEquals("Rick Astley - Never Gonna Give You Up", result.title)
        assertEquals(212L, result.duration)
        assertEquals("https://i.ytimg.com/vi/x/frame0.jpg", result.thumbnailUrl)
        assertEquals(1, result.progressiveFormats.size)
        assertEquals("https://rr1.googlevideo.com/videoplayback?x=1", result.progressiveFormats[0].url)
        assertEquals(360, result.progressiveFormats[0].height)
    }

    @Test
    fun `parsePlayerResponse throws on unplayable video`() {
        val json = """
            {
              "playabilityStatus": {"status": "ERROR", "reason": "Video unavailable"},
              "videoDetails": {"title": "x", "lengthSeconds": "0"}
            }
        """.trimIndent()
        try {
            YouTubeInnerTube.parsePlayerResponse(json)
            fail("should throw on unplayable")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("不可播放"))
        }
    }

    @Test
    fun `parsePlayerResponse throws when streamingData missing`() {
        val json = """{"playabilityStatus": {"status": "OK"}, "videoDetails": {"title": "x"}}"""
        try {
            YouTubeInnerTube.parsePlayerResponse(json)
            fail("should throw when streamingData missing")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("streamingData"))
        }
    }

    @Test
    fun `pickBestProgressive chooses highest height`() {
        val low = YouTubeFormat(18, "video/mp4", "360p", 360, "http://a")
        val high = YouTubeFormat(22, "video/mp4", "720p", 720, "http://b")
        assertEquals(high, YouTubeInnerTube.pickBestProgressive(listOf(low, high)))
        assertNull(YouTubeInnerTube.pickBestProgressive(emptyList()))
    }

    @Test
    fun `parsePlayerResponse skips signatureCipher formats`() {
        val json = """
            {
              "playabilityStatus": {"status": "OK"},
              "videoDetails": {"title": "t", "lengthSeconds": "10"},
              "streamingData": {
                "formats": [
                  {"itag": 18, "mimeType": "video/mp4", "height": 360, "signatureCipher": "s=abc&url=https%3A%2F%2Fexample.com%2Fv"},
                  {"itag": 22, "mimeType": "video/mp4", "height": 720, "url": "https://rr1.googlevideo.com/videoplayback?y=2"}
                ]
              }
            }
        """.trimIndent()
        val result = YouTubeInnerTube.parsePlayerResponse(json)
        assertEquals(1, result.progressiveFormats.size)
        assertEquals(22, result.progressiveFormats[0].itag)
    }
}
