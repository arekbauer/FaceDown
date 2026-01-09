package com.arekb.facedown.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arekb.facedown.BuildConfig
import com.arekb.facedown.R
import com.arekb.facedown.data.settings.AppTheme
import com.arekb.facedown.ui.launchEmailIntent
import com.arekb.facedown.ui.settings.components.FaceDownListItem
import com.arekb.facedown.ui.settings.components.ItemPosition

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToTimerOptions: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Observe Theme for the subtitle
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()

    // Local state for the Theme Dialog
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        bottomBar = { Spacer(Modifier.height(contentPadding.calculateBottomPadding())) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.toolbar_settings_label), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineMediumEmphasized) },
            )
        }
    ) { innerPadding ->

        val effectivePadding =
            PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = contentPadding.calculateBottomPadding()
            )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(effectivePadding)
                .verticalScroll(rememberScrollState())
        ) {
            FaceDownListItem(
                topText = "Preferences",
                icon = R.drawable.settings_alarm,
                title = "Timer options",
                subtitle = "Sounds, haptics",
                position = ItemPosition.Top, // <--- Top of the group
                onClick = onNavigateToTimerOptions
            )

            FaceDownListItem(
                icon = R.drawable.settings_palette,
                title = "App theme",
                subtitle = when (currentTheme) {
                    AppTheme.SYSTEM -> "Default"
                    AppTheme.LIGHT -> "Light"
                    AppTheme.DARK -> "Dark"
                },
                position = ItemPosition.Bottom, // <--- Bottom of the group
                onClick = { showThemeDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            FaceDownListItem(
                topText = "Support",
                icon = R.drawable.settings_bug,
                title = "Report a bug",
                subtitle = "Found an issue? Let us know",
                position = ItemPosition.Top,
                onClick = {
                    // Replace with your actual Google Form URL
                    uriHandler.openUri("https://forms.gle/YOUR_FORM_ID")
                }
            )
            FaceDownListItem(
                icon = R.drawable.settings_translate,
                title = "Help with translation",
                subtitle = "Make FaceDown accessible to everyone",
                position = ItemPosition.Bottom,
                onClick = {
                    launchEmailIntent(
                        context = context,
                        email = "hello@facedown.app", // Your email
                        subject = "Contributing to FaceDown Translation"
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            FaceDownListItem(
                topText = "Data management",
                icon = R.drawable.settings_data,
                title = "Data",
                subtitle = "Clear history, export",
                position = ItemPosition.Single, // <--- Standalone bubble
                onClick = onNavigateToAbout
            )
            Spacer(modifier = Modifier.height(24.dp))

            FaceDownListItem(
                topText = "About Facedown",
                icon = R.drawable.settings_info,
                title = "About",
                subtitle = "Version " + BuildConfig.VERSION_NAME,
                position = ItemPosition.Single, // <--- Standalone bubble
                onClick = onNavigateToAbout
            )
        }
    }

    // --- THEME DIALOG ---
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { viewModel.updateTheme(it) },
            onDismissRequest = { showThemeDialog = false }
        )
    }
}

// Simple Radio Button Dialog
@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Choose theme") },
        text = {
            Column {
                AppTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onThemeSelected(theme)
                                onDismissRequest() // Close on select
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = null // Handled by Row
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (theme) {
                                AppTheme.SYSTEM -> "System default"
                                AppTheme.LIGHT -> "Light"
                                AppTheme.DARK -> "Dark"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        }
    )
}