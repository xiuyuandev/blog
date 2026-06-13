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
    val weeklyTotalMin: Int = 0,
    val monthlyTotalMin: Int = 0,
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

    // 用 selectedDate Flow 驱动记录查询，避免 collect 累积
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())

    init {
        // 监听所有记录，更新有记录的日期集合
        viewModelScope.launch {
            repository.getAllRecords().collect { records ->
                val dates = records.map { record ->
                    Calendar.getInstance().apply {
                        timeInMillis = record.startDateTime
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }.toSet()
                _uiState.update { it.copy(datesWithRecords = dates) }
            }
        }

        // 监听 selectedDate 变化，查询对应日期的记录
        viewModelScope.launch {
            _selectedDate.flatMapLatest { timestamp ->
                val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis

                cal.add(Calendar.DAY_OF_MONTH, 1)
                val endOfDay = cal.timeInMillis

                repository.getRecordsByDate(startOfDay, endOfDay)
            }.collect { records ->
                _uiState.update {
                    it.copy(recordsByDate = records, selectedDate = _selectedDate.value)
                }
            }
        }
    }

    fun selectDate(timestamp: Long) {
        _selectedDate.value = timestamp
        loadWeeklyStats(timestamp)
        loadMonthlyStats(timestamp)
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
            _uiState.update { it.copy(weeklyTotalMin = total, isLoading = false) }
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
            _uiState.update { it.copy(monthlyTotalMin = total, isLoading = false) }
        }
    }
}
