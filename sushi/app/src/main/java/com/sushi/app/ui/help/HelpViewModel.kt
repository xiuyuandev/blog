package com.sushi.app.ui.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.SushiContainer
import com.sushi.app.data.model.HelpEntry
import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.logic.ExperienceEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class HelpUiState(
    val entries: List<HelpEntry> = emptyList(),
    val isLoading: Boolean = true
)

class HelpViewModel(
    private val repository: SushiRepository = SushiContainer.repository,
    private val engine: ExperienceEngine = SushiContainer.engine
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
