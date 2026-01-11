package com.arekb.facedown.data.database

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arekb.facedown.R

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationMinutes: Int,
    val timestamp: Long,
    val tag: String,
    val note: String?
)

enum class TagType(
    val id: String,              // 1. The Stable ID (Saved to DB)
    @StringRes val labelRes: Int // 2. The Translation Key (Shown in UI)
) {
    FOCUS("focus", R.string.focus),
    WORK("work", R.string.work),
    STUDY("study", R.string.study),
    READ("read", R.string.read);

    companion object {
        // Helper: Converts DB String -> Enum
        // If the ID isn't found (e.g. data corruption), default to FOCUS
        fun fromId(id: String): TagType = entries.find { it.id == id } ?: FOCUS
    }
}