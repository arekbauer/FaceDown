package com.arekb.facedown.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.facedown.data.dnd.DoNotDisturbManager
import com.arekb.facedown.data.sensor.AccelerometerRepository
import com.arekb.facedown.data.timer.TimerRepository
import com.arekb.facedown.domain.model.OrientationState
import com.arekb.facedown.domain.model.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accelerometerRepository: AccelerometerRepository,
    private val dndManager: DoNotDisturbManager
) : ViewModel() {

    // Expose state to UI
    val orientationState = accelerometerRepository.orientationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OrientationState.UNKNOWN
        )

    val timerState = TimerRepository.timerState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimerState.Idle
        )

    fun resetTimer() {
        TimerRepository.reset()
    }

    fun hasDndPermission(): Boolean {
        return dndManager.hasPermission()
    }
}