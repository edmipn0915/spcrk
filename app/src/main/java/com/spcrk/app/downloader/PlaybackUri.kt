package com.spcrk.app.downloader

/**
 * 下載產物識別：filePath 欄位可能是絕對路徑（Android 9-）或 content:// URI（Android 10+ MediaStore）。
 * 下游一律用 File(path) 操作會讓 content:// URI 永遠「不存在」，此處集中判斷避免契約破壞。
 */
object PlaybackUri {
    fun isContentUri(path: String): Boolean =
        path.startsWith("content://", ignoreCase = true)
}
