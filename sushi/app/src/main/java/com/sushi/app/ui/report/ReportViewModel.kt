package com.sushi.app.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.SushiContainer
import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.logic.ExperienceEngine
import com.sushi.app.logic.WeeklyReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val weeklyReport: WeeklyReport? = null,
    val activePeriod: List<Pair<String, Int>> = emptyList(),
    val peakHour: String = "",
    val skillDistribution: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true
)

class ReportViewModel(
    private val repository: SushiRepository = SushiContainer.repository,
    private val engine: ExperienceEngine = SushiContainer.engine
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val weekly = engine.generateWeeklyReport(System.currentTimeMillis())
            val period = engine.activePeriodAnalysis()
            val dist = engine.skillDistribution()

            val peak = period.maxByOrNull { it.value }
            val peakHourText = if (peak != null && peak.value > 0) peak.key else "暂无数据"

            _uiState.value = ReportUiState(
                weeklyReport = weekly,
                activePeriod = period.toList().sortedBy { it.first },
                peakHour = peakHourText,
                skillDistribution = dist,
                isLoading = false
            )
        }
    }
}
