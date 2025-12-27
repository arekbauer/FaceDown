package com.arekb.facedown.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arekb.facedown.R
import com.arekb.facedown.ui.stats.components.StatsHeroCard
import com.arekb.facedown.ui.theme.FaceDownTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val layoutDirection = LocalLayoutDirection.current

    val streak by viewModel.currentStreak.collectAsState()
    val totalMinutes by viewModel.totalFocusMinutes.collectAsState()

    val formattedTime = remember(totalMinutes) {
        val hours: Int = totalMinutes / 60
        val minutes: Int = totalMinutes % 60
        if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.toolbar_stats_label), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineMediumEmphasized) },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,

    ) { innerPadding ->

        val effectivePadding =
            PaddingValues(
                top = contentPadding.calculateTopPadding() + innerPadding.calculateTopPadding(),
                start = contentPadding.calculateStartPadding(layoutDirection) + 32.dp,
                end = contentPadding.calculateEndPadding(layoutDirection) + 32.dp,
                bottom = contentPadding.calculateBottomPadding() + innerPadding.calculateBottomPadding()
            )

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(effectivePadding)
            ) {

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
            StatsScreen(contentPadding = PaddingValues(16.dp))
        }
    }
}