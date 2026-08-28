package com.spcrk.app.ai.api

/**
 * Document 基本信息（与知识库无关，仅用于文件解析结果）。
 */
data class DocumentInfo(
    val uri: String,
    val name: String,
    val size: Long,
    val content: String,
    val uploadedAt: Long = System.currentTimeMillis()
)
