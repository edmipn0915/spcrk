package com.spcrk.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mcp_servers")
data class McpServer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val connectionType: String,
    val command: String = "",
    val args: String = "",
    val url: String = "",
    val headers: String = "",
    val isEnabled: Boolean = true,
    val source: String = "manual",
    val createdAt: Long = System.currentTimeMillis()
)
