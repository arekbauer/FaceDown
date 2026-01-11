package com.arekb.facedown.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.facedown.data.dnd.DoNotDisturbManager
import com.arekb.facedown.data.session.SessionRepository
import com.arekb.facedown.data.timer.TimerRepository
import com.arekb.facedown.domain.model.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dndManager: DoNotDisturbManager,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _selectedDuration = MutableStateFlow(15)
    val selectedDuration = _selectedDuration.asStateFlow()

    val timerState = TimerRepository.timerState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimerState.Idle
        )

    fun setDuration(minutes: Int) {
        _selectedDuration.value = minutes
    }

    fun hasDndPermission(): Boolean {
        return dndManager.hasPermission()
    }

    fun saveSession(minutes: Int, tag: String, note: String?) {
        viewModelScope.launch {
            sessionRepository.logSession(minutes, tag, note)
        }
    }
}