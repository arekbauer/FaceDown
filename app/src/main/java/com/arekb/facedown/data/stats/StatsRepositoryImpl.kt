package com.arekb.facedown.data.stats

import com.arekb.facedown.data.database.SessionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class HeatmapLevel { NONE, LOW, MEDIUM, HIGH, EXTREME }

class StatsRepositoryImpl @Inject constructor(
    private val dao: SessionDao
) : StatsRepository {

    override val currentStreak: Flow<Int> = dao.getSessionTimestamps()
        .map { timestamps ->
            calculateStreak(timestamps)
        }
        .flowOn(Dispatchers.Default)

    override val totalFocusTime: Flow<Int> = dao.getTotalFocusMinutes()
        .map { it ?: 0 } // Handle null (empty DB) by returning 0
        .flowOn(Dispatchers.IO)

    override fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        // Convert timestamps to unique "Days"
        // We use systemDefault() so it aligns with the user's current clock
        val daysWithSessions = timestamps
            .map { millis ->
                Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .distinct() // Remove duplicates (multiple sessions in one day = 1 day)

        if (daysWithSessions.isEmpty()) return 0

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val mostRecentSessionDate = daysWithSessions.first()

        // Check if the streak is effectively dead
        // If the last session wasn't today OR yesterday, the streak is broken.
        if (mostRecentSessionDate != today && mostRecentSessionDate != yesterday) {
            return 0
        }

        // Count the chain
        var streak = 0
        var checkDate = mostRecentSessionDate

        for (date in daysWithSessions) {
            if (date == checkDate) {
                streak++
                // Prepare to check for the day before this one
                checkDate = checkDate.minusDays(1)
            } else {
                // The chain broke
                break
            }
        }

        return streak
    }

    override fun getHeatmapData(): Flow<Map<LocalDate, HeatmapLevel>> {
        return dao.getSessionTimestamps()
            .map { timestamps ->
                // Group timestamps by Date
                val sessionsByDate = timestamps.groupBy {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }

                // Create a map of the last 365 days (Empty by default)
                val today = LocalDate.now()
                val firstSessionDate = sessionsByDate.keys.minOrNull()
                val startDate = firstSessionDate?.minusWeeks(16) ?: today.minusWeeks(16)
                val heatmap = mutableMapOf<LocalDate, HeatmapLevel>()

                var currentDate = startDate
                while (!currentDate.isAfter(today)) {
                    val count = sessionsByDate[currentDate]?.size ?: 0
                    val level = when {
                        count == 0 -> HeatmapLevel.NONE
                        count <= 2 -> HeatmapLevel.LOW
                        count <= 4 -> HeatmapLevel.MEDIUM
                        count <= 6 -> HeatmapLevel.HIGH
                        else -> HeatmapLevel.EXTREME
                    }
                    heatmap[currentDate] = level
                    currentDate = currentDate.plusDays(1)
                }
                heatmap
            }
            .flowOn(Dispatchers.Default)
    }
}