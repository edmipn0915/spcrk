package com.spcrk.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DownloadHistory::class, Note::class, ChatMessage::class, McpServer::class, Skill::class, ScheduledTask::class, UsageRecord::class, Assistant::class, KnowledgeDocument::class, KnowledgeCategory::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadHistoryDao(): DownloadHistoryDao
    abstract fun noteDao(): NoteDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun mcpServerDao(): McpServerDao
    abstract fun skillDao(): SkillDao
    abstract fun scheduledTaskDao(): ScheduledTaskDao
    abstract fun usageRecordDao(): UsageRecordDao
    abstract fun assistantDao(): AssistantDao
    abstract fun knowledgeDocumentDao(): KnowledgeDocumentDao
    abstract fun knowledgeCategoryDao(): KnowledgeCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spcrk_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
