package com.arekb.facedown.data.settings

import kotlinx.coroutines.flow.Flow

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

interface SettingsRepository {
    // Observables (The ViewModel watches these)
    val appTheme: Flow<AppTheme>
    val hapticsEnabled: Flow<Boolean>
    val soundEnabled: Flow<Boolean>
    val timerSoundUri: Flow<String?> // Null means default sound
    val hasSeenOnboarding: Flow<Boolean>

    // Actions (The ViewModel calls these)
    suspend fun setAppTheme(theme: AppTheme)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setTimerSoundUri(uri: String?)
    suspend fun setHasSeenOnboarding(seen: Boolean)
}