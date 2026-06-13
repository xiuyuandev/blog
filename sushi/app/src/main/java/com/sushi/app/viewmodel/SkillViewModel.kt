package com.sushi.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.data.model.*
import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.logic.ExperienceEngine
import com.sushi.app.logic.SettlementResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkillUiState(
    val selectedCategory: SkillCategory = SkillCategory.COGNITION,
    val skills: List<SkillDisplay> = emptyList(),
    val allSkills: List<Skill> = emptyList(),
    val selectedSkill: SkillDetail? = null,
    val isLoading: Boolean = true,
    val showManualInject: Boolean = false,
    val manualInjectResult: SettlementResult.Success? = null
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
    val lockedAffixes: List<Affix>,
    val historyRecords: List<TimeRecord> = emptyList()
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

            // 获取该技能的历史记录
            val historyRecords = repository.getRecordsBySkillId(skillId).first()

            _uiState.update {
                it.copy(
                    selectedSkill = SkillDetail(
                        skill, level, progress, tier.label,
                        professions, unlocked, locked, historyRecords
                    )
                )
            }
        }
    }

    fun clearSelectedSkill() {
        _uiState.update { it.copy(selectedSkill = null, showManualInject = false, manualInjectResult = null) }
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

    fun showManualInject() {
        _uiState.update { it.copy(showManualInject = true) }
    }

    fun hideManualInject() {
        _uiState.update { it.copy(showManualInject = false) }
    }

    fun manualInject(
        skillId: String,
        netDurationMin: Int,
        startDateTime: Long,
        endDateTime: Long,
        description: String
    ) {
        viewModelScope.launch {
            val result = experienceEngine.manualInject(
                skillId = skillId,
                netDurationMin = netDurationMin,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                description = description
            )
            when (result) {
                is SettlementResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showManualInject = false,
                            manualInjectResult = result
                        )
                    }
                    // 刷新技能详情
                    selectSkill(skillId)
                }
                is SettlementResult.Error -> {
                    _uiState.update { it.copy(showManualInject = false) }
                }
            }
        }
    }

    fun dismissManualInjectResult() {
        _uiState.update { it.copy(manualInjectResult = null) }
    }
}
