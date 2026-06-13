package com.sushi.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.data.model.*
import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.logic.ExperienceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ReviewUiState(
    val selectedDate: Long = System.currentTimeMillis(),
    val recordsByDate: List<TimeRecord> = emptyList(),
    val weeklyStats: Map<SkillCategory, Int> = emptyMap(),
    val monthlyStats: Map<SkillCategory, Int> = emptyMap(),
    val datesWithRecords: Set<Long> = emptySet(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: SushiRepository,
    private val experienceEngine: ExperienceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        loadDatesWithRecords()
        selectDate(System.currentTimeMillis())
    }

    private fun loadDatesWithRecords() {
        viewModelScope.launch {
            repository.getAllRecords().collect { records ->
                val dates = records.map { record ->
                    // 将 timestamp 转换为当天 0 点的时间戳
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = record.timestamp
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.timeInMillis
                }.toSet()
                _uiState.update { it.copy(datesWithRecords = dates) }
            }
        }
    }

    fun selectDate(timestamp: Long) {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = cal.timeInMillis

        viewModelScope.launch {
            repository.getRecordsByDate(startOfDay, endOfDay).collect { records ->
                _uiState.update {
                    it.copy(selectedDate = timestamp, recordsByDate = records)
                }
            }
        }

        loadWeeklyStats(startOfDay)
        loadMonthlyStats(startOfDay)
    }

    private fun loadWeeklyStats(referenceTime: Long) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply { timeInMillis = referenceTime }
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val weekStart = cal.timeInMillis

            cal.add(Calendar.WEEK_OF_YEAR, 1)
            val weekEnd = cal.timeInMillis

            val total = repository.getTotalNetDuration(weekStart, weekEnd)
            // 简化：按分类统计需要更复杂的查询，这里先展示总计
            _uiState.update { it.copy(weeklyStats = mapOf(), isLoading = false) }
        }
    }

    private fun loadMonthlyStats(referenceTime: Long) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply { timeInMillis = referenceTime }
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            val monthEnd = cal.timeInMillis

            val total = repository.getTotalNetDuration(monthStart, monthEnd)
            _uiState.update { it.copy(monthlyStats = mapOf(), isLoading = false) }
        }
    }
}
