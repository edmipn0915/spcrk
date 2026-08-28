package com.spcrk.app.ai.api

import com.spcrk.app.data.Skill
import kotlinx.coroutines.flow.Flow

interface SkillService {
    fun getAllSkills(): Flow<List<Skill>>
    suspend fun installSkill(skill: Skill)
    suspend fun uninstallSkill(id: Long)
    suspend fun enableSkill(id: Long, enabled: Boolean)
    suspend fun initializeBuiltInSkills()
    suspend fun triggerSkill(trigger: String, input: String): String
    fun getAllTriggers(): Map<String, suspend (String) -> String>
}
