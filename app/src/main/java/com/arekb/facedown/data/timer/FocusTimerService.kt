package com.arekb.facedown.data.timer

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.arekb.facedown.MainActivity
import com.arekb.facedown.R
import com.arekb.facedown.data.dnd.DoNotDisturbManager
import com.arekb.facedown.data.notification.FocusNotificationManager
import com.arekb.facedown.data.sensor.AccelerometerRepository
import com.arekb.facedown.data.timer.ServiceConstants.GRACE_LIMIT
import com.arekb.facedown.data.timer.ServiceConstants.STARTING_COUNTDOWN
import com.arekb.facedown.domain.audio.AudioPlayer
import com.arekb.facedown.domain.model.OrientationState
import com.arekb.facedown.domain.model.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

object ServiceConstants {
    const val ACTION_START = "ACTION_START"
    const val ACTION_PAUSE = "ACTION_PAUSE"
    const val ACTION_RESUME = "ACTION_RESUME"
    const val ACTION_RESET = "ACTION_RESET" // Used for going back to home via failing or manually

    const val STARTING_COUNTDOWN = 5
    const val GRACE_LIMIT = 10
}

@AndroidEntryPoint
class FocusTimerService : Service() {

    @Inject lateinit var sensorRepository: AccelerometerRepository
    @Inject lateinit var dndManager: DoNotDisturbManager
    @Inject lateinit var audioPlayer: AudioPlayer
    @Inject lateinit var notificationManager: FocusNotificationManager

    // Service Lifecycle Scope
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    private var storedDurationMillis: Long = 0L
    private var storedTotalDurationMillis: Long = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ServiceConstants.ACTION_START) {
            startForeground(1, createNotification("Focus Session Active"))
        }

        when (action) {
            ServiceConstants.ACTION_START -> {
                val minutes = intent.getIntExtra("DURATION", 25)
                storedTotalDurationMillis = minutes * 60 * 1000L
                startTimerLoop(storedTotalDurationMillis)
            }
            ServiceConstants.ACTION_PAUSE -> pauseTimer()

            ServiceConstants.ACTION_RESUME -> resumeTimer()

            ServiceConstants.ACTION_RESET -> {
                timerJob?.cancel()
                audioPlayer.stop()
                dndManager.turnOffDnd()
                TimerRepository.updateState(TimerState.Idle)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun pauseTimer() {
        // 1. Cancel the running loop
        timerJob?.cancel()

        // 2. Turn OFF DND
        dndManager.turnOffDnd()

        // 3. Update UI to Paused State
        val remainingSeconds = storedDurationMillis / 1000
        val totalSeconds = storedTotalDurationMillis / 1000

        TimerRepository.updateState(
            TimerState.Paused(
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds,
                currentProgress = 1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())
            )
        )

        // 4. Update Notification
        updateForegroundNotification(TimerState.GracePeriod(GRACE_LIMIT, (remainingSeconds/1000)))
    }

    private fun resumeTimer() {
        // Resume with the time we had left
        startTimerLoop(storedDurationMillis)
    }

    private fun startTimerLoop(durationMillis: Long) {
        dndManager.turnOnDnd()

        timerJob?.cancel()
        timerJob = serviceScope.launch {

            // 1. The Countdown
            for (i in STARTING_COUNTDOWN downTo 0) {
                TimerRepository.updateState(TimerState.Startup(i))
                delay(1000)
            }

            // 2. The Active Session
            val sessionEndTime = System.currentTimeMillis() + durationMillis

            // State variable to track when the user MUST put the phone back down
            var gracePeriodDeadline: Long? = null

            // We create a "Ticker" that emits Unit every second
            val tickerFlow = flow {
                while (currentCoroutineContext().isActive) {
                    emit(Unit)
                    delay(1000)
                }
            }

            // We combine the Ticker with the Sensor
            combine(tickerFlow, sensorRepository.orientationFlow) { _, orientation ->
                orientation
            }.collect { currentOrientation ->

                val now = System.currentTimeMillis()
                val timeRemainingMillis = sessionEndTime - now
                storedDurationMillis = timeRemainingMillis

                val secondsRemaining = (timeRemainingMillis / 1000).coerceAtLeast(0)

                // 1. CHECK FOR COMPLETION
                if (timeRemainingMillis <= 0) {
                    triggerAlarmSequence()
                    cancel() // Stop the coroutine
                    return@collect
                }

                // 2. STATE MACHINE LOGIC
                if (currentOrientation == OrientationState.FACE_UP) {

                    // A. Just entered Face Up? Set the deadline.
                    if (gracePeriodDeadline == null) {
                        gracePeriodDeadline = now + (GRACE_LIMIT * 1000)
                        updateForegroundNotification(TimerState.GracePeriod(GRACE_LIMIT, (timeRemainingMillis/1000)))
                    }

                    // B. Check if Deadline passed (Failure)
                    val graceRemainingMillis = gracePeriodDeadline!! - now
                    if (graceRemainingMillis <= 0) {
                        failSession()
                        cancel()
                        return@collect
                    }

                    // C. Update UI with Grace State
                    TimerRepository.updateState(
                        TimerState.GracePeriod(
                            remainingGraceSeconds = (graceRemainingMillis / 1000).toInt(),
                            originalRemainingSeconds = secondsRemaining
                        )
                    )

                } else {
                    // Face down logic

                    // A. Reset Grace Deadline (They recovered!)
                    gracePeriodDeadline = null

                    TimerRepository.updateState(
                        TimerState.Running(
                            remainingSeconds = secondsRemaining,
                            totalSeconds = storedTotalDurationMillis / 1000,
                            currentProgress = 1f - (secondsRemaining.toFloat() / (storedTotalDurationMillis / 1000).toFloat())
                        )
                    )

                    updateForegroundNotification(
                        TimerState.Running(
                            remainingSeconds = timeRemainingMillis / 1000,
                            totalSeconds = storedTotalDurationMillis / 1000,
                            currentProgress = 1f - (timeRemainingMillis.toFloat() / storedTotalDurationMillis)
                        )
                    )
                }
            }
        }
    }

    private fun triggerAlarmSequence() {
        // Calculate minutes from storedTotalDurationMillis
        val minutes = (storedTotalDurationMillis / 1000 / 60).toInt()
        TimerRepository.updateState(TimerState.Completed(minutes))

        try {
            // OPTION A: The System Default Alarm (Production Ready)
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // Fallback

            audioPlayer.playAscendingAlarm(alarmUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // C. The "Stop" Monitor
        // We launch a NEW coroutine to watch for the "Stop" gesture (Pick Up)
        serviceScope.launch {
            sensorRepository.orientationFlow
                .collect { orientation ->
                    // The "Physical Button" Logic:
                    if (orientation == OrientationState.FACE_UP) {
                        finishAndCleanup()
                        cancel() // Stop monitoring sensor
                    }
                }
        }
    }

    private fun finishAndCleanup() {
        // 1. Cut the Sound
        audioPlayer.stop()

        // 2. Restore System Settings
        dndManager.turnOffDnd()

        // 3. Kill Service
        stopSelf()
    }

    private fun failSession() {
        dndManager.turnOffDnd()
        TimerRepository.updateState(TimerState.Failed)
        stopSelf()
    }

    private fun updateForegroundNotification(state: TimerState, remaining: Long = storedDurationMillis) {
        val notification = notificationManager.buildNotification(
            state = state,
            totalDurationMillis = storedTotalDurationMillis,
            remainingMillis = remaining,
        )

        // For ACTION_START, we use startForeground. For updates, we use notificationManager.notify
        notificationManager.updateNotification(notification)
    }

    private fun createNotificationChannel() {
        val channelId = "focus_channel"
        val channelName = "Focus Timer Status"
        val channelDescription = "Shows the active timer status"

        val importance = android.app.NotificationManager.IMPORTANCE_LOW

        val channel = android.app.NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(content: String): android.app.Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, "focus_channel")
            .setContentTitle("FaceDown")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
        dndManager.turnOffDnd() // Safety net: Always turn off DND if service dies
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}