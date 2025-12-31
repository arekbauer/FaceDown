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

    // Gets the 5 most recent sessions
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC LIMIT 5")
    fun getRecentSessions(): Flow<List<FocusSession>>

    // Powers the "Total Focus Time" badge
    @Query("SELECT SUM(durationMinutes) FROM focus_sessions")
    fun getTotalFocusMinutes(): Flow<Int?>

    // Fetches only timestamps for streak calculation with no limit
    @Query("SELECT timestamp FROM focus_sessions ORDER BY timestamp DESC")
    fun getSessionTimestamps(): Flow<List<Long>>

    // Fetches sessions in a specific time window
    @Query("SELECT * FROM focus_sessions WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getSessionsInWindow(startTime: Long, endTime: Long): Flow<List<FocusSession>>
}