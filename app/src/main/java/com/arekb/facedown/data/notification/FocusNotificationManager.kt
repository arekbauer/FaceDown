package com.arekb.facedown.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.arekb.facedown.R
import com.arekb.facedown.domain.model.TimerState
import java.util.Locale

class FocusNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        @Suppress("HardCodedStringLiteral")
        const val CHANNEL_ID = "focus_session_channel"
        const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.focus_session),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.shows_active_focus_timer_progress)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(
        state: TimerState,
        totalDurationMillis: Long,
        remainingMillis: Long
    ): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSilent(true) // The service handles the alarm logic
            .setPriority(NotificationCompat.PRIORITY_LOW)

        // Handle Version-Specific Progress Logic
        if (Build.VERSION.SDK_INT >= 36) { // Android 16 (Baklava)
            applyModernProgress(builder, state, totalDurationMillis, remainingMillis)
        } else {
            applyLegacyProgress(builder, state, totalDurationMillis, remainingMillis)
        }

        return builder.build()
    }

    /**
     * Android 16+ System-Rendered Progress
     */
    private fun applyModernProgress(
        builder: NotificationCompat.Builder,
        state: TimerState,
        totalMillis: Long,
        remainingMillis: Long,
    ) {
        val style = NotificationCompat.ProgressStyle()

        when (state) {
            is TimerState.Running -> {
                builder.setContentTitle(context.getString(R.string.focusing))
                builder.setContentText(context.getString(R.string.keep_phone_face_down))

                // Add the focus segment
                style.addProgressSegment(
                    NotificationCompat.ProgressStyle.Segment(totalMillis.toInt())
                        .setColor(context.getColor(R.color.ocean_blue))
                )

                // setWhen + setProgress allows the System UI to animate the countdown
                builder.setWhen(System.currentTimeMillis() + remainingMillis)
                builder.setStyle(style.setProgress((totalMillis - remainingMillis).toInt()))
                builder.setShortCriticalText(formatMillis(remainingMillis))
            }
            is TimerState.GracePeriod -> {
                builder.setContentTitle(context.getString(R.string.resume_focus))
                builder.setContentText(context.getString(R.string.flip_phone_back))

                // Add the focus segment
                style.addProgressSegment(
                    NotificationCompat.ProgressStyle.Segment(totalMillis.toInt())
                        .setColor(context.getColor(R.color.error_red))
                )

                // setWhen + setProgress allows the System UI to animate the countdown
                builder.setWhen(System.currentTimeMillis() + remainingMillis)
                builder.setStyle(style.setProgress((totalMillis - remainingMillis).toInt()))
                builder.setShortCriticalText(formatMillis(remainingMillis))
            }
            is TimerState.Paused -> {
                builder.setContentTitle(context.getString(R.string.timer_paused))
                builder.setContentText(context.getString(R.string.flip_to_resume))

                // In Grace/Pause, we show the segment but stop the "playhead" progress
                style.addProgressSegment(
                    NotificationCompat.ProgressStyle.Segment(totalMillis.toInt())
                        .setColor(context.getColor(R.color.slate_grey))
                )
                builder.setStyle(style.setProgress((totalMillis - remainingMillis).toInt()))
                builder.setShortCriticalText(formatMillis(remainingMillis))
            }
            else -> { /* Handle Idle/Startup */ }
        }
    }

    private fun applyLegacyProgress(
        builder: NotificationCompat.Builder,
        state: TimerState,
        totalMillis: Long,
        remainingMillis: Long
    ) {
        val progress = if (totalMillis > 0)
            ((totalMillis - remainingMillis).toFloat() / totalMillis * 100).toInt()
        else 0

        when (state) {
            is TimerState.Running -> {
                builder.setContentTitle(context.getString(R.string.focusing))
                builder.setProgress(100, progress, false)
            }
            is TimerState.GracePeriod -> {
                builder.setContentTitle(context.getString(R.string.put_the_phone_down))
                builder.setProgress(100, progress, false)
            }
            is TimerState.Paused -> {
                builder.setContentTitle(context.getString(R.string.timer_paused))
                builder.setProgress(100, progress, true) // Indeterminate to show pause
            }
            else -> {}
        }
    }

    private fun formatMillis(ms: Long): String {
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    fun updateNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}