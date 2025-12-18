package com.arekb.facedown.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
fun rememberFaceDownAppState(
    // We inject the backStack here so it survives config changes
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
        return backStack.lastOrNull() == route
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
                    backStack.removeLast()
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

    fun goBack() {
        backStack.removeLastOrNull()
    }
}