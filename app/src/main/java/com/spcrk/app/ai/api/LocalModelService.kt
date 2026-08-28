package com.spcrk.app.ai.api

import android.net.Uri

interface LocalModelService {
    fun getAvailableEmbeddingModels(): List<EmbeddingModelInfo>
    fun getAvailableLLMModels(): List<EmbeddingModelInfo>
    fun getDownloadedModels(): List<LocalModelInfo>
    suspend fun downloadModel(
        url: String, fileName: String,
        onProgress: (Float) -> Unit,
        onComplete: (Boolean) -> Unit
    )
    fun deleteModel(fileName: String): Boolean
    fun importLocalFile(uri: Uri): Boolean
    fun isModelDownloaded(fileName: String): Boolean
}
