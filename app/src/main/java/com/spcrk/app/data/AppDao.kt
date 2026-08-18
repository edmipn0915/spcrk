package com.spcrk.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadHistoryDao {
    @Query("SELECT * FROM download_history ORDER BY downloadTime DESC")
    fun getAll(): Flow<List<DownloadHistory>>

    @Insert
    suspend fun insert(history: DownloadHistory): Long

    @Delete
    suspend fun delete(history: DownloadHistory)

    @Query("DELETE FROM download_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getConversation(convId: String): Flow<List<ChatMessage>>

    @Insert
    suspend fun insert(message: ChatMessage): Long

    @Delete
    suspend fun delete(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE conversationId = :convId")
    suspend fun deleteByConversationId(convId: String)

    @Query("SELECT DISTINCT conversationId FROM chat_messages ORDER BY timestamp DESC")
    fun getConversations(): Flow<List<String>>
}

@Dao
interface McpServerDao {
    @Query("SELECT * FROM mcp_servers ORDER BY createdAt DESC")
    fun getAll(): Flow<List<McpServer>>

    @Query("SELECT * FROM mcp_servers WHERE isEnabled = 1")
    fun getEnabled(): Flow<List<McpServer>>

    @Insert
    suspend fun insert(server: McpServer): Long

    @Update
    suspend fun update(server: McpServer)

    @Delete
    suspend fun delete(server: McpServer)
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY installedAt DESC")
    fun getAll(): Flow<List<Skill>>

    @Query("SELECT * FROM skills WHERE isEnabled = 1")
    fun getEnabled(): Flow<List<Skill>>

    @Query("SELECT * FROM skills WHERE trigger = :trigger LIMIT 1")
    suspend fun findByTrigger(trigger: String): Skill?

    @Insert
    suspend fun insert(skill: Skill): Long

    @Update
    suspend fun update(skill: Skill)

    @Delete
    suspend fun delete(skill: Skill)
}

@Dao
interface ScheduledTaskDao {
    @Query("SELECT * FROM scheduled_tasks ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ScheduledTask>>

    @Query("SELECT * FROM scheduled_tasks WHERE isEnabled = 1")
    fun getEnabled(): Flow<List<ScheduledTask>>

    @Insert
    suspend fun insert(task: ScheduledTask): Long

    @Update
    suspend fun update(task: ScheduledTask)

    @Delete
    suspend fun delete(task: ScheduledTask)
}

@Dao
interface UsageRecordDao {
    @Query("SELECT * FROM usage_records ORDER BY timestamp DESC LIMIT 200")
    fun getRecent(): Flow<List<UsageRecord>>

    @Query("SELECT * FROM usage_records WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getSince(startTime: Long): Flow<List<UsageRecord>>

    @Query("SELECT SUM(totalTokens) FROM usage_records WHERE timestamp >= :startTime")
    suspend fun getTotalTokensSince(startTime: Int): Int?

    @Insert
    suspend fun insert(record: UsageRecord): Long

    @Query("DELETE FROM usage_records")
    suspend fun clearAll()
}

@Dao
interface AssistantDao {
    @Query("SELECT * FROM assistants ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Assistant>>

    @Query("SELECT * FROM assistants WHERE isActive = 1 LIMIT 1")
    fun getActive(): Flow<Assistant?>

    @Query("SELECT * FROM assistants WHERE category = :category ORDER BY createdAt DESC")
    fun getByCategory(category: String): Flow<List<Assistant>>

    @Insert
    suspend fun insert(assistant: Assistant): Long

    @Update
    suspend fun update(assistant: Assistant)

    @Delete
    suspend fun delete(assistant: Assistant)

    @Query("SELECT * FROM assistants WHERE id = :id")
    suspend fun getById(id: Long): Assistant?

    @Query("UPDATE assistants SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE assistants SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Long)
}

@Dao
interface KnowledgeDocumentDao {
    @Query("SELECT * FROM knowledge_documents ORDER BY createdAt DESC")
    fun getAll(): Flow<List<KnowledgeDocument>>

    @Query("SELECT * FROM knowledge_documents WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<KnowledgeDocument>>

    @Query("SELECT * FROM knowledge_documents WHERE fileType = :fileType ORDER BY createdAt DESC")
    fun getByFileType(fileType: String): Flow<List<KnowledgeDocument>>

    @Insert
    suspend fun insert(document: KnowledgeDocument): Long

    @Update
    suspend fun update(document: KnowledgeDocument)

    @Delete
    suspend fun delete(document: KnowledgeDocument)

    @Query("SELECT * FROM knowledge_documents WHERE id = :id")
    suspend fun getById(id: Long): KnowledgeDocument?

    @Query("DELETE FROM knowledge_documents")
    suspend fun clearAll()

    @Query("SELECT * FROM knowledge_documents WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getByCategoryId(categoryId: Long): Flow<List<KnowledgeDocument>>
}

@Dao
interface KnowledgeCategoryDao {
    @Query("SELECT * FROM knowledge_categories ORDER BY createdAt DESC")
    fun getAll(): Flow<List<KnowledgeCategory>>

    @Insert
    suspend fun insert(category: KnowledgeCategory): Long

    @Update
    suspend fun update(category: KnowledgeCategory)

    @Delete
    suspend fun delete(category: KnowledgeCategory)

    @Query("SELECT * FROM knowledge_categories WHERE id = :id")
    suspend fun getById(id: Long): KnowledgeCategory?

    @Query("DELETE FROM knowledge_categories")
    suspend fun clearAll()
}
