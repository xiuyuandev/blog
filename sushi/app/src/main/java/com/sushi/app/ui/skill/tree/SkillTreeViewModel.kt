package com.sushi.app.ui.skill.tree

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.SushiContainer
import com.sushi.app.data.model.Skill
import com.sushi.app.data.repository.SushiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class SkillTreeUiState(
    val skills: List<Skill> = emptyList(),
    val isLoading: Boolean = true
)

class SkillTreeViewModel(
    private val repository: SushiRepository = SushiContainer.repository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SkillTreeUiState())
    val uiState: StateFlow<SkillTreeUiState> = _uiState.asStateFlow()

    init {
        repository.getAllSkills()
            .onEach { list ->
                _uiState.value = SkillTreeUiState(skills = list, isLoading = false)
            }
            .launchIn(viewModelScope)
    }
}
