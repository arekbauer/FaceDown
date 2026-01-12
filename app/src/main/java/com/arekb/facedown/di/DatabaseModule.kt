package com.arekb.facedown.di

import android.content.Context
import androidx.room.Room
import com.arekb.facedown.data.database.AppDatabase
import com.arekb.facedown.data.database.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("HardCodedStringLiteral")
private const val DATABASE_NAME = "facedown_db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            ).fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideSessionDao(database: AppDatabase): SessionDao {
        return database.sessionDao()
    }
}