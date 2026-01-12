package com.arekb.facedown.ui.stats

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.arekb.facedown.R
import com.arekb.facedown.ui.stats.components.EmptyHistoryMessage
import com.arekb.facedown.ui.stats.components.SessionCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                title = { Text(stringResource(R.string.session_history), style = MaterialTheme.typography.headlineSmallEmphasized) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
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
            val refreshState = pagingItems.loadState.refresh

            // Only show if we are NOT loading and the list is actually empty
            if (refreshState is LoadState.NotLoading && pagingItems.itemCount == 0) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyHistoryMessage()
                    }
                }
            }

            @Suppress("HardCodedStringLiteral")
            items(
                count = pagingItems.itemCount,
                // Paging 3 key handling
                key = { index ->
                    // Stable keys are important for Paging
                    val item = pagingItems[index]
                    if (item is HistoryItem.SessionItem) item.session.id else "header_$index"
                }
            ) { index ->
                when (val item = pagingItems[index]) {
                    is HistoryItem.Header -> {
                        // Render the Date Divider
                        Text(
                            text = getHeaderText(item.type),
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

            // Error Handling (if any)
            val appendState = pagingItems.loadState.append

            if (appendState is LoadState.Error) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = { pagingItems.retry() }) {
                            Text(
                                text = stringResource(R.string.error_retry_msg),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getHeaderText(type: HistoryHeaderType): String {
    return when (type) {
        HistoryHeaderType.Today -> stringResource(R.string.today)
        HistoryHeaderType.Yesterday -> stringResource(R.string.yesterday)
        is HistoryHeaderType.MonthYear -> {
            // "October 2024" -> Internationalized
            val locale = Locale.getDefault()
            val pattern = DateFormat.getBestDateTimePattern(locale, "MMMMy")
            val formatter = DateTimeFormatter.ofPattern(pattern, locale)

            // Reconstruct a date (e.g., 1st of that month) to format it
            val date = LocalDate.of(type.year, type.month, 1)
            date.format(formatter)
        }
    }
}