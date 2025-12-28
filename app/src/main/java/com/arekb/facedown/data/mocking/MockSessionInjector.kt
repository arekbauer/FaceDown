package com.arekb.facedown.data.mocking

import com.arekb.facedown.data.database.FocusSession
import com.arekb.facedown.data.database.SessionDao
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class MockSessionInjector @Inject constructor(
    private val sessionDao: SessionDao
) {

    suspend fun injectSingleSession() {
        val today = System.currentTimeMillis()

        createSession(offsetDays = 0, minutes = 15, tag = "Focus").let {
            sessionDao.insertSession(it)
        }
    }

    suspend fun injectMockData() {
        // Clear existing data (Optional: only if you want a clean slate)
        // sessionDao.deleteAll()

        val today = System.currentTimeMillis()
        val oneDay = 86400000L // 24 hours in millis

        val mockSessions = listOf(
            // 1. TODAY (Active Streak)
            createSession(offsetDays = 0, minutes = 45, tag = "Focus"),
            createSession(offsetDays = 0, minutes = 25, tag = "Study"),

            // 2. YESTERDAY (Streak continues)
            createSession(offsetDays = 1, minutes = 60, tag = "Work"),

            // 3. TWO DAYS AGO (Streak continues)
            createSession(offsetDays = 2, minutes = 15, tag = "Read"),

            // 4. FOUR DAYS AGO (Gap! Streak should stop at 3)
            createSession(offsetDays = 4, minutes = 30, tag = "Read"),

            // 5. LAST MONTH (For Heatmap later)
            createSession(offsetDays = 30, minutes = 50, tag = "Work")
        )

        mockSessions.forEach { sessionDao.insertSession(it) }
    }

    private fun createSession(offsetDays: Long, minutes: Int, tag: String): FocusSession {
        // Calculate timestamp for "X days ago"
        // We use LocalDate to ensure we hit the correct "calendar day" regardless of time
        val date = LocalDate.now().minusDays(offsetDays)
        val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return FocusSession(
            durationMinutes = minutes,
            timestamp = timestamp,
            tag = tag,
            note = "Mock Data",
        )
    }
}