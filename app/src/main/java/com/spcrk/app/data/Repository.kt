package com.spcrk.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class Repository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val historyDao = db.downloadHistoryDao()
    private val noteDao = db.noteDao()
    private val chatDao = db.chatMessageDao()
    private val mcpServerDao = db.mcpServerDao()
    private val skillDao = db.skillDao()
    private val scheduledTaskDao = db.scheduledTaskDao()
    private val usageRecordDao = db.usageRecordDao()
    private val assistantDao = db.assistantDao()
    private val knowledgeDocumentDao = db.knowledgeDocumentDao()
    private val knowledgeCategoryDao = db.knowledgeCategoryDao()

    fun getAllHistory(): Flow<List<DownloadHistory>> = historyDao.getAll()
    suspend fun addHistory(history: DownloadHistory) = historyDao.insert(history)
    suspend fun deleteHistory(history: DownloadHistory) = historyDao.delete(history)
    suspend fun deleteHistoryById(id: Long) = historyDao.deleteById(id)

    fun getAllNotes(): Flow<List<Note>> = noteDao.getAll()
    suspend fun addNote(note: Note) = noteDao.insert(note)
    suspend fun updateNote(note: Note) = noteDao.update(note)
    suspend fun deleteNote(note: Note) = noteDao.delete(note)
    suspend fun getNoteById(id: Long) = noteDao.getById(id)

    fun getConversation(convId: String): Flow<List<ChatMessage>> = chatDao.getConversation(convId)
    suspend fun addMessage(message: ChatMessage) = chatDao.insert(message)
    suspend fun deleteMessage(message: ChatMessage) = chatDao.delete(message)
    suspend fun deleteConversationMessages(convId: String) = chatDao.deleteByConversationId(convId)
    fun getConversations(): Flow<List<String>> = chatDao.getConversations()

    fun getAllMcpServers(): Flow<List<McpServer>> = mcpServerDao.getAll()
    fun getEnabledMcpServers(): Flow<List<McpServer>> = mcpServerDao.getEnabled()
    suspend fun addMcpServer(server: McpServer) = mcpServerDao.insert(server)
    suspend fun updateMcpServer(server: McpServer) = mcpServerDao.update(server)
    suspend fun deleteMcpServer(server: McpServer) = mcpServerDao.delete(server)

    fun getAllSkills(): Flow<List<Skill>> = skillDao.getAll()
    fun getEnabledSkills(): Flow<List<Skill>> = skillDao.getEnabled()
    suspend fun findSkillByTrigger(trigger: String) = skillDao.findByTrigger(trigger)
    suspend fun addSkill(skill: Skill) = skillDao.insert(skill)
    suspend fun updateSkill(skill: Skill) = skillDao.update(skill)
    suspend fun deleteSkill(skill: Skill) = skillDao.delete(skill)

    fun getAllScheduledTasks(): Flow<List<ScheduledTask>> = scheduledTaskDao.getAll()
    fun getEnabledScheduledTasks(): Flow<List<ScheduledTask>> = scheduledTaskDao.getEnabled()
    suspend fun addScheduledTask(task: ScheduledTask) = scheduledTaskDao.insert(task)
    suspend fun updateScheduledTask(task: ScheduledTask) = scheduledTaskDao.update(task)
    suspend fun deleteScheduledTask(task: ScheduledTask) = scheduledTaskDao.delete(task)

    fun getRecentUsageRecords(): Flow<List<UsageRecord>> = usageRecordDao.getRecent()
    fun getUsageRecordsSince(startTime: Long): Flow<List<UsageRecord>> = usageRecordDao.getSince(startTime)
    suspend fun getTotalTokensSince(startTime: Int) = usageRecordDao.getTotalTokensSince(startTime)
    suspend fun addUsageRecord(record: UsageRecord) = usageRecordDao.insert(record)
    suspend fun clearUsageRecords() = usageRecordDao.clearAll()

    fun getAllAssistants(): Flow<List<Assistant>> = assistantDao.getAll()
    fun getActiveAssistant(): Flow<Assistant?> = assistantDao.getActive()
    fun getAssistantsByCategory(category: String): Flow<List<Assistant>> = assistantDao.getByCategory(category)
    suspend fun addAssistant(assistant: Assistant) = assistantDao.insert(assistant)
    suspend fun updateAssistant(assistant: Assistant) = assistantDao.update(assistant)
    suspend fun deleteAssistant(assistant: Assistant) = assistantDao.delete(assistant)
    suspend fun getAssistantById(id: Long) = assistantDao.getById(id)
    suspend fun clearActiveAssistant() = assistantDao.clearActive()
    suspend fun setActiveAssistant(id: Long) = assistantDao.setActive(id)

    fun getAllKnowledgeDocuments(): Flow<List<KnowledgeDocument>> = knowledgeDocumentDao.getAll()
    fun searchKnowledgeDocuments(query: String): Flow<List<KnowledgeDocument>> = knowledgeDocumentDao.search(query)
    fun getKnowledgeDocumentsByFileType(fileType: String): Flow<List<KnowledgeDocument>> = knowledgeDocumentDao.getByFileType(fileType)
    suspend fun addKnowledgeDocument(document: KnowledgeDocument) = knowledgeDocumentDao.insert(document)
    suspend fun updateKnowledgeDocument(document: KnowledgeDocument) = knowledgeDocumentDao.update(document)
    suspend fun deleteKnowledgeDocument(document: KnowledgeDocument) = knowledgeDocumentDao.delete(document)
    suspend fun getKnowledgeDocumentById(id: Long) = knowledgeDocumentDao.getById(id)
    suspend fun clearKnowledgeDocuments() = knowledgeDocumentDao.clearAll()
    fun getKnowledgeDocumentsByCategory(categoryId: Long) = knowledgeDocumentDao.getByCategoryId(categoryId)

    fun getAllCategories(): Flow<List<KnowledgeCategory>> = knowledgeCategoryDao.getAll()
    suspend fun addCategory(category: KnowledgeCategory) = knowledgeCategoryDao.insert(category)
    suspend fun updateCategory(category: KnowledgeCategory) = knowledgeCategoryDao.update(category)
    suspend fun deleteCategory(category: KnowledgeCategory) = knowledgeCategoryDao.delete(category)
    suspend fun getCategoryById(id: Long) = knowledgeCategoryDao.getById(id)
    suspend fun clearCategories() = knowledgeCategoryDao.clearAll()
}
