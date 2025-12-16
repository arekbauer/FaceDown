package com.arekb.facedown

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FaceDownApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            "focus_channel",
            "Focus Timer",
            NotificationManager.IMPORTANCE_LOW // Low importance so it doesn't make sound
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}