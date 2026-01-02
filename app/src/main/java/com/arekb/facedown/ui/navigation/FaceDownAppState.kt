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
        if (destination == Screen.Timer) {
            // Logic: Clear everything down to root
            if (backStack.size > 1) {
                // Remove everything after the first element (Timer)
                while (backStack.size > 1) {
                    backStack.removeAt(backStack.lastIndex)
                }
            }
        } else {
            // Logic: Switch Overlay
            if (backStack.size < 2) {
                backStack.add(destination)
            } else {
                // Replace the top element (Swap Stats <-> Settings)
                backStack[backStack.lastIndex] = destination
            }
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