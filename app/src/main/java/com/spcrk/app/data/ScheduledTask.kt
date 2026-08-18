package com.spcrk.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_tasks")
data class ScheduledTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cronExpression: String,
    val action: String,
    val actionParams: String = "",
    val isEnabled: Boolean = true,
    val lastRun: Long = 0,
    val nextRun: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
