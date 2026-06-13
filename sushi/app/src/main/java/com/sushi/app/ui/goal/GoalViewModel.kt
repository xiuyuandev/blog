package com.sushi.app.ui.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.SushiContainer
import com.sushi.app.data.model.Goal
import com.sushi.app.data.model.Skill
import com.sushi.app.data.repository.SushiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Calendar
import java.util.UUID

data class GoalUiState(
    val goals: List<Goal> = emptyList(),
    val allSkills: List<Skill> = emptyList(),
    val showCreateDialog: Boolean = false
)

class GoalViewModel(
    private val repository: SushiRepository = SushiContainer.repository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getActiveGoals(),
                repository.getAllSkills()
            ) { goals, skills -> goals to skills }
                .onEach { (goals, skills) ->
                    _uiState.value = _uiState.value.copy(goals = goals, allSkills = skills)
                }
                .launchIn(viewModelScope)
        }
    }

    fun showCreateDialog() { _uiState.value = _uiState.value.copy(showCreateDialog = true) }
    fun hideCreateDialog() { _uiState.value = _uiState.value.copy(showCreateDialog = false) }

    fun createGoal(name: String, period: String, targetMinutes: Int, scopeSkillId: String?) {
        viewModelScope.launch {
            val (start, end) = currentPeriodRange(period)
            val goal = Goal(
                id = UUID.randomUUID().toString(),
                name = name,
                period = period,
                targetMinutes = targetMinutes,
                scopeSkillId = scopeSkillId,
                startTime = start,
                endTime = end,
                currentMinutes = 0,
                isActive = true
            )
            repository.insertGoal(goal)
            hideCreateDialog()
        }
    }

    fun deactivateGoal(id: String) {
        viewModelScope.launch { repository.deactivateGoal(id) }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch { repository.deleteGoal(id) }
    }

    private fun currentPeriodRange(period: String): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val endCal = Calendar.getInstance()
        when (period) {
            "WEEKLY" -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                endCal.timeInMillis = cal.timeInMillis
                endCal.add(Calendar.WEEK_OF_YEAR, 1)
            }
            "MONTHLY" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                endCal.timeInMillis = cal.timeInMillis
                endCal.add(Calendar.MONTH, 1)
            }
            else -> {
                endCal.add(Calendar.MONTH, 1)
            }
        }
        return cal.timeInMillis to endCal.timeInMillis
    }
}
