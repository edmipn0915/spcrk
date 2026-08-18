package com.spcrk.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge_documents")
data class KnowledgeDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val filePath: String = "",
    val fileType: String = "",
    val chunkCount: Int = 0,
    val categoryId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
