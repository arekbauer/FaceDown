package com.arekb.facedown.data.backup

import android.content.Context
import android.net.Uri
import com.arekb.facedown.data.database.SessionDao
import com.arekb.facedown.data.database.TagType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
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
            csvBuilder.append("Date,Time,Duration (Min),Tag,Note\n")

            // 2. Date Formatter
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            // 3. Build the rows
            for (session in sessions) {
                val dateStr = dateFormat.format(Date(session.timestamp))
                val timeStr = timeFormat.format(Date(session.timestamp))

                val tagEnum = TagType.fromId(session.tag)
                val humanReadableTag = context.getString(tagEnum.labelRes)

                // Handle nullable note (if null, replace with empty string)
                // We wrap tag and note in quotes "" to safely handle commas inside the text
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