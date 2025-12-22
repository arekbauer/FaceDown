package com.arekb.facedown.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import com.arekb.facedown.data.timer.FocusTimerService
import com.arekb.facedown.data.timer.ServiceConstants

// Helper: Format Seconds to MM:SS
fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

fun sendTimerCommand(context: Context, action: String, minutes: Int = 0) {
    val intent = Intent(context, FocusTimerService::class.java).apply {
        this.action = action
        if (minutes > 0) putExtra("DURATION", minutes)
    }
    // Only use startForegroundService for the actual START command
    if (action == ServiceConstants.ACTION_START && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}