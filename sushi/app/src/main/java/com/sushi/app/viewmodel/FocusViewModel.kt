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

data class FocusUiState(
    val isFocusing: Boolean = false,
    val elapsedSeconds: Int = 0,
    val currentTaskId: String? = null,
    val currentTaskName: String? = null,
    val activeTasks: List<Task> = emptyList(),
    val showSettlement: Boolean = false,
    val rawDurationMin: Int = 0,
    val netDurationMin: Int = 0,
    val settlementResult: SettlementResult.Success? = null,
    val isCreatingTask: Boolean = false
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val repository: SushiRepository,
    private val experienceEngine: ExperienceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getActiveTasks().collect { tasks ->
                _uiState.update { it.copy(activeTasks = tasks) }
            }
        }
    }

    fun startFocus(taskId: String, taskName: String) {
        _uiState.update {
            it.copy(
                isFocusing = true,
                currentTaskId = taskId,
                currentTaskName = taskName,
                elapsedSeconds = 0,
                showSettlement = false
            )
        }
    }

    fun updateElapsedTime(seconds: Int) {
        _uiState.update { it.copy(elapsedSeconds = seconds) }
    }

    fun stopFocus() {
        val elapsed = _uiState.value.elapsedSeconds
        val rawMin = elapsed / 60
        _uiState.update {
            it.copy(
                isFocusing = false,
                showSettlement = true,
                rawDurationMin = rawMin,
                netDurationMin = rawMin // 默认纯时间等于原始时间
            )
        }
    }

    fun adjustNetDuration(minutes: Int) {
        val current = _uiState.value.netDurationMin
        val newNet = (current + minutes).coerceAtLeast(0)
        _uiState.update { it.copy(netDurationMin = newNet) }
    }

    fun setNetDuration(minutes: Int) {
        _uiState.update { it.copy(netDurationMin = minutes.coerceAtLeast(0)) }
    }

    fun confirmSettlement() {
        val state = _uiState.value
        val taskId = state.currentTaskId ?: return

        viewModelScope.launch {
            val result = experienceEngine.settleTime(
                taskId = taskId,
                rawDurationMin = state.rawDurationMin,
                netDurationMin = state.netDurationMin
            )

            when (result) {
                is SettlementResult.Success -> {
                    _uiState.update {
                        it.copy(
                            settlementResult = result,
                            showSettlement = false
                        )
                    }
                }
                is SettlementResult.Error -> {
                    // 处理错误
                    _uiState.update { it.copy(showSettlement = false) }
                }
            }
        }
    }

    fun dismissSettlementResult() {
        _uiState.update {
            it.copy(
                settlementResult = null,
                currentTaskId = null,
                currentTaskName = null,
                elapsedSeconds = 0
            )
        }
    }

    fun showCreateTask() {
        _uiState.update { it.copy(isCreatingTask = true) }
    }

    fun hideCreateTask() {
        _uiState.update { it.copy(isCreatingTask = false) }
    }

    fun createTask(name: String, linkedSkillIds: List<String>) {
        viewModelScope.launch {
            val task = Task(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                linkedSkillIds = linkedSkillIds,
                createdAt = System.currentTimeMillis()
            )
            repository.insertTask(task)
            _uiState.update { it.copy(isCreatingTask = false) }
        }
    }
}
