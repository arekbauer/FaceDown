package com.arekb.facedown.data.stats

import com.arekb.facedown.data.database.SessionDao
import com.arekb.facedown.ui.stats.components.WeeklyBarData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
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

    override fun getWeeklyProgress(): Flow<List<WeeklyBarData>> {
        return flow {
            val now = LocalDate.now()
            val zoneId = ZoneId.systemDefault()

            // Calculate "This Week" window (Monday 00:00 -> Sunday 23:59)
            val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli()

            val endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(LocalTime.MAX)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()

            // Emit the flow from DB
            dao.getSessionsInWindow(startOfWeek, endOfWeek).collect { sessions ->

                // Group by Day of Week
                val minutesByDay = sessions.groupBy { session ->
                    Instant.ofEpochMilli(session.timestamp)
                        .atZone(zoneId)
                        .dayOfWeek // Enum: MONDAY, TUESDAY...
                }.mapValues { (_, sessionsList) ->
                    // Directly sum the minutes
                    sessionsList.sumOf { it.durationMinutes }
                }

                // D. Find the maximum value to normalize bar heights
                // If max is 0 (empty week), default to 1 to avoid divide-by-zero
                val maxMinutes = minutesByDay.values.maxOrNull() ?: 1
                val safeMax = if (maxMinutes == 0) 1 else maxMinutes

                // E. Build the list of 7 days (Mon-Sun)
                val barData = DayOfWeek.entries.map { dayOfWeek ->
                    val minutes = minutesByDay[dayOfWeek] ?: 0

                    WeeklyBarData(
                        dayLabel = dayOfWeek.name.take(1), // "M", "T"...
                        minutes = minutes,
                        ratio = minutes.toFloat() / safeMax.toFloat(),
                        isToday = dayOfWeek == now.dayOfWeek
                    )
                }

                emit(barData)
            }
        }.flowOn(Dispatchers.Default)
    }
}