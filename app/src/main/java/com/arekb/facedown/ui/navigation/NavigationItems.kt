package com.arekb.facedown.ui.navigation

import com.arekb.facedown.R

// Simple Data Class for the Menu
data class NavigationItem(
    val route: Screen,
    val label: Int, // Resource ID
    val selectedIcon: Int,
    val unselectedIcon: Int
)

// The Configuration List
val mainScreens = listOf(
    NavigationItem(
        route = Screen.Timer,
        label = R.string.toolbar_timer_label,
        selectedIcon = R.drawable.toolbar_timer_filled,
        unselectedIcon = R.drawable.toolbar_timer_outlined
    ),
    NavigationItem(
        route = Screen.Stats,
        label = R.string.toolbar_stats_label,
        selectedIcon = R.drawable.toolbar_stats_filled,
        unselectedIcon = R.drawable.toolbar_stats_outlined
    ),
    NavigationItem(
        // Note: We point to Settings.Main as the entry point
        route = Screen.Settings.Main,
        label = R.string.toolbar_settings_label,
        selectedIcon = R.drawable.toolbar_settings_filled,
        unselectedIcon = R.drawable.toolbar_settings_outlined
    )
)