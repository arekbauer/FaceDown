package com.arekb.facedown.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.widget.Toast
import androidx.core.net.toUri
import com.arekb.facedown.data.timer.FocusTimerService
import com.arekb.facedown.data.timer.ServiceConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    if (action == ServiceConstants.ACTION_START) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

fun launchEmailIntent(context: Context, email: String, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri() // Only email apps should handle this
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    // Safety check: ensure there is an app to handle it
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Support Email", email)
        clipboard.setPrimaryClip(clip)

        // Tell the user
        Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

suspend fun getRingtoneName(context: Context, uriString: String?): String = withContext(Dispatchers.IO) {
    if (uriString.isNullOrEmpty()) return@withContext "Default Notification"

    val uri = uriString.toUri()
    val ringtone = RingtoneManager.getRingtone(context, uri)
    ringtone?.getTitle(context) ?: "Unknown"
}