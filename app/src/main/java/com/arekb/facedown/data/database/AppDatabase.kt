package com.arekb.facedown.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FocusSession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}