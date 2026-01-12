package com.arekb.facedown.data.backup

import android.content.Context
import android.net.Uri
import com.arekb.facedown.R
import com.arekb.facedown.data.database.SessionDao
import com.arekb.facedown.data.database.TagType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: SessionDao
) {

    suspend fun exportDataToUri(uri: Uri) = withContext(Dispatchers.IO) {
        try {
            val sessions = dao.getAllSessions()
            val csvBuilder = StringBuilder()
            // 1. The Header Row
            csvBuilder.append(context.getString(R.string.date_time_duration_min_tag_note) + "\n")

            // 2. Date Formatter
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

            val zoneId = ZoneId.systemDefault()

            // 3. Build the rows
            for (session in sessions) {
                android.util.Log.d("BackupDebug", "Raw Timestamp: ${session.timestamp}")
                // Convert Long -> Instant -> ZonedDateTime (Timezone aware)
                val zonedDateTime = Instant.ofEpochMilli(session.timestamp).atZone(zoneId)

                // Format using the modern formatters
                val dateStr = zonedDateTime.format(dateFormatter)
                val timeStr = zonedDateTime.format(timeFormatter)

                val tagEnum = TagType.fromId(session.tag)
                val humanReadableTag = context.getString(tagEnum.labelRes)

                // Handle nullable note and CSV escaping
                val safeNote = "\"${session.note ?: ""}\""

                csvBuilder.append(
                    "$dateStr,$timeStr,${session.durationMinutes},\"$humanReadableTag\",$safeNote\n"
                )
            }

            // 4. Write to file
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvBuilder.toString().toByteArray())
            }

            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

}