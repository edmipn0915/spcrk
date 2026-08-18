package com.spcrk.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_history")
data class DownloadHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val platform: String,
    val filePath: String,
    val fileSize: Long,
    val downloadTime: Long,
    val videoUrl: String
)
