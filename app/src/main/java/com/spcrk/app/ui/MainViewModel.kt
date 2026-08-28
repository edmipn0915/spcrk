package com.spcrk.app.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spcrk.app.AppContainer
import com.spcrk.app.getAppContainer
import com.spcrk.app.data.DownloadHistory
import com.spcrk.app.downloader.VideoDownloader
import com.spcrk.app.model.DownloadState
import com.spcrk.app.model.VideoInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class MainUiState(
    val url: String = "",
    val isDownloading: Boolean = false,
    val videoInfo: VideoInfo? = null,
    val downloadProgress: Float = 0f,
    val errorMessage: String? = null,
    val downloadState: DownloadState = DownloadState.Idle,
    val downloadSpeed: String = "",
    val downloadedSize: String = "",
    val totalSize: String = "",
    val completedFilePath: String? = null,
    val completedFileSize: Long = 0L
)

class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val container: AppContainer = getAppContainer(app)
    private val videoDownloader = container.videoDownloader
    private val repository = container.repository
    private var downloadJob: Job? = null

    private var lastProgressTime = 0L
    private var lastProgressBytes = 0L

    fun updateUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            url = url,
            errorMessage = null
        )
    }

    fun startDownload() {
        var url = _uiState.value.url.trim()
        
        // Auto-prepend https:// if missing
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        
        if (url.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "请输入视频链接"
            )
            return
        }

        if (!isValidUrl(url)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "请输入有效的视频链接"
            )
            return
        }

        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloading = true,
                errorMessage = null,
                downloadProgress = 0f,
                downloadState = DownloadState.Parsing,
                downloadSpeed = "",
                downloadedSize = "",
                totalSize = "",
                completedFilePath = null,
                completedFileSize = 0L
            )

            lastProgressTime = System.currentTimeMillis()
            lastProgressBytes = 0L

            try {
                Log.d("VideoDownloader", "开始解析URL: $url")

                // Parse video info
                _uiState.value = _uiState.value.copy(
                    downloadState = DownloadState.Parsing
                )

                val videoInfo = videoDownloader.parseVideoUrl(url)
                Log.d("VideoDownloader", "解析结果: ${videoInfo.title}, URL: ${videoInfo.videoUrl}")

                // Check if video URL is available
                if (videoInfo.videoUrl.isNullOrEmpty()) {
                    Log.e("VideoDownloader", "无法获取视频下载地址")
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        downloadState = DownloadState.Error("无法获取视频下载地址"),
                        errorMessage = "无法获取视频下载地址，该视频可能需要特殊处理或尝试其他视频"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    videoInfo = videoInfo,
                    downloadState = DownloadState.Downloading
                )

                Log.d("VideoDownloader", "开始下载: ${videoInfo.videoUrl}")

                // Start download
                videoDownloader.downloadVideo(
                    context = getApplication(),
                    videoInfo = videoInfo,
                    onProgress = { downloadedBytes, totalBytes ->
                        val currentTime = System.currentTimeMillis()
                        val elapsed = currentTime - lastProgressTime
                        val progress = if (totalBytes > 0) {
                            (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        if (elapsed >= 500) {
                            val speedBps = ((downloadedBytes - lastProgressBytes) * 1000L) / elapsed

                            _uiState.value = _uiState.value.copy(
                                downloadProgress = progress,
                                downloadSpeed = formatSpeed(speedBps),
                                downloadedSize = formatSize(downloadedBytes),
                                totalSize = if (totalBytes > 0) formatSize(totalBytes) else "未知"
                            )

                            lastProgressTime = currentTime
                            lastProgressBytes = downloadedBytes
                        } else {
                            _uiState.value = _uiState.value.copy(
                                downloadProgress = progress,
                                downloadedSize = formatSize(downloadedBytes),
                                totalSize = if (totalBytes > 0) formatSize(totalBytes) else "未知"
                            )
                        }
                    },
                    onComplete = { filePath ->
                        Log.d("VideoDownloader", "下载完成: $filePath")
                        // content:// URI（Android 10+ MediaStore）不能 File() 檢查，改查 ContentResolver
                        val fileSize = if (com.spcrk.app.downloader.PlaybackUri.isContentUri(filePath)) {
                            try {
                                val resolver = getApplication<Application>().contentResolver
                                resolver.query(
                                    android.net.Uri.parse(filePath),
                                    arrayOf(android.provider.MediaStore.MediaColumns.SIZE),
                                    null, null, null
                                )?.use { c ->
                                    if (c.moveToFirst()) c.getLong(0) else 0L
                                } ?: 0L
                            } catch (e: Exception) {
                                0L
                            }
                        } else {
                            val f = java.io.File(filePath)
                            if (f.exists()) f.length() else 0L
                        }
                        val history = DownloadHistory(
                            title = videoInfo.title,
                            platform = videoInfo.platform,
                            filePath = filePath,
                            fileSize = fileSize,
                            downloadTime = System.currentTimeMillis(),
                            videoUrl = videoInfo.url
                        )
                        viewModelScope.launch {
                            repository.addHistory(history)
                        }
                        _uiState.value = _uiState.value.copy(
                            isDownloading = false,
                            downloadProgress = 1f,
                            downloadState = DownloadState.Completed,
                            errorMessage = null,
                            downloadSpeed = "",
                            downloadedSize = "完成",
                            completedFilePath = filePath,
                            completedFileSize = fileSize
                        )
                    },
                    onError = { error ->
                        Log.e("VideoDownloader", "下载错误: $error")
                        _uiState.value = _uiState.value.copy(
                            isDownloading = false,
                            downloadState = DownloadState.Error(error),
                            errorMessage = error,
                            downloadSpeed = ""
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("VideoDownloader", "解析错误", e)
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    downloadState = DownloadState.Error(e.message ?: "下载失败，请重试"),
                    errorMessage = "错误: ${e.message ?: "未知错误"}",
                    downloadSpeed = ""
                )
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isDownloading = false,
            downloadState = DownloadState.Idle,
            downloadSpeed = "",
            downloadedSize = "",
            totalSize = ""
        )
    }

    private fun isValidUrl(url: String): Boolean {
        val supportedPatterns = listOf(
            "bilibili.com",
            "b23.tv",
            "youtube.com",
            "youtu.be",
            "douyin.com",
            "iesdouyin.com",
            "kuaishou.com",
            "youku.com",
            "iqiyi.com",
            "vimeo.com",
            "dailymotion.com",
            "twitter.com",
            "x.com",
            "instagram.com",
            "tiktok.com",
            "weibo.com"
        )

        return supportedPatterns.any { url.contains(it, ignoreCase = true) }
    }

    private fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond < 1024 -> "$bytesPerSecond B/s"
            bytesPerSecond < 1024 * 1024 -> "${bytesPerSecond / 1024} KB/s"
            else -> "${String.format("%.1f", bytesPerSecond.toFloat() / (1024 * 1024))} MB/s"
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${String.format("%.1f", bytes.toFloat() / (1024 * 1024))} MB"
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        downloadJob?.cancel()
        _uiState.value = MainUiState()
    }
}
