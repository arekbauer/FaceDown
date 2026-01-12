package com.arekb.facedown

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@Suppress("HardCodedStringLiteral")
private const val NOTIFICATION_ID = "focus_channel"

@HiltAndroidApp
class FaceDownApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            NOTIFICATION_ID,
            getString(R.string.focus_timer),
            NotificationManager.IMPORTANCE_LOW // Low importance so it doesn't make sound
        ).apply {
            description = getString(R.string.channel_description_focus)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}