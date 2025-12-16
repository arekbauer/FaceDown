package com.arekb.facedown.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.arekb.facedown.di.ApplicationScope
import com.arekb.facedown.domain.audio.AudioPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAudioPlayer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    // We inject an Application-scoped CoroutineScope for the fade-in logic
    // (This ensures the fade doesn't crash if a specific screen closes)
    @param:ApplicationScope private val externalScope: CoroutineScope
) : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var volumeJob: Job? = null

    override fun playAscendingAlarm(uri: Uri) {
        stop()

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )

            // Now strictly uses the URI passed in
            setDataSource(context, uri)

            isLooping = true
            setVolume(0.0f, 0.0f)

            setOnPreparedListener { mp ->
                mp.start()
                startVolumeFadeIn(mp)
            }

            // Robust Error Handling
            setOnErrorListener { _, what, extra ->
                // Log error in production (e.g., Crashlytics)
                stop() // Clean up
                true // Handled
            }

            prepareAsync()
        }
    }

    private fun startVolumeFadeIn(player: MediaPlayer) {
        volumeJob?.cancel()
        volumeJob = externalScope.launch {
            val fadeDuration = 10_000L // 10 seconds to max volume
            val steps = 20
            val delayPerStep = fadeDuration / steps

            for (i in 1..steps) {
                if (!isActive || mediaPlayer !== player) return@launch

                val volume = i / steps.toFloat()
                // Quadratic curve sounds more natural to human ear than linear
                val logarithmicVolume = volume * volume

                try {
                    if (player.isPlaying) {
                        player.setVolume(logarithmicVolume, logarithmicVolume)
                    }
                } catch (e: IllegalStateException) {
                    // Player might have been released concurrently
                    break
                }
                delay(delayPerStep)
            }
        }
    }

    override fun stop() {
        volumeJob?.cancel()
        volumeJob = null

        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
        } catch (e: IllegalStateException) {
            // Ignored: Player was already in an invalid state
        } finally {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}