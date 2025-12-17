package com.arekb.facedown.data.dnd

import android.app.NotificationManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoNotDisturbManager @Inject constructor(
    private val notificationManager: NotificationManager
) {
    fun hasPermission(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun turnOnDnd() {
        if (hasPermission()) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }

    fun turnOffDnd() {
        if (hasPermission()) {
            // ALL allows everything back through
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}