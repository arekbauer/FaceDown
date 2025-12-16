package com.arekb.facedown.data.timer

import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.arekb.facedown.R
import com.arekb.facedown.data.dnd.DoNotDisturbManager
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
    const val STARTING_COUNTDOWN = 5
    const val GRACE_LIMIT = 10
}

@AndroidEntryPoint
class FocusTimerService : Service() {

    @Inject lateinit var sensorRepository: AccelerometerRepository
    @Inject lateinit var dndManager: DoNotDisturbManager
    @Inject lateinit var audioPlayer: AudioPlayer

    // Service Lifecycle Scope
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val durationMinutes = intent?.getIntExtra("DURATION", 25) ?: 25 // hardcoded for now
        startSession(durationMinutes)

        // Foreground Service Notification (Required by Android)
        startForeground(1, createNotification("Focus Session Active"))

        return START_NOT_STICKY
    }

    private fun startSession(minutes: Int) {
        val durationMillis = minutes * 60 * 1000L

        dndManager.turnOnDnd()

        timerJob?.cancel()
        timerJob = serviceScope.launch {

            // 1. The Countdown
            for (i in STARTING_COUNTDOWN downTo 1) {
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
                    // FACE DOWN (Safe Zone)

                    // A. Reset Grace Deadline (They recovered!)
                    gracePeriodDeadline = null

                    // B. Update UI with Running State
                    val totalDurationSeconds = minutes * 60
                    TimerRepository.updateState(
                        TimerState.Running(
                            remainingSeconds = secondsRemaining,
                            totalSeconds = totalDurationSeconds.toLong(),
                            currentProgress = 1f - (secondsRemaining.toFloat() / totalDurationSeconds.toFloat())
                        )
                    )
                }
            }
        }
    }

    private fun triggerAlarmSequence() {
        TimerRepository.updateState(TimerState.Completed)

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

    private fun createNotification(content: String): android.app.Notification {
        return NotificationCompat.Builder(this, "focus_channel")
            .setContentTitle("FaceDown")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
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