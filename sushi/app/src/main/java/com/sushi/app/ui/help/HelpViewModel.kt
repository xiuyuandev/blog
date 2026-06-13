package com.sushi.app.ui.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.data.model.HelpEntry
import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.logic.ExperienceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HelpUiState(
    val entries: List<HelpEntry> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HelpViewModel @Inject constructor(
    private val repository: SushiRepository,
    private val engine: ExperienceEngine
) : ViewModel() {
    private val _uiState = MutableStateFlow(HelpUiState())
    val uiState: StateFlow<HelpUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            engine.seedHelpEntries()
            val list = repository.getAllHelpEntries().first()
            _uiState.value = HelpUiState(entries = list, isLoading = false)
        }
    }
}
