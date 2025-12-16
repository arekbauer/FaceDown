package com.arekb.facedown.data.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.arekb.facedown.domain.model.OrientationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccelerometerRepository @Inject constructor(
    private val sensorManager: SensorManager
) {
    // Threshold: -8.0 allows for slight tilt (not perfectly flat), but definitely "down"
    private val faceDownThreshold = -8.0f

    val orientationFlow: Flow<OrientationState> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val zAxis = it.values[2] // Index 2 is the Z-axis

                    // Map raw physics to Domain State
                    val newState = if (zAxis < faceDownThreshold) {
                        OrientationState.FACE_DOWN
                    } else {
                        OrientationState.FACE_UP
                    }

                    trySend(newState)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No-op for this use case
            }
        }

        // Register the listener (Sampling rate: UI is sufficient for now)
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        // Cleanup when the Flow collection stops (Crucial for battery!)
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
        // Optimization: Only emit if the state actually changes
        .distinctUntilChanged()
        // Optimization: Run on IO thread
        .flowOn(Dispatchers.IO)
}