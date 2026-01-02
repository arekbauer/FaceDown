package com.arekb.facedown.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.arekb.facedown.ui.stats.components.SessionCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    // 1. Collect the Paging Data
    val pagingItems = viewModel.historyPagingFlow.collectAsLazyPagingItems()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // 2. Define your shapes list (Mocking the list you mentioned)
    val iconShapes = remember {
        listOf(
            CircleShape,
            RoundedCornerShape(16.dp),
            CutCornerShape(topEnd = 24.dp),
            RoundedCornerShape(4.dp),
            RoundedCornerShape(4.dp)
        )
    }

    Scaffold(
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
    ) { padding ->
        // 3. The List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Gap between cards
        ) {

            // Paging 3 Item Handling
            items(
                count = pagingItems.itemCount,
                key = pagingItems.itemKey { it.id } // Stable IDs for performance
            ) { index ->
                val session = pagingItems[index]

                if (session != null) {
                    // Logic to cycle through shapes based on index
                    val currentShape = iconShapes[index % iconShapes.size]

                    SessionCard(
                        session = session,
                        shape = currentShape,
                        onClick = { /* TODO: Open Details */ }
                    )
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