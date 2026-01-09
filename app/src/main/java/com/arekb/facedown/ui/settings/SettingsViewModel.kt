package com.arekb.facedown.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.facedown.data.settings.AppTheme
import com.arekb.facedown.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Expose streams for the UI to observe
    // using .stateIn ensures they are always hot and ready
    val currentTheme = settingsRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.SYSTEM)

    val isHapticsEnabled = settingsRepository.hapticsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isSoundEnabled = settingsRepository.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentSoundUri = settingsRepository.timerSoundUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Actions for the UI to call
    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setAppTheme(theme) }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun updateTimerSound(uri: String?) {
        viewModelScope.launch { settingsRepository.setTimerSoundUri(uri) }
    }

    fun clearHistory() {
        // We will connect this to your SessionRepository later!
    }
}