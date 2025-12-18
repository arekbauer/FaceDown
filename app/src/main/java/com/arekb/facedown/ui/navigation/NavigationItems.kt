package com.arekb.facedown.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.arekb.facedown.R
import com.arekb.facedown.ui.Screen

// Simple Data Class for the Menu
data class NavigationItem(
    val route: Screen,
    val label: Int, // Resource ID
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

//TODO: Change the icons

// The Configuration List
val mainScreens = listOf(
    NavigationItem(
        route = Screen.Timer,
        label = R.string.toolbar_timer_label,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    ),
    NavigationItem(
        route = Screen.Stats,
        label = R.string.toolbar_stats_label,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    ),
    NavigationItem(
        // Note: We point to Settings.Main as the entry point
        route = Screen.Settings.Main,
        label = R.string.toolbar_settings_label,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)