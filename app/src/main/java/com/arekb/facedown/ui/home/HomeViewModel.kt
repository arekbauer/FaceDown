package com.arekb.facedown.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.facedown.data.dnd.DoNotDisturbManager
import com.arekb.facedown.data.timer.TimerRepository
import com.arekb.facedown.domain.model.TimerState
import com.arekb.facedown.domain.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dndManager: DoNotDisturbManager,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val timerState = TimerRepository.timerState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimerState.Idle
        )

    fun resetTimer() {

    }

    fun hasDndPermission(): Boolean {
        return dndManager.hasPermission()
    }

    fun saveSession(minutes: Int, tag: String, note: String?) {
        viewModelScope.launch {
            sessionRepository.logSession(minutes, tag, note)
            // After saving, we reset the app to the start screen
            resetTimer()
        }
    }
}