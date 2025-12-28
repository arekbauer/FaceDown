package com.arekb.facedown.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.facedown.data.mocking.MockSessionInjector
import com.arekb.facedown.data.stats.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val sessionInjector: MockSessionInjector
) : ViewModel() {

    val currentStreak = statsRepository.currentStreak
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalFocusMinutes = statsRepository.totalFocusTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Helper to format for UI (e.g. "14h 30m")
    fun formatDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

     fun injectSessions(){
        viewModelScope.launch {
            sessionInjector.injectMockData()
        }
    }
}