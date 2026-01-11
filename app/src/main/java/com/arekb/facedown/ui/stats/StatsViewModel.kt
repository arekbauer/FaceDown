package com.arekb.facedown.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.arekb.facedown.data.database.FocusSession
import com.arekb.facedown.data.mocking.MockSessionInjector
import com.arekb.facedown.data.session.SessionRepository
import com.arekb.facedown.data.stats.HeatmapLevel
import com.arekb.facedown.data.stats.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@HiltViewModel
class StatsViewModel @Inject constructor(
    statsRepository: StatsRepository,
    sessionRepository: SessionRepository,
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

    val recentSessions = sessionRepository.getRecentSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val historyPagingFlow = sessionRepository.getSessionHistoryStream()
        .map { pagingData ->
            pagingData.map { HistoryItem.SessionItem(it) } // 1. Wrap raw items
                .insertSeparators { before: HistoryItem.SessionItem?, after: HistoryItem.SessionItem? ->
                    // logic: decide if we need a header between 'before' and 'after'

                    if (after == null) {
                        // End of list
                        return@insertSeparators null
                    }

                    if (before == null) {
                        // Start of list: Always show a header for the very first item
                        return@insertSeparators HistoryItem.Header(getHeaderForTimestamp(after.session.timestamp))
                    }

                    // Check if the group changed
                    val beforeHeader = getHeaderForTimestamp(before.session.timestamp)
                    val afterHeader = getHeaderForTimestamp(after.session.timestamp)

                    if (beforeHeader != afterHeader) {
                        // The group changed! Insert the new header.
                        HistoryItem.Header(afterHeader)
                    } else {
                        null // Same group, no separator needed
                    }
                }
        }
        .cachedIn(viewModelScope)

     fun injectSessions(){
        viewModelScope.launch {
            sessionInjector.injectMockData()
        }
    }
}

private fun getHeaderForTimestamp(timestamp: Long): String {
    val zoneId = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
    val today = LocalDate.now()

    return when {
        date.isEqual(today) -> "Today"
        date.isEqual(today.minusDays(1)) -> "Yesterday"
        // For older items: "October 2024"
        else -> date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }
}

data class HeatmapWeek(
    val weekIndex: Int,
    val days: List<Pair<LocalDate, HeatmapLevel>>
)

sealed class HistoryItem {
    data class SessionItem(val session: FocusSession) : HistoryItem()
    data class Header(val title: String) : HistoryItem()
}