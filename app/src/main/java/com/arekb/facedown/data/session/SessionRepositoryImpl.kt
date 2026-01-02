package com.arekb.facedown.data.session

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.arekb.facedown.data.database.FocusSession
import com.arekb.facedown.data.database.SessionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
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

    override fun getSessionHistoryStream(): Flow<PagingData<FocusSession>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20, // Load 20 items at a time
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.getSessionsPagingSource() }
        ).flow
    }

    override fun getRecentSessions(): Flow<List<FocusSession>> {
        return dao.getRecentSessions()
            .flowOn(Dispatchers.Default)
    }
}