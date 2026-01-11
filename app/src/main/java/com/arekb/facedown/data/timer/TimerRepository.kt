package com.arekb.facedown.data.timer

import com.arekb.facedown.domain.model.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// A simple Singleton to bridge the Service and the UI
object TimerRepository {
    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState = _timerState.asStateFlow()

    fun updateState(newState: TimerState) {
        _timerState.value = newState
    }

}