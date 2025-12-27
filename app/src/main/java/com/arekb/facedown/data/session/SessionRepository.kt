package com.arekb.facedown.data.session

import com.arekb.facedown.data.database.FocusSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun logSession(minutes: Int, tag: String, note: String?)
    fun getRecentSessions(): Flow<List<FocusSession>>
    fun getTotalMinutes(): Flow<Int>
}