package com.arekb.facedown.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("HardCodedStringLiteral")
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsRepository {

    // 2. Define the Keys
    @Suppress("HardCodedStringLiteral")
    private object Keys {
        val THEME = stringPreferencesKey("app_theme")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val TIMER_SOUND_URI = stringPreferencesKey("timer_sound_uri")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    // 3. Get the DataStore instance
    private val dataStore = context.dataStore

    // --- READING DATA ---

    override val appTheme: Flow<AppTheme> = dataStore.data.map { prefs ->
        // Default to SYSTEM if nothing is saved
        val name = prefs[Keys.THEME] ?: AppTheme.SYSTEM.name
        try {
            AppTheme.valueOf(name)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    override val hapticsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.HAPTICS] ?: true // Default: On
    }

    override val soundEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.SOUND_ENABLED] ?: true // Default: On
    }

    override val timerSoundUri: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.TIMER_SOUND_URI] // Default: null (System Default)
    }

    override val hasSeenOnboarding: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETE] ?: false // Default: False
    }

    // --- WRITING DATA ---

    override suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme.name
        }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.HAPTICS] = enabled
        }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    override suspend fun setTimerSoundUri(uri: String?) {
        dataStore.edit { prefs ->
            if (uri != null) {
                prefs[Keys.TIMER_SOUND_URI] = uri
            } else {
                prefs.remove(Keys.TIMER_SOUND_URI)
            }
        }
    }

    override suspend fun setHasSeenOnboarding(seen: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETE] = seen
        }
    }
}