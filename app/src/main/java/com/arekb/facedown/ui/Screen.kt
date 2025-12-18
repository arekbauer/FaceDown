package com.arekb.facedown.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen : NavKey {
    @Serializable data object Timer : Screen()
    @Serializable data object Stats : Screen()

    // Nested Hierarchy for Settings (Scalable)
    @Serializable data object Settings : Screen() {
        @Serializable data object Main : Screen()
        // Future: @Serializable data object Appearance : Screen()
    }
}