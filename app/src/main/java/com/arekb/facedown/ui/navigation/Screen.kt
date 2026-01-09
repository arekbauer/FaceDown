package com.arekb.facedown.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen : NavKey {

    open val navSection: Screen get() = this
    @Serializable
    data object Timer : Screen()

    @Serializable
    sealed class Stats : Screen() {
        override val navSection: Screen get() = Main
        @Serializable
        data object Main : Stats()
        @Serializable
        data object History : Stats()
    }

    // Nested Hierarchy for Settings (Scalable)
    @Serializable
    sealed class Settings : Screen() {
        override val navSection: Screen get() = Main
        @Serializable
        data object Main : Settings()
        @Serializable
        data object TimerOptions : Settings()
        @Serializable
        data object Data : Settings()
        @Serializable
        data object About : Settings()
    }
}