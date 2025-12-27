package com.arekb.facedown.data.session

import com.arekb.facedown.data.database.FocusSession
import com.arekb.facedown.data.database.SessionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val dao: SessionDao
) : SessionRepository {

    override suspend fun logSession(minutes: Int, tag: String, note: String?) {
        val session = FocusSession(
            durationMinutes = minutes,
            timestamp = System.currentTimeMillis(),
            tag = tag,
            note = note
        )
        dao.insertSession(session)
    }

    override fun getRecentSessions(): Flow<List<FocusSession>> {
        return dao.getAllSessions()
    }

    override fun getTotalMinutes(): Flow<Int> {
        return dao.getTotalFocusMinutes().map { it ?: 0 }
    }
}