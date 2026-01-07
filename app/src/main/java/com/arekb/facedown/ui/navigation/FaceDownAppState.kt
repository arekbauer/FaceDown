package com.arekb.facedown.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
fun rememberFaceDownAppState(
    backStack: MutableList<NavKey> = rememberNavBackStack(Screen.Timer)
): FaceDownAppState {
    return remember(backStack) {
        FaceDownAppState(backStack)
    }
}

@Stable
class FaceDownAppState(
    val backStack: MutableList<NavKey>
) {
    /**
     * The single source of truth for "Current Selection" logic.
     * This removes the logic from the UI.
     */
    fun isSelected(route: Screen): Boolean {
        val currentNavKey = backStack.lastOrNull() ?: return false

        // Cast the generic key to our specific Screen type
        val currentScreen = currentNavKey as? Screen ?: return false

        // Now .navSection will be recognized
        return currentScreen.navSection == route.navSection
    }

    /**
     * Encapsulated Navigation Logic.
     * "Timer is Base. Others are Overlays. Max depth 2."
     */
    fun navigateToTopLevelDestination(destination: Screen) {
        // 1. If we are already on this screen, do nothing
        if (backStack.lastOrNull() == destination) return

        // 2. Check if the destination is already in the history
        val index = backStack.indexOf(destination)

        if (index != -1) {
            // CASE: Going BACK to a previous screen (e.g. Timer or Stats)
            // We pop everything that was added after this screen.
            // Example: [Timer, Stats, Settings] -> Click Stats -> [Timer, Stats]
            // This triggers the 'popTransitionSpec' (Slide Right)
            while (backStack.lastIndex > index) {
                backStack.removeLast()
            }
        } else {
            // CASE: Going FORWARD to a new screen
            // Example: [Timer, Stats] -> Click Settings -> [Timer, Stats, Settings]
            // This triggers the 'transitionSpec' (Slide Left)
            backStack.add(destination)
        }
    }

    /**
     * Simple Navigation Logic.
     */
    fun navigateTo(destination: Screen) {
        backStack.add(destination)
    }

    fun goBack() {
        backStack.removeLastOrNull()
    }
}