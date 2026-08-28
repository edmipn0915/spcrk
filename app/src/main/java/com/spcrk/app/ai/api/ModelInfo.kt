package com.spcrk.app.ai.api

import android.net.Uri

/** 可用的嵌入/LLM 模型信息。移至 api 层以避免 LocalModelService 反向依赖 LocalModelManager。 */
data class EmbeddingModelInfo(
    val id: String,
    val name: String,
    val sizeMB: Int,
    val isRecommended: Boolean = false,
    val downloadUrl: String,
    val source: String = "huggingface"
)

/** 已下载的本地模型信息。移至 api 层以避免 LocalModelService 反向依赖 LocalModelManager。 */
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
