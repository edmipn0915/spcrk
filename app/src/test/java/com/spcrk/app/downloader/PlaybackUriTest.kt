package com.spcrk.app.downloader

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PlaybackUri 純邏輯測試。
 * 覆蓋 Android 10+ MediaStore 發布後，filePath 欄位可能存放 content:// URI
 * 而下游卻以 File() 操作造成「文件不存在」的契約破壞。
 */
class PlaybackUriTest {

    @Test
    fun `media store uri is detected as content uri`() {
        assertTrue(
            PlaybackUri.isContentUri("content://media/external/downloads/123")
        )
    }

    @Test
    fun `legacy absolute path is not a content uri`() {
        assertFalse(
            PlaybackUri.isContentUri("/storage/emulated/0/Download/video.mp4")
        )
    }

    @Test
    fun `empty path is not a content uri`() {
        assertFalse(PlaybackUri.isContentUri(""))
    }
}
