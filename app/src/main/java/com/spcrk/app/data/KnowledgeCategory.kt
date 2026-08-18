package com.spcrk.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge_categories")
data class KnowledgeCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "rag",
    val embeddingModel: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
