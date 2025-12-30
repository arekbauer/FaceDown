package com.arekb.facedown.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.facedown.data.mocking.MockSessionInjector
import com.arekb.facedown.data.stats.HeatmapLevel
import com.arekb.facedown.data.stats.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

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

    val weeklyStats = statsRepository.getWeeklyProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val heatmapState: StateFlow<List<HeatmapWeek>> = statsRepository.getHeatmapData()
        .map { dataMap ->
            // Convert the flat map into a list of Weeks
            val sortedDates = dataMap.keys.sorted()
            if (sortedDates.isEmpty()) return@map emptyList()

            // Chunk into groups of 7 days
            sortedDates.chunked(7).mapIndexed { index, batch ->
                HeatmapWeek(
                    weekIndex = index,
                    days = batch.map { date -> date to (dataMap[date] ?: HeatmapLevel.NONE) }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

     fun injectSessions(){
        viewModelScope.launch {
            sessionInjector.injectMockData()
        }
    }
}

data class HeatmapWeek(
    val weekIndex: Int,
    val days: List<Pair<LocalDate, HeatmapLevel>>
)