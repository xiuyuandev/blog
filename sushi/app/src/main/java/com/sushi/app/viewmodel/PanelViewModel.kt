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

data class PanelUiState(
    val professionName: String = "游侠",
    val totalPureTimeMin: Int = 0,
    val attributes: Map<AttributeType, Int> = emptyMap(),
    val unlockedAffixes: List<Affix> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PanelViewModel @Inject constructor(
    private val repository: SushiRepository,
    private val experienceEngine: ExperienceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(PanelUiState())
    val uiState: StateFlow<PanelUiState> = _uiState.asStateFlow()

    // 内存中的属性面板状态，仅当有新 TimeRecord 插入时触发重新计算
    private var cachedAttributes: Map<AttributeType, Int>? = null

    init {
        viewModelScope.launch {
            combine(
                repository.getAllProfessions(),
                repository.getAllSkills(),
                repository.getAllAffixes()
            ) { professions, skills, affixes ->
                // 找到经验最高的职业作为核心职业
                val mainProfession = professions.maxByOrNull { it.totalExp }
                val professionName = mainProfession?.name ?: "游侠"

                // 计算总纯时间
                val totalPureTime = skills.sumOf { it.totalExp }

                // 计算属性面板
                val attributes = calculateAttributes(skills, affixes)
                cachedAttributes = attributes

                // 获取已解锁词条（技能等级 >= 10）
                val unlockedAffixes = affixes.filter { affix ->
                    val skill = skills.find { it.id == affix.requiredSkillId }
                    skill != null && experienceEngine.calculateLevel(skill.totalExp) >= affix.requiredSkillLevel
                }

                _uiState.update {
                    it.copy(
                        professionName = professionName,
                        totalPureTimeMin = totalPureTime,
                        attributes = attributes,
                        unlockedAffixes = unlockedAffixes,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    private fun calculateAttributes(
        skills: List<Skill>,
        affixes: List<Affix>
    ): Map<AttributeType, Int> {
        val attributes = mutableMapOf<AttributeType, Int>(
            AttributeType.PHYSIQUE to 0,
            AttributeType.INTELLECT to 0,
            AttributeType.CREATION to 0,
            AttributeType.INSIGHT to 0,
            AttributeType.DOMINION to 0
        )

        for (skill in skills) {
            val level = experienceEngine.calculateLevel(skill.totalExp)
            for (affix in affixes) {
                if (affix.requiredSkillId == skill.id && level >= affix.requiredSkillLevel) {
                    for ((attrType, bonus) in affix.bonusAttributes) {
                        attributes[attrType] = (attributes[attrType] ?: 0) + bonus
                    }
                }
            }
        }

        return attributes
    }

    /**
     * 当有新时间记录插入时，触发属性面板重新计算
     */
    fun refreshAttributes() {
        viewModelScope.launch {
            val attributes = experienceEngine.recalculateAttributes()
            cachedAttributes = attributes
            _uiState.update { it.copy(attributes = attributes) }
        }
    }
}
