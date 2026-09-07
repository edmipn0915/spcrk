package com.spcrk.app.downloader

import android.content.ContentValues
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import android.media.MediaFormat
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.spcrk.app.model.Platform
import com.spcrk.app.model.VideoInfo
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
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
        private const val MERGE_BUFFER_SIZE = 1024 * 1024
    }

    suspend fun parseVideoUrl(url: String): VideoInfo {
        val platform = Platform.fromUrl(url)
        val parser = parsers[platform] ?: throw UnsupportedPlatformException("暂不支持该平台: ${platform.displayName}")

        return withContext(Dispatchers.IO) {
            parser.parse(url)
        }
    }

    /**
     * 下載影片。當 [audioUrl] 不為空時視為「純影像軌 + 純音軌」雙軌下載，
     * 下載完成後以 MediaMuxer 合併成單一 mp4 再發布。
     *
     * @param qualityTag 選中畫質的文字標記（如 1080P），會加進檔名方便辨識。
     */
    suspend fun downloadVideo(
        context: Context,
        videoInfo: VideoInfo,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit,
        audioUrl: String? = null,
        qualityTag: String? = null
    ) {
        withContext(Dispatchers.IO) {
            var tempFiles = mutableListOf<File>()
            try {
                // Check if video URL is available
                val downloadUrl = videoInfo.videoUrl
                    ?: throw DownloadException("无法获取视频下载地址，请尝试其他视频")

                if (downloadUrl.isEmpty()) {
                    throw DownloadException("视频下载地址为空")
                }

                val tempDir = File(context.cacheDir, "downloads").apply { mkdirs() }

                val cleanTitle = videoInfo.title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                val cleanTag = qualityTag?.replace(Regex("[\\\\/:*?\"<>|]"), "_")?.trim('_')
                    ?.let { "_$it" } ?: ""
                val fileName = "${cleanTitle}${cleanTag}_${System.currentTimeMillis()}.mp4"

                // 先下載到 app 快取目錄（可隨機讀寫、支援多執行緒），
                // 避免 Android 11+（targetSdk 30+）無法直接寫入公共 Download 目錄。
                val baseName = fileName.removeSuffix(".mp4")

                if (audioUrl.isNullOrBlank()) {
                    // —— 單一 URL（含音軌的 progressive / 單檔來源）——
                    val tempFile = File(tempDir, fileName)
                    val contentLength = getContentLengthSafe(downloadUrl, videoInfo.url)
                    downloadTrack(downloadUrl, videoInfo.url, tempFile, contentLength, 0L, contentLength, onProgress)

                    onProgress(contentLength.coerceAtLeast(0L), contentLength.coerceAtLeast(0L))
                    val publishedPath = publishVideo(context, tempFile, fileName)
                    onComplete(publishedPath)
                } else {
                    // —— 雙軌：影像 + 音軌分開下載，再合併 ——
                    val videoLen = getContentLengthSafe(downloadUrl, videoInfo.url)
                    val audioLen = getContentLengthSafe(audioUrl, videoInfo.url)
                    val known = videoLen > 0 && audioLen > 0
                    val reportTotal = if (known) videoLen + audioLen else -1L

                    val videoTemp = File(tempDir, "${baseName}_video.mp4")
                    val audioTemp = File(tempDir, "${baseName}_audio.m4a")
                    tempFiles = mutableListOf(videoTemp, audioTemp)

                    downloadTrack(downloadUrl, videoInfo.url, videoTemp, videoLen, 0L, reportTotal, onProgress)

                    if (known) {
                        onProgress(videoLen, reportTotal)
                        downloadTrack(audioUrl, videoInfo.url, audioTemp, audioLen, videoLen, reportTotal, onProgress)
                        onProgress(reportTotal, reportTotal)
                    } else {
                        // 長度未知時改以單執行緒下載音軌（不更新百分比）
                        downloadTrack(audioUrl, videoInfo.url, audioTemp, audioLen, 0L, -1L, onProgress)
                    }

                    val mergedFile = File(tempDir, "${baseName}_merged.mp4")
                    muxToMp4(videoTemp, audioTemp, mergedFile)
                    tempFiles.add(mergedFile)

                    val publishedPath = publishVideo(context, mergedFile, fileName)
                    onComplete(publishedPath)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError(e.message ?: "下载失败")
            } finally {
                tempFiles.forEach { it.delete() }
            }
        }
    }

    /**
     * 把快取的臨時檔發布到用戶可見目錄，回傳可播放的 uri / 路徑字串。
     */
    private fun publishVideo(context: Context, tempFile: File, fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            publishToMediaStore(context, tempFile, fileName)
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            val dest = File(dir, fileName)
            if (!tempFile.renameTo(dest)) tempFile.copyTo(dest, overwrite = true)
            tempFile.delete()
            dest.absolutePath
        }
    }

    private fun publishToMediaStore(context: Context, tempFile: File, fileName: String): String {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Sparck")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw DownloadException("无法创建下载文件")
        try {
            resolver.openOutputStream(uri)?.use { out ->
                tempFile.inputStream().use { it.copyTo(out) }
            } ?: throw DownloadException("无法写入下载文件")
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
        tempFile.delete()
        return uri.toString()
    }

    private fun getContentLengthSafe(url: String, referer: String): Long {
        return try {
            getContentLength(url, referer)
        } catch (_: Exception) {
            -1L
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

    /**
     * 下載單一軌道到 [outputFile]。
     *
     * @param trackLength 此軌總長（未知可傳 <=0，會改用單執行緒）
     * @param offsetBytes 此軌開始前已回報的位元組數（供雙軌合併時計算總進度）
     * @param reportTotal 進度回呼使用的總量（未知傳 -1）
     */
    private suspend fun downloadTrack(
        url: String,
        referer: String,
        outputFile: File,
        trackLength: Long,
        offsetBytes: Long,
        reportTotal: Long,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ) {
        if (trackLength > 1024 * 1024) {
            multiThreadDownload(url, referer, outputFile, trackLength, offsetBytes, reportTotal, onProgress)
        } else {
            singleThreadDownload(url, referer, outputFile, offsetBytes, reportTotal, onProgress)
        }
    }

    private suspend fun singleThreadDownload(
        url: String,
        referer: String,
        outputFile: File,
        offsetBytes: Long,
        reportTotal: Long,
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

            val total = if (reportTotal > 0) reportTotal else {
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
                            onProgress(offsetBytes + totalBytesRead, total)
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
        offsetBytes: Long,
        reportTotal: Long,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val chunkSize = contentLength / THREAD_COUNT
            val tempFiles = mutableListOf<File>()
            val downloadedBytes = AtomicLong(offsetBytes)
            val total = if (reportTotal > 0) reportTotal else contentLength

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

                        downloadChunk(url, referer, tempFiles[threadIndex], start, end, downloadedBytes, total, onProgress)
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
        downloadedBytes: AtomicLong,
        reportTotal: Long,
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
                        if (reportTotal > 0) {
                            onProgress(total, reportTotal)
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

    /** 以 MediaMuxer 把「純影像檔 + 純音軌檔」合併成單一 mp4。 */
    private fun muxToMp4(videoFile: File, audioFile: File, outputFile: File) {
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val videoExtractor = MediaExtractor()
        val audioExtractor = MediaExtractor()
        try {
            videoExtractor.setDataSource(videoFile.absolutePath)
            audioExtractor.setDataSource(audioFile.absolutePath)

            var videoTrack = -1
            var audioTrack = -1
            var videoIndex = -1
            var audioIndex = -1

            for (i in 0 until videoExtractor.trackCount) {
                val fmt = videoExtractor.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("video/") && videoTrack < 0) {
                    videoTrack = muxer.addTrack(fmt)
                    videoIndex = i
                }
            }
            for (i in 0 until audioExtractor.trackCount) {
                val fmt = audioExtractor.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/") && audioTrack < 0) {
                    audioTrack = muxer.addTrack(fmt)
                    audioIndex = i
                }
            }

            if (videoTrack < 0 || audioTrack < 0) {
                throw DownloadException("合併失敗：找不到可用的影/音軌")
            }

            videoExtractor.selectTrack(videoIndex)
            audioExtractor.selectTrack(audioIndex)
            muxer.start()

            val videoBuf = ByteBuffer.allocateDirect(MERGE_BUFFER_SIZE)
            val audioBuf = ByteBuffer.allocateDirect(MERGE_BUFFER_SIZE)
            val videoInfo = MediaCodec.BufferInfo()
            val audioInfo = MediaCodec.BufferInfo()
            videoInfo.size = -1
            audioInfo.size = -1
            var videoEof = false
            var audioEof = false
            var videoSamples = 0
            var audioSamples = 0

            while (!videoEof || !audioEof) {
                if (!videoEof && videoInfo.size < 0) {
                    videoInfo.size = videoExtractor.readSampleData(videoBuf, 0)
                    if (videoInfo.size < 0) {
                        videoEof = true
                    } else {
                        videoInfo.presentationTimeUs = videoExtractor.sampleTime
                        videoInfo.flags = videoExtractor.sampleFlags
                    }
                }
                if (!audioEof && audioInfo.size < 0) {
                    audioInfo.size = audioExtractor.readSampleData(audioBuf, 0)
                    if (audioInfo.size < 0) {
                        audioEof = true
                    } else {
                        audioInfo.presentationTimeUs = audioExtractor.sampleTime
                        audioInfo.flags = audioExtractor.sampleFlags
                    }
                }
                if (videoEof && audioEof) break

                val writeVideo = when {
                    videoEof -> false
                    audioEof -> true
                    else -> videoInfo.presentationTimeUs <= audioInfo.presentationTimeUs
                }

                if (writeVideo) {
                    muxer.writeSampleData(videoTrack, videoBuf, videoInfo)
                    videoSamples++
                    videoInfo.size = -1
                } else {
                    muxer.writeSampleData(audioTrack, audioBuf, audioInfo)
                    audioSamples++
                    audioInfo.size = -1
                }
            }

            if (videoSamples == 0 || audioSamples == 0) {
                throw DownloadException("合併失敗：影像或音軌內容為空")
            }

            muxer.stop()
        } finally {
            try {
                videoExtractor.release()
            } catch (_: Exception) {
            }
            try {
                audioExtractor.release()
            } catch (_: Exception) {
            }
            try {
                muxer.release()
            } catch (_: Exception) {
            }
        }
    }
}

class UnsupportedPlatformException(message: String) : Exception(message)
class DownloadException(message: String) : Exception(message)
