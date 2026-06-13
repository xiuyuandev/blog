package com.sushi.app.ui.achievement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.data.model.Achievement
import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.logic.ExperienceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementUiState(
    val achievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val repository: SushiRepository,
    private val engine: ExperienceEngine
) : ViewModel() {
    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            engine.seedDefaultAchievements()
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val list = repository.getAllAchievementsSync()
            _uiState.value = AchievementUiState(
                achievements = list,
                unlockedCount = list.count { it.isUnlocked },
                totalCount = list.size,
                isLoading = false
            )
        }
    }
}
