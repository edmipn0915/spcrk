package com.spcrk.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spcrk.app.AppContainer
import com.spcrk.app.getAppContainer
import com.spcrk.app.ai.api.SkillService
import com.spcrk.app.data.Skill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SkillManageUiState(
    val skills: List<Skill> = emptyList(),
    val isLoading: Boolean = true
)

class SkillManageViewModel(application: Application) : AndroidViewModel(application) {

    private val container: AppContainer = getAppContainer(application)
    private val repository = container.repository
    private val skillService = container.skillService

    private val _uiState = MutableStateFlow(SkillManageUiState())
    val uiState: StateFlow<SkillManageUiState> = _uiState.asStateFlow()

    init { loadSkills() }

    private fun loadSkills() {
        viewModelScope.launch {
            repository.getAllSkills().collect { skills ->
                _uiState.value = SkillManageUiState(skills = skills, isLoading = false)
            }
        }
    }

    fun enableSkill(skill: Skill, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSkill(skill.copy(isEnabled = enabled))
            skillService.enableSkill(skill.id, enabled)
        }
    }

    fun deleteSkill(skill: Skill) {
        viewModelScope.launch {
            repository.deleteSkill(skill)
            skillService.uninstallSkill(skill.id)
        }
    }

    fun addSkill(skill: Skill) {
        viewModelScope.launch {
            repository.addSkill(skill)
            skillService.installSkill(skill)
        }
    }
}

class SkillManageViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SkillManageViewModel::class.java))
            return SkillManageViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
