package com.arekb.facedown.domain.model

sealed interface TimerState {
    // 1. Initial state (Waiting for user input)
    data object Idle : TimerState

    // 2. Startup Mode (Phone is Face Up)
    data class Startup(
        val countdownSeconds: Int // 5, 4, 3...
    ) : TimerState

    // 3. Standard Focus Mode (Phone is Face Down)
    data class Running(
        val remainingSeconds: Long,
        val totalSeconds: Long,
        val currentProgress: Float // 0.0 to 1.0 helper for UI
    ) : TimerState

    // 3. The "Panic" Mode (Phone is Face Up)
    data class GracePeriod(
        val remainingGraceSeconds: Int, // e.g., 5, 4, 3...
        val originalRemainingSeconds: Long // Remember where we paused
    ) : TimerState

    data class Paused(
        val remainingSeconds: Long,
        val totalSeconds: Long,
        val currentProgress: Float
    ) : TimerState

    // Session Failed (Held face up too long)
    data object Failed : TimerState

    // Success (Time == 0) - Phase 3 will handle this
    data object Completed : TimerState
}