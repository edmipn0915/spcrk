package com.spcrk.app.downloader

import android.os.Environment
import com.spcrk.app.model.Platform
import com.spcrk.app.model.VideoInfo
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class VideoDownloader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val parsers = mapOf(
        Platform.BILIBILI to BilibiliParser(),
        Platform.BILIBILI_SHORT to BilibiliParser(),
        Platform.YOUTUBE to YouTubeParser(),
        Platform.YOUTUBE_SHORT to YouTubeParser(),
        Platform.DOUYIN to DouyinParser(),
        Platform.KUAISHOU to KuaishouParser(),
        Platform.YOUKU to YoukuParser(),
        Platform.WEIBO to WeiboParser()
    )

    companion object {
        private const val THREAD_COUNT = 4
        private const val BUFFER_SIZE = 8192
    }

    suspend fun parseVideoUrl(url: String): VideoInfo {
        val platform = Platform.fromUrl(url)
        val parser = parsers[platform] ?: throw UnsupportedPlatformException("暂不支持该平台: ${platform.displayName}")

        return withContext(Dispatchers.IO) {
            parser.parse(url)
        }
    }

    suspend fun downloadVideo(
        videoInfo: VideoInfo,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Check if video URL is available
                val downloadUrl = videoInfo.videoUrl
                    ?: throw DownloadException("无法获取视频下载地址，请尝试其他视频")

                if (downloadUrl.isEmpty()) {
                    throw DownloadException("视频下载地址为空")
                }

                val fileName = "${videoInfo.title.replace(Regex("[\\\\/:*?\"<>|]"), "_")}_${System.currentTimeMillis()}.mp4"
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                // Ensure directory exists
                if (!downloadDir.exists()) {
                    val created = downloadDir.mkdirs()
                    if (!created) {
                        throw DownloadException("无法创建下载目录")
                    }
                }

                val outputFile = File(downloadDir, fileName)

                // Get file size
                val contentLength = getContentLength(downloadUrl, videoInfo.url)

                if (contentLength > 1024 * 1024) {
                    // Multi-thread download for large files
                    multiThreadDownload(downloadUrl, videoInfo.url, outputFile, contentLength, onProgress)
                } else {
                    // Single thread download for small files
                    singleThreadDownload(downloadUrl, videoInfo.url, outputFile, contentLength, onProgress)
                }

                onProgress(contentLength.coerceAtLeast(0L), contentLength.coerceAtLeast(0L))
                onComplete(outputFile.absolutePath)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError(e.message ?: "下载失败")
            }
        }
    }

    private fun getContentLength(url: String, referer: String): Long {
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .addHeader("Referer", referer)
            .head()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw DownloadException("获取文件信息失败: ${response.code}")
        }
        return response.body?.contentLength() ?: -1L
    }

    private suspend fun singleThreadDownload(
        url: String,
        referer: String,
        outputFile: File,
        contentLength: Long,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", referer)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw DownloadException("下载失败: ${response.code}")
            }

            val body = response.body ?: throw DownloadException("下载失败: 空响应")

            val total = if (contentLength > 0) contentLength else {
                body.contentLength().coerceAtLeast(0L)
            }

            body.byteStream().use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var totalBytesRead = 0L
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (total > 0) {
                            onProgress(totalBytesRead, total)
                        }
                    }
                    outputStream.flush()
                }
            }
        }
    }

    private suspend fun multiThreadDownload(
        url: String,
        referer: String,
        outputFile: File,
        contentLength: Long,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val chunkSize = contentLength / THREAD_COUNT
            val tempFiles = mutableListOf<File>()
            val downloadedBytes = AtomicLong(0)

            for (i in 0 until THREAD_COUNT) {
                val tempFile = File(outputFile.parent, "${outputFile.name}.part$i")
                tempFiles.add(tempFile)
            }

            try {
                val jobs = (0 until THREAD_COUNT).map { threadIndex ->
                    async {
                        val start = threadIndex * chunkSize
                        val end = if (threadIndex == THREAD_COUNT - 1) {
                            contentLength - 1
                        } else {
                            start + chunkSize - 1
                        }

                        downloadChunk(url, referer, tempFiles[threadIndex], start, end, contentLength, downloadedBytes, onProgress)
                    }
                }

                jobs.awaitAll()
                mergeFiles(outputFile, tempFiles)

            } catch (e: Exception) {
                tempFiles.forEach { it.delete() }
                throw e
            }
        }
    }

    private suspend fun downloadChunk(
        url: String,
        referer: String,
        tempFile: File,
        start: Long,
        end: Long,
        totalLength: Long,
        downloadedBytes: AtomicLong,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", referer)
                .addHeader("Range", "bytes=$start-$end")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful && response.code != 206) {
                throw DownloadException("分块下载失败: ${response.code}")
            }

            val body = response.body ?: throw DownloadException("分块下载失败: 空响应")

            body.byteStream().use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        val total = downloadedBytes.addAndGet(bytesRead.toLong())
                        if (totalLength > 0) {
                            onProgress(total, totalLength)
                        }
                    }
                    outputStream.flush()
                }
            }
        }
    }

    private fun mergeFiles(outputFile: File, tempFiles: List<File>) {
        RandomAccessFile(outputFile, "rw").use { raf ->
            tempFiles.forEach { tempFile ->
                tempFile.inputStream().use { inputStream ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        raf.write(buffer, 0, bytesRead)
                    }
                }
                tempFile.delete()
            }
        }
    }
}

class UnsupportedPlatformException(message: String) : Exception(message)
class DownloadException(message: String) : Exception(message)
