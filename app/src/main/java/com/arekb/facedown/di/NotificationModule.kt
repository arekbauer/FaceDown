package com.arekb.facedown.di

import android.content.Context
import com.arekb.facedown.data.notification.FocusNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): FocusNotificationManager {
        return FocusNotificationManager(context)
    }
}