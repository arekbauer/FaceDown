package com.arekb.facedown.data.stats

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface StatsRepository {

    val currentStreak: Flow<Int>
    val totalFocusTime: Flow<Int>

    fun calculateStreak(timestamps: List<Long>) : Int

    fun getHeatmapData(): Flow<Map<LocalDate, HeatmapLevel>>
}