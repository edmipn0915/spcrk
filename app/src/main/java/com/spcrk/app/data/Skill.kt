package com.spcrk.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val trigger: String,
    val config: String = "",
    val isEnabled: Boolean = true,
    val installedAt: Long = System.currentTimeMillis()
)
