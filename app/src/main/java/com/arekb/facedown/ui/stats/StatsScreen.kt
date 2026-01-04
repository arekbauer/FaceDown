package com.arekb.facedown.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arekb.facedown.BuildConfig
import com.arekb.facedown.R
import com.arekb.facedown.ui.stats.components.AnimatedWeeklyChart
import com.arekb.facedown.ui.stats.components.ConsistencyCard
import com.arekb.facedown.ui.stats.components.SessionCard
import com.arekb.facedown.ui.stats.components.StatsHeroCard
import com.arekb.facedown.ui.theme.FaceDownTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    contentPadding: PaddingValues,
    onNavigateToHistory: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current

    val streak by viewModel.currentStreak.collectAsState()
    val totalMinutes by viewModel.totalFocusMinutes.collectAsState()
    val sessions by viewModel.recentSessions.collectAsStateWithLifecycle()

    val formattedTime = remember(totalMinutes) {
        val hours: Int = totalMinutes / 60
        val minutes: Int = totalMinutes % 60
        if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    val iconShape = remember {
        listOf(
            MaterialShapes.SoftBurst,
            MaterialShapes.Gem,
            MaterialShapes.Pill,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Flower
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { Spacer(Modifier.height(contentPadding.calculateBottomPadding())) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.toolbar_stats_label), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineMediumEmphasized)},
                actions = {
                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = viewModel::injectSessions
                        ) {
                            Spacer(Modifier.size(24.dp))
                        }
                    }
                },
            )
        },

    ) { innerPadding ->

        val effectivePadding =
            PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = contentPadding.calculateBottomPadding()
            )

        LazyColumn(
            contentPadding = effectivePadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    StatsHeroCard(
                        icon = R.drawable.icon_fire_filled,
                        value = "$streak",
                        label = "Day streak",
                        containerColour = MaterialTheme.colorScheme.primaryContainer,
                        contentColour = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = MaterialShapes.Pill.toShape(),
                        modifier = Modifier.weight(1f)
                    )

                    StatsHeroCard(
                        icon = R.drawable.icon_hourglass_filled,
                        value = formattedTime,
                        label = "Total focus",
                        containerColour = MaterialTheme.colorScheme.secondaryContainer,
                        contentColour = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = MaterialShapes.SoftBurst.toShape(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                ConsistencyCard(weeks = viewModel.heatmapState.collectAsState().value)
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                val weeklyData by viewModel.weeklyStats.collectAsState()
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Weekly Snapshot",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    AnimatedWeeklyChart(weekData = weeklyData)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Row(modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Sessions",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onNavigateToHistory
                    ) {
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            itemsIndexed(
                items = sessions,
                key = { _, session -> session.id}
            ) { index, session ->
                val currentShape = iconShape[index % iconShape.size]

                SessionCard(
                    session = session,
                    shape = currentShape.toShape()
                )
            }
        }
    }
}

@Preview(name = "Stats Screen", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun StatsScreenPreview()
{
    FaceDownTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StatsScreen(contentPadding = PaddingValues(16.dp), onNavigateToHistory = {})
        }
    }
}