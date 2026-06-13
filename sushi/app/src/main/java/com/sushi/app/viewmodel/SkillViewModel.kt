package com.sushi.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.data.model.*
import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.logic.ExperienceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkillUiState(
    val selectedCategory: SkillCategory = SkillCategory.COGNITION,
    val skills: List<SkillDisplay> = emptyList(),
    val allSkills: List<Skill> = emptyList(),
    val selectedSkill: SkillDetail? = null,
    val isLoading: Boolean = true
)

data class SkillDisplay(
    val skill: Skill,
    val level: Int,
    val progress: Int,
    val tierLabel: String
)

data class SkillDetail(
    val skill: Skill,
    val level: Int,
    val progress: Int,
    val tierLabel: String,
    val professions: List<Profession>,
    val unlockedAffixes: List<Affix>,
    val lockedAffixes: List<Affix>
)

@HiltViewModel
class SkillViewModel @Inject constructor(
    private val repository: SushiRepository,
    private val experienceEngine: ExperienceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillUiState())
    val uiState: StateFlow<SkillUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllSkills(),
                repository.getAllProfessions(),
                repository.getAllAffixes()
            ) { skills, professions, affixes ->
                val displays = skills.map { skill ->
                    val level = experienceEngine.calculateLevel(skill.totalExp)
                    val progress = experienceEngine.calculateProgress(skill.totalExp)
                    val tier = experienceEngine.getVisualTier(level)
                    SkillDisplay(skill, level, progress, tier.label)
                }

                _uiState.update { state ->
                    val filtered = displays.filter { it.skill.category == state.selectedCategory }
                    state.copy(
                        allSkills = skills,
                        skills = filtered,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun selectCategory(category: SkillCategory) {
        val allDisplays = _uiState.value.allSkills.map { skill ->
            val level = experienceEngine.calculateLevel(skill.totalExp)
            val progress = experienceEngine.calculateProgress(skill.totalExp)
            val tier = experienceEngine.getVisualTier(level)
            SkillDisplay(skill, level, progress, tier.label)
        }
        val filtered = allDisplays.filter { it.skill.category == category }
        _uiState.update { it.copy(selectedCategory = category, skills = filtered) }
    }

    fun selectSkill(skillId: String) {
        viewModelScope.launch {
            val skill = repository.getSkillById(skillId) ?: return@launch
            val level = experienceEngine.calculateLevel(skill.totalExp)
            val progress = experienceEngine.calculateProgress(skill.totalExp)
            val tier = experienceEngine.getVisualTier(level)

            val professions = skill.linkedProfessionIds.mapNotNull { id ->
                repository.getProfessionById(id)
            }

            val affixes = repository.getAffixesBySkillIds(listOf(skillId))
            val unlocked = affixes.filter { level >= it.requiredSkillLevel }
            val locked = affixes.filter { level < it.requiredSkillLevel }

            _uiState.update {
                it.copy(
                    selectedSkill = SkillDetail(
                        skill, level, progress, tier.label,
                        professions, unlocked, locked
                    )
                )
            }
        }
    }

    fun clearSelectedSkill() {
        _uiState.update { it.copy(selectedSkill = null) }
    }

    fun addProfessionToSkill(skillId: String, professionId: String) {
        viewModelScope.launch {
            val skill = repository.getSkillById(skillId) ?: return@launch
            val updated = skill.copy(
                linkedProfessionIds = skill.linkedProfessionIds + professionId
            )
            repository.updateSkill(updated)
        }
    }

    fun removeProfessionFromSkill(skillId: String, professionId: String) {
        viewModelScope.launch {
            val skill = repository.getSkillById(skillId) ?: return@launch
            val updated = skill.copy(
                linkedProfessionIds = skill.linkedProfessionIds.filter { it != professionId }
            )
            repository.updateSkill(updated)
        }
    }
}
