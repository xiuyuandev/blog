package com.sushi.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.SushiContainer
import com.sushi.app.sync.SyncConfigManager
import com.sushi.app.sync.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentStep: Int = 0
)

class OnboardingViewModel(
    private val configManager: SyncConfigManager = SushiContainer.syncConfigManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun markOnboarded() {
        viewModelScope.launch {
            configManager.saveOnboarded(true)
        }
    }
}
