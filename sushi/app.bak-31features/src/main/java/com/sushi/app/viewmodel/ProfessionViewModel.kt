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

data class ProfessionUiState(
    val professions: List<ProfessionDisplay> = emptyList(),
    val selectedProfession: ProfessionDetail? = null,
    val isCreating: Boolean = false,
    val isLoading: Boolean = true
)

data class ProfessionDisplay(
    val profession: Profession,
    val level: Int,
    val progress: Int,
    val tierLabel: String
)

data class ProfessionDetail(
    val profession: Profession,
    val level: Int,
    val progress: Int,
    val tierLabel: String,
    val totalPureTimeMin: Int,
    val coreSkills: List<SkillWithProgress>,
    val unlockedAffixes: List<Affix> = emptyList(),
    val lockedAffixes: List<Affix> = emptyList(),
    val professionTasks: List<Task> = emptyList()
)

data class SkillWithProgress(
    val skill: Skill,
    val level: Int,
    val progress: Int
)

@HiltViewModel
class ProfessionViewModel @Inject constructor(
    private val repository: SushiRepository,
    private val experienceEngine: ExperienceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfessionUiState())
    val uiState: StateFlow<ProfessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllProfessions(),
                repository.getAllSkills()
            ) { professions, skills ->
                val displays = professions.map { prof ->
                    val level = experienceEngine.calculateLevel(prof.totalExp)
                    val progress = experienceEngine.calculateProgress(prof.totalExp)
                    val tier = experienceEngine.getVisualTier(level)
                    ProfessionDisplay(prof, level, progress, tier.label)
                }

                _uiState.update {
                    it.copy(professions = displays, isLoading = false)
                }
            }.collect()
        }
    }

    fun selectProfession(professionId: String) {
        viewModelScope.launch {
            val profession = repository.getProfessionById(professionId) ?: return@launch
            val level = experienceEngine.calculateLevel(profession.totalExp)
            val progress = experienceEngine.calculateProgress(profession.totalExp)
            val tier = experienceEngine.getVisualTier(level)

            val allSkills = repository.getAllSkills().first()

            val coreSkills = allSkills
                .filter { professionId in it.linkedProfessionIds }
                .map { skill ->
                    val sLevel = experienceEngine.calculateLevel(skill.totalExp)
                    val sProgress = experienceEngine.calculateProgress(skill.totalExp)
                    SkillWithProgress(skill, sLevel, sProgress)
                }

            // Get affixes for all core skills
            val allAffixes = repository.getAllAffixes().first()
            val coreSkillIds = coreSkills.map { it.skill.id }.toSet()
            val relevantAffixes = allAffixes.filter { it.requiredSkillId in coreSkillIds }
            val unlockedAffixes = relevantAffixes.filter { affix ->
                val skill = coreSkills.find { it.skill.id == affix.requiredSkillId }
                skill != null && skill.level >= affix.requiredSkillLevel
            }
            val lockedAffixes = relevantAffixes.filter { affix ->
                val skill = coreSkills.find { it.skill.id == affix.requiredSkillId }
                skill == null || skill.level < affix.requiredSkillLevel
            }

            // #20 职业专属任务
            val professionTasks = repository.getTasksByProfession(professionId).first()
                .filter { !it.isCompleted }

            _uiState.update {
                it.copy(
                    selectedProfession = ProfessionDetail(
                        profession, level, progress, tier.label,
                        profession.totalExp, coreSkills,
                        unlockedAffixes, lockedAffixes,
                        professionTasks = professionTasks
                    )
                )
            }
        }
    }

    fun clearSelectedProfession() {
        _uiState.update { it.copy(selectedProfession = null) }
    }

    fun showCreate() {
        _uiState.update { it.copy(isCreating = true) }
    }

    fun hideCreate() {
        _uiState.update { it.copy(isCreating = false) }
    }

    fun createProfession(name: String) {
        viewModelScope.launch {
            val profession = Profession(
                id = java.util.UUID.randomUUID().toString(),
                name = name
            )
            repository.insertProfession(profession)
            _uiState.update { it.copy(isCreating = false) }
        }
    }

    fun deleteProfession(professionId: String) {
        viewModelScope.launch {
            repository.deleteProfession(professionId)
            _uiState.update { it.copy(selectedProfession = null) }
        }
    }

    fun updateProfessionName(professionId: String, name: String) {
        viewModelScope.launch {
            repository.updateProfessionName(professionId, name)
            // Refresh selected profession if it's the one being edited
            if (_uiState.value.selectedProfession?.profession?.id == professionId) {
                selectProfession(professionId)
            }
        }
    }

    /**
     * #20 添加职业专属任务
     */
    fun addProfessionTask(professionId: String, name: String, linkedSkillId: String) {
        viewModelScope.launch {
            val task = Task(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                linkedSkillId = linkedSkillId,
                linkedProfessionId = professionId,
                createdAt = System.currentTimeMillis()
            )
            repository.insertTask(task)
            selectProfession(professionId)
        }
    }
}
