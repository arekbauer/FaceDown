package com.arekb.facedown.ui.stats

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.arekb.facedown.ui.stats.components.SessionCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryScreen(
    contentPadding: PaddingValues,
    viewModel: StatsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val pagingItems = viewModel.historyPagingFlow.collectAsLazyPagingItems()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val layoutDirection = LocalLayoutDirection.current

    val iconShapes = remember {
        listOf(
            MaterialShapes.SoftBurst,
            MaterialShapes.Gem,
            MaterialShapes.Pill,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Flower
        )
    }

    Scaffold(
        bottomBar = { Spacer(Modifier.height(contentPadding.calculateBottomPadding())) },
        topBar = {
            TopAppBar(
                title = { Text("Session History", style = MaterialTheme.typography.headlineSmallEmphasized) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->

        val effectivePadding =
            PaddingValues(
                top = innerPadding.calculateTopPadding(),
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = contentPadding.calculateBottomPadding()
            )

        LazyColumn(
            contentPadding = effectivePadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            //horizontalAlignment = Alignment.CenterHorizontally
        ) {

            items(
                count = pagingItems.itemCount,
                // Paging 3 key handling
                key = { index ->
                    when (val item = pagingItems[index]) {
                        is HistoryItem.SessionItem -> "session_${item.session.id}"
                        is HistoryItem.Header -> "header_${item.title}"
                        null -> "null_$index"
                    }
                },
                contentType = { index ->
                    when (pagingItems[index]) {
                        is HistoryItem.SessionItem -> "session"
                        is HistoryItem.Header -> "header"
                        null -> null
                    }
                }
            ) { index ->
                when (val item = pagingItems[index]) {
                    is HistoryItem.Header -> {
                        // Render the Date Divider
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                    is HistoryItem.SessionItem -> {
                        val currentShape = iconShapes[(index * 2) % iconShapes.size]
                        SessionCard(
                            session = item.session,
                            shape = currentShape.toShape(),
                        )
                    }
                    null -> {
                        // Placeholders during loading (optional)
                    }
                }
            }

            // Optional: Handle Loading & Error States at the bottom
            /* when (pagingItems.loadState.append) {
                is LoadState.Loading -> item { CircularProgressIndicator(...) }
                is LoadState.Error -> item { Text("Error loading more") }
                else -> {}
            }
            */
        }
    }
}