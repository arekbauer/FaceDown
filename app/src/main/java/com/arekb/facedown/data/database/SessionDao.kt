package com.arekb.facedown.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: FocusSession)

    // Powers the History List
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC LIMIT 50")
    fun getAllSessions(): Flow<List<FocusSession>>

    // Powers the Weekly Bar Chart (Phase 5)
    // We filter by a time range (e.g., Start of Monday -> Now)
    @Query("SELECT * FROM focus_sessions WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getSessionsInRange(startTime: Long, endTime: Long): List<FocusSession>

    // Powers the "Total Focus Time" badge
    @Query("SELECT SUM(durationMinutes) FROM focus_sessions")
    fun getTotalFocusMinutes(): Flow<Int?>

    // Fetches only timestamps for streak calculation with no limit
    @Query("SELECT timestamp FROM focus_sessions ORDER BY timestamp DESC")
    fun getSessionTimestamps(): Flow<List<Long>>
}