package com.arekb.facedown.ui.settings

import android.app.LocaleManager
import android.content.Context
import android.net.Uri
import android.os.LocaleList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.facedown.data.backup.BackupRepository
import com.arekb.facedown.data.session.SessionRepository
import com.arekb.facedown.data.settings.AppTheme
import com.arekb.facedown.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val backupRepository: BackupRepository,
    @param:ApplicationContext private val context: Context
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

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

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
        viewModelScope.launch { sessionRepository.clearAllData() }
    }

    fun performExport(uri: Uri) {
        viewModelScope.launch {
            val result = backupRepository.exportDataToUri(uri)

            if (result.isSuccess) {
                _uiEvent.send("Export successful!")
            } else {
                _uiEvent.send("Export failed.")
            }
        }
    }

    // Function to change language
    fun setAppLocale(languageCode: String) {
        // For Android 13+ (API 33+)
        // The system handles storage automatically
        context.getSystemService(LocaleManager::class.java)
            ?.applicationLocales = LocaleList.forLanguageTags(languageCode)
    }

    // Function to get current language tag (for UI display)
    fun getCurrentLanguage(): String {
        val currentLocales =
            context.getSystemService(LocaleManager::class.java)?.applicationLocales

        // If empty, it means "System"
        return if (currentLocales != null && !currentLocales.isEmpty) {
            currentLocales.get(0).language
        } else {
            "system"
        }
    }
}