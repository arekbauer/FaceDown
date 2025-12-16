package com.arekb.facedown.domain.audio

import android.net.Uri

interface AudioPlayer {
    /**
     * Starts playing the alarm sound with a gradual volume increase.
     * @param uri The URI of the alarm sound resource.
     */
    fun playAscendingAlarm(uri: Uri)

    /**
     * Immediately stops playback and releases resources.
     */
    fun stop()
}