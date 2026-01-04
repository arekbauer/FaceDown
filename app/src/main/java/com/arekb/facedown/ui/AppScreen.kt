package com.arekb.facedown.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.arekb.facedown.domain.model.TimerState
import com.arekb.facedown.ui.home.HomeScreen
import com.arekb.facedown.ui.home.HomeViewModel
import com.arekb.facedown.ui.navigation.FaceDownAppState
import com.arekb.facedown.ui.navigation.Screen
import com.arekb.facedown.ui.navigation.mainScreens
import com.arekb.facedown.ui.navigation.rememberFaceDownAppState
import com.arekb.facedown.ui.stats.HistoryScreen
import com.arekb.facedown.ui.stats.StatsScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    appState: FaceDownAppState = rememberFaceDownAppState(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    // 1. Setup Window & Layout Helpers
    val layoutDirection = LocalLayoutDirection.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()
    val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()

    val timerState = homeViewModel.timerState.collectAsStateWithLifecycle().value
    val showBottomBar = when (timerState) {
        is TimerState.Idle -> true
        else -> false
    }

    // 3. Toolbar Behavior
    val toolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        FloatingToolbarExitDirection.Bottom
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // Check if screen is wide (Tablet/Foldable)
            val isWideScreen = remember {
                windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
            }
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { height -> height } + fadeIn(),
                exit = slideOutVertically { height -> height } + fadeOut()
            ) {
                // Container for the Floating Toolbar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = cutoutInsets.calculateStartPadding(layoutDirection),
                            end = cutoutInsets.calculateEndPadding(layoutDirection)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        scrollBehavior = toolbarScrollBehavior,
                        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
                        modifier = Modifier
                            .padding(
                                // Add bottom padding to avoid navigation bar overlap
                                bottom = systemBarsInsets.calculateBottomPadding() + ScreenOffset,
                                top = ScreenOffset
                            )
                            .zIndex(1f) // Ensure it floats above content
                    ) {
                        // 4. Iterate through your navigation items
                        mainScreens.fastForEach { item ->
                            // Check if this item is the "Active" screen
                            val isSelected = appState.isSelected(item.route)

                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                                tooltip = { PlainTooltip { Text(stringResource(item.label)) } },
                                state = rememberTooltipState(),
                            ) {
                                ToggleButton(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        appState.navigateToTopLevelDestination(item.route)
                                    },
                                    colors = ToggleButtonDefaults.toggleButtonColors(),
                                    shapes = ToggleButtonDefaults.shapes(
                                        CircleShape,
                                        CircleShape,
                                        CircleShape
                                    ),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Crossfade Icon
                                        Crossfade(isSelected, label = "IconFade") { selected ->
                                            Icon(
                                                painter = painterResource(if (selected) item.selectedIcon else item.unselectedIcon),
                                                contentDescription = null
                                            )
                                        }
                                        // Expand Text Label (only when selected or on wide screens)
                                        AnimatedVisibility(
                                            visible = isSelected || isWideScreen,
                                            enter = expandHorizontally(),
                                            exit = shrinkHorizontally()
                                        ) {
                                            Text(
                                                text = stringResource(item.label),
                                                maxLines = 1,
                                                overflow = TextOverflow.Clip,
                                                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { contentPadding ->
        // 5. The Content Area
        SharedTransitionLayout {
            NavDisplay(
                backStack = appState.backStack,
                onBack = appState::goBack,
                // TODO: Add a slide in and out animation
                entryProvider = entryProvider {

                    entry<Screen.Timer> {
                        HomeScreen(contentPadding = contentPadding)
                    }

                    entry<Screen.Stats.Main> {
                        StatsScreen(
                            contentPadding = contentPadding,
                            onNavigateToHistory = { appState.navigateTo(Screen.Stats.History) })
                    }

                    entry<Screen.Stats.History> {
                        HistoryScreen(
                            contentPadding = contentPadding,
                            onBackClick = appState::goBack
                        )
                    }

                    entry<Screen.Settings.Main> {
                        // TODO: Add your SettingsScreen here
                        // SettingsScreen(contentPadding = contentPadding)
                        TypographyShowcase(contentPadding = contentPadding)
                    }
                }
            )
        }
    }
}