package com.arekb.facedown.data.stats

import com.arekb.facedown.data.database.FocusSession
import com.arekb.facedown.ui.stats.components.WeeklyBarData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface StatsRepository {

    val currentStreak: Flow<Int>
    val totalFocusTime: Flow<Int>

    fun getRecentSessions(): Flow<List<FocusSession>>

    fun calculateStreak(timestamps: List<Long>) : Int

    fun getHeatmapData(): Flow<Map<LocalDate, HeatmapLevel>>

    fun getWeeklyProgress(): Flow<List<WeeklyBarData>>
}