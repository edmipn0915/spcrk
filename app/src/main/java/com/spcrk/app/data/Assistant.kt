package com.spcrk.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assistants")
data class Assistant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val systemPrompt: String = "",
    val category: String = "general",
    val icon: String = "🤖",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

object PresetAssistants {
    val presets = listOf(
        Assistant(name = "翻译助手", description = "多语言互译", systemPrompt = "你是一个专业的翻译助手，可以翻译多种语言。", category = "language", icon = "🌐"),
        Assistant(name = "代码助手", description = "编程问题解答", systemPrompt = "你是一个专业的程序员，擅长编写和调试代码。", category = "code", icon = "💻"),
        Assistant(name = "写作助手", description = "文案创作优化", systemPrompt = "你是一个专业的写作助手，擅长创作和优化文案。", category = "writing", icon = "✍️"),
        Assistant(name = "分析助手", description = "数据分析总结", systemPrompt = "你是一个数据分析专家，擅长分析和总结信息。", category = "analysis", icon = "📊"),
        Assistant(name = "学习助手", description = "知识答疑解惑", systemPrompt = "你是一个耐心的老师，擅长解答学习问题。", category = "learning", icon = "📚")
    )
}
