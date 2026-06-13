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
    val currentSkillId: String? = null,
    val activeTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val allSkills: List<Skill> = emptyList(),
    val showSettlement: Boolean = false,
    val rawDurationMin: Int = 0,
    val netDurationMin: Int = 0,
    val description: String = "",
    val startDateTime: Long = System.currentTimeMillis(),
    val settlementResult: SettlementResult.Success? = null,
    val isCreatingTask: Boolean = false,
    val isPaused: Boolean = false,
    // #11 中断次数统计
    val interruptCount: Int = 0,
    // #12 上一次暂停原因
    val lastPauseReason: String? = null,
    // #24 全屏专注模式
    val isFullScreen: Boolean = false,
    // #21 毕业提示
    val showGraduationHint: Boolean = false,
    // 暂停原因对话框
    val showPauseReasonDialog: Boolean = false,
    // 正在进行的记录 ID（用于暂停日志）
    val inProgressRecordId: String? = null
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
        viewModelScope.launch {
            repository.getCompletedTasks().collect { tasks ->
                _uiState.update { it.copy(completedTasks = tasks) }
            }
        }
        viewModelScope.launch {
            repository.getAllSkills().collect { skills ->
                _uiState.update { it.copy(allSkills = skills) }
            }
        }
    }

    fun startFocus(taskId: String, taskName: String, skillId: String) {
        _uiState.update {
            it.copy(
                isFocusing = true,
                currentTaskId = taskId,
                currentTaskName = taskName,
                currentSkillId = skillId,
                elapsedSeconds = 0,
                showSettlement = false,
                startDateTime = System.currentTimeMillis(),
                interruptCount = 0,
                lastPauseReason = null,
                isFullScreen = false,
                showGraduationHint = false,
                inProgressRecordId = null
            )
        }
    }

    fun startFocusBySkill(skillId: String, skillName: String) {
        _uiState.update {
            it.copy(
                isFocusing = true,
                currentTaskId = null,
                currentTaskName = skillName,
                currentSkillId = skillId,
                elapsedSeconds = 0,
                showSettlement = false,
                startDateTime = System.currentTimeMillis(),
                interruptCount = 0,
                lastPauseReason = null,
                isFullScreen = false,
                showGraduationHint = false,
                inProgressRecordId = null
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
                isPaused = false,
                showSettlement = true,
                rawDurationMin = rawMin,
                netDurationMin = rawMin,
                description = ""
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

    fun updateDescription(text: String) {
        _uiState.update { it.copy(description = text) }
    }

    fun pauseFocus() {
        // 弹出暂停原因对话框
        _uiState.update { it.copy(showPauseReasonDialog = true) }
    }

    fun resumeFocus() {
        _uiState.update { it.copy(isPaused = false) }
    }

    /**
     * 确认暂停原因后真正暂停
     */
    fun confirmPause(reason: String, category: String) {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        // 记录暂停日志（如有进行中的记录）
        if (state.inProgressRecordId != null) {
            viewModelScope.launch {
                repository.insertPauseLog(
                    PauseLog(
                        id = java.util.UUID.randomUUID().toString(),
                        timeRecordId = state.inProgressRecordId,
                        pausedAt = now,
                        resumedAt = null,
                        reason = reason,
                        reasonCategory = category
                    )
                )
            }
        }
        _uiState.update {
            it.copy(
                isPaused = true,
                interruptCount = it.interruptCount + 1,
                lastPauseReason = reason,
                showPauseReasonDialog = false
            )
        }
    }

    fun dismissPauseReasonDialog() {
        // 跳过记录直接暂停
        _uiState.update {
            it.copy(
                isPaused = true,
                interruptCount = it.interruptCount + 1,
                lastPauseReason = "未说明",
                showPauseReasonDialog = false
            )
        }
    }

    /**
     * #24 切换全屏专注模式
     */
    fun toggleFullScreen() {
        _uiState.update { it.copy(isFullScreen = !it.isFullScreen) }
    }

    fun setFullScreen(enabled: Boolean) {
        _uiState.update { it.copy(isFullScreen = enabled) }
    }

    fun confirmSettlement() {
        val state = _uiState.value
        if (state.netDurationMin <= 0) return
        val skillId = state.currentSkillId ?: return

        val startDt = state.startDateTime
        val endDt = System.currentTimeMillis()

        viewModelScope.launch {
            val result = if (state.currentTaskId != null) {
                experienceEngine.settleTime(
                    taskId = state.currentTaskId,
                    rawDurationMin = state.rawDurationMin,
                    netDurationMin = state.netDurationMin,
                    startDateTime = startDt,
                    endDateTime = endDt,
                    description = state.description.ifBlank { state.currentTaskName ?: "" },
                    interruptCount = state.interruptCount,
                    lastPauseReason = state.lastPauseReason,
                    tagsSnapshot = emptyList(),
                    isFullScreen = state.isFullScreen
                )
            } else {
                experienceEngine.manualInject(
                    skillId = skillId,
                    netDurationMin = state.netDurationMin,
                    startDateTime = startDt,
                    endDateTime = endDt,
                    description = state.description.ifBlank { state.currentTaskName ?: "" }
                )
            }

            when (result) {
                is SettlementResult.Success -> {
                    if (state.currentTaskId != null) {
                        repository.markTaskCompleted(state.currentTaskId)
                    }
                    // #21 检查毕业提示
                    val skill = repository.getSkillById(skillId)
                    val showGraduation = skill != null &&
                        experienceEngine.calculateLevel(skill.totalExp) >= 100 &&
                        !skill.isGraduated
                    _uiState.update {
                        it.copy(
                            settlementResult = result,
                            showSettlement = false,
                            showGraduationHint = showGraduation
                        )
                    }
                }
                is SettlementResult.Error -> {
                    _uiState.update { it.copy(showSettlement = false) }
                }
            }
        }
    }

    /**
     * #21 主动毕业技能
     */
    fun graduateSkill(message: String) {
        val skillId = _uiState.value.currentSkillId ?: return
        viewModelScope.launch {
            repository.graduateSkill(skillId, message.ifBlank { "已完成此程，步入新境。" })
            _uiState.update { it.copy(showGraduationHint = false) }
        }
    }

    fun dismissGraduationHint() {
        _uiState.update { it.copy(showGraduationHint = false) }
    }

    fun dismissSettlement() {
        _uiState.update {
            it.copy(
                showSettlement = false,
                currentTaskId = null,
                currentTaskName = null,
                currentSkillId = null,
                elapsedSeconds = 0,
                description = "",
                interruptCount = 0,
                lastPauseReason = null,
                isFullScreen = false
            )
        }
    }

    fun dismissSettlementResult() {
        _uiState.update {
            it.copy(
                settlementResult = null,
                currentTaskId = null,
                currentTaskName = null,
                currentSkillId = null,
                elapsedSeconds = 0,
                description = "",
                interruptCount = 0,
                lastPauseReason = null,
                isFullScreen = false
            )
        }
    }

    fun showCreateTask() {
        _uiState.update { it.copy(isCreatingTask = true) }
    }

    fun hideCreateTask() {
        _uiState.update { it.copy(isCreatingTask = false) }
    }

    fun createTask(name: String, linkedSkillId: String) {
        viewModelScope.launch {
            val task = Task(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                linkedSkillId = linkedSkillId,
                createdAt = System.currentTimeMillis()
            )
            repository.insertTask(task)
            _uiState.update { it.copy(isCreatingTask = false) }
        }
    }

    /**
     #17 任务模板
     */
    fun createTemplate(name: String, linkedSkillId: String) {
        viewModelScope.launch {
            val task = Task(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                linkedSkillId = linkedSkillId,
                createdAt = System.currentTimeMillis(),
                isTemplate = true
            )
            repository.insertTask(task)
            _uiState.update { it.copy(isCreatingTask = false) }
        }
    }

    fun reactivateTask(taskId: String) {
        viewModelScope.launch {
            repository.reactivateTask(taskId)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }
}
