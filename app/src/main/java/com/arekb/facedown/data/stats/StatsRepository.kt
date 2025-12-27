package com.arekb.facedown.data.stats

import kotlinx.coroutines.flow.Flow

interface StatsRepository {

    val currentStreak: Flow<Int>
    val totalFocusTime: Flow<Int>

    fun calculateStreak(timestamps: List<Long>) : Int
}