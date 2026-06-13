package com.sushi.app.ui.reflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.data.model.DailyReflection
import com.sushi.app.data.repository.SushiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class ReflectionUiState(
    val todayKey: String = "",
    val todayReflection: DailyReflection? = null,
    val pastReflections: List<DailyReflection> = emptyList(),
    val showEditDialog: Boolean = false,
    val editingReflection: DailyReflection? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ReflectionViewModel @Inject constructor(
    private val repository: SushiRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReflectionUiState())
    val uiState: StateFlow<ReflectionUiState> = _uiState.asStateFlow()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val today = dateFormat.format(Date())
            val todayR = repository.getReflectionByDate(today)
            val past = repository.getAllReflections().first()
                .filter { it.dateKey != today }
                .sortedByDescending { it.dateKey }
                .take(50)
            _uiState.value = _uiState.value.copy(
                todayKey = today,
                todayReflection = todayR,
                pastReflections = past,
                isLoading = false
            )
        }
    }

    fun showEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = true, editingReflection = null)
    }

    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, editingReflection = null)
    }

    fun editReflection(r: DailyReflection) {
        _uiState.value = _uiState.value.copy(showEditDialog = true, editingReflection = r)
    }

    fun saveReflection(highlight: String, lesson: String, improvement: String, mood: Int) {
        viewModelScope.launch {
            val existing = _uiState.value.editingReflection ?: _uiState.value.todayReflection
            val dateKey = existing?.dateKey ?: dateFormat.format(Date())
            val reflection = DailyReflection(
                id = existing?.id ?: UUID.randomUUID().toString(),
                dateKey = dateKey,
                highlight = highlight,
                lesson = lesson,
                improvement = improvement,
                moodScore = mood,
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )
            if (existing != null) {
                repository.updateReflection(reflection)
            } else {
                repository.insertReflection(reflection)
            }
            hideEditDialog()
            load()
        }
    }
}
