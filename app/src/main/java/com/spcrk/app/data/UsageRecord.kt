package com.spcrk.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_records")
data class UsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val model: String,
    val provider: String,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0,
    val cost: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
