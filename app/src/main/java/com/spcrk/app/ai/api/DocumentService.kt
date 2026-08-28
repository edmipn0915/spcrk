package com.spcrk.app.ai.api

import android.net.Uri
import com.spcrk.app.data.KnowledgeDocument
import kotlinx.coroutines.flow.Flow

interface DocumentService {
    suspend fun loadDocument(uri: Uri): DocumentInfo
    suspend fun saveToKnowledgeBase(document: DocumentInfo): Long
    fun getKnowledgeDocuments(): Flow<List<KnowledgeDocument>>
    fun searchKnowledgeDocuments(query: String): Flow<List<KnowledgeDocument>>
    suspend fun removeKnowledgeDocument(id: Long)
    suspend fun clearKnowledgeBase()
}
