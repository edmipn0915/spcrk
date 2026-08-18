package com.spcrk.app.ai

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

data class EmbeddingModelInfo(
    val id: String,
    val name: String,
    val sizeMB: Int,
    val isRecommended: Boolean = false,
    val downloadUrl: String,
    val source: String = "huggingface"
)

data class LocalModelInfo(
    val id: String,
    val name: String,
    val type: String,
    val sizeMB: Int,
    val filePath: String,
    val status: String,
    val progress: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

class LocalModelManager(private val context: Context) {

    val modelsDir: File get() = File(context.filesDir, "local_models")

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    init {
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
    }

    fun getAvailableEmbeddingModels(): List<EmbeddingModelInfo> = listOf(
        EmbeddingModelInfo(
            id = "qwen-embedding-0.6B",
            name = "Qwen3 Embedding 0.6B",
            sizeMB = 1200,
            isRecommended = true,
            downloadUrl = "https://huggingface.co/Qwen/Qwen3-Embedding-0.6B/resolve/main/Qwen3-Embedding-0.6B.gguf",
            source = "huggingface"
        ),
        EmbeddingModelInfo(
            id = "text2vec-base-chinese",
            name = "Text2Vec Base Chinese",
            sizeMB = 400,
            isRecommended = false,
            downloadUrl = "https://huggingface.co/GanymedeNil/text2vec-base-chinese/resolve/main/text2vec-base-chinese.gguf",
            source = "huggingface"
        ),
        EmbeddingModelInfo(
            id = "bge-small-zh-v1.5",
            name = "BGE Small ZH v1.5",
            sizeMB = 90,
            isRecommended = false,
            downloadUrl = "https://huggingface.co/BAAI/bge-small-zh-v1.5/resolve/main/bge-small-zh-v1.5.gguf",
            source = "huggingface"
        )
    )

    fun getAvailableLLMModels(): List<EmbeddingModelInfo> = listOf(
        EmbeddingModelInfo(
            id = "qwen2.5-1.5b-instruct",
            name = "Qwen2.5 1.5B Instruct",
            sizeMB = 1500,
            isRecommended = true,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            source = "huggingface"
        ),
        EmbeddingModelInfo(
            id = "qwen2.5-3b-instruct",
            name = "Qwen2.5 3B Instruct",
            sizeMB = 3000,
            isRecommended = false,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf",
            source = "huggingface"
        ),
        EmbeddingModelInfo(
            id = "llama-3-8b-instruct",
            name = "Llama 3 8B Instruct",
            sizeMB = 8000,
            isRecommended = false,
            downloadUrl = "https://huggingface.co/QuantFactory/Meta-Llama-3-8B-Instruct-GGUF/resolve/main/Meta-Llama-3-8B-Instruct.Q4_K_M.gguf",
            source = "huggingface"
        )
    )

    fun getDownloadedModels(): List<LocalModelInfo> {
        if (!modelsDir.exists()) return emptyList()

        val embeddingModels = getAvailableEmbeddingModels().associateBy { it.id }
        val llmModels = getAvailableLLMModels().associateBy { it.id }

        return modelsDir.listFiles()?.mapNotNull { file ->
            if (file.isFile && file.extension == "gguf") {
                val modelId = file.nameWithoutExtension
                val embeddingModel = embeddingModels[modelId]
                val llmModel = llmModels[modelId]

                when {
                    embeddingModel != null -> LocalModelInfo(
                        id = modelId,
                        name = embeddingModel.name,
                        type = "embedding",
                        sizeMB = embeddingModel.sizeMB,
                        filePath = file.absolutePath,
                        status = "ready",
                        createdAt = file.lastModified()
                    )
                    llmModel != null -> LocalModelInfo(
                        id = modelId,
                        name = llmModel.name,
                        type = "llm",
                        sizeMB = llmModel.sizeMB,
                        filePath = file.absolutePath,
                        status = "ready",
                        createdAt = file.lastModified()
                    )
                    else -> LocalModelInfo(
                        id = modelId,
                        name = file.name,
                        type = "unknown",
                        sizeMB = (file.length() / (1024 * 1024)).toInt(),
                        filePath = file.absolutePath,
                        status = "ready",
                        createdAt = file.lastModified()
                    )
                }
            } else null
        } ?: emptyList()
    }

    suspend fun downloadModel(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit = {},
        onComplete: (Boolean) -> Unit = {}
    ) {
        withContext(Dispatchers.IO) {
            try {
                val targetFile = File(modelsDir, fileName)
                val tempFile = File(modelsDir, "$fileName.tmp")

                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    onComplete(false)
                    return@withContext
                }

                val body = response.body ?: run {
                    onComplete(false)
                    return@withContext
                }

                val contentLength = body.contentLength()
                var bytesRead = 0L

                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(tempFile)

                val buffer = ByteArray(8192)
                var read: Int

                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                    bytesRead += read
                    if (contentLength > 0) {
                        val progress = bytesRead.toFloat() / contentLength.toFloat()
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                        }
                    }
                }

                outputStream.close()
                inputStream.close()

                if (tempFile.renameTo(targetFile)) {
                    onComplete(true)
                } else {
                    tempFile.delete()
                    onComplete(false)
                }
            } catch (e: Exception) {
                val tempFile = File(modelsDir, "$fileName.tmp")
                if (tempFile.exists()) {
                    tempFile.delete()
                }
                onComplete(false)
            }
        }
    }

    fun importLocalFile(uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val fileName = getFileNameFromUri(uri) ?: "imported_model.gguf"
            val targetFile = File(modelsDir, fileName)

            val outputStream = FileOutputStream(targetFile)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()

            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        if (fileName == null) {
            fileName = uri.lastPathSegment
        }
        return fileName
    }

    fun deleteModel(fileName: String): Boolean {
        return try {
            val file = File(modelsDir, fileName)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getModelPath(fileName: String): String {
        return File(modelsDir, fileName).absolutePath
    }

    fun isModelDownloaded(fileName: String): Boolean {
        return File(modelsDir, fileName).exists()
    }
}
