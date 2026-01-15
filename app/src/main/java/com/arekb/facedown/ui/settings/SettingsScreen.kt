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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.arekb.facedown.ui.settings.components.LanguageSelectionSheet
import com.arekb.facedown.ui.settings.data.getLanguageDisplayName

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToTimerOptions: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()

    // Local state for the Theme Dialog
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    val currentLanguage = viewModel.getCurrentLanguage()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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

        LazyColumn(
            contentPadding = effectivePadding,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                FaceDownListItem(
                    topText = stringResource(R.string.preferences),
                    icon = R.drawable.settings_alarm,
                    title = stringResource(R.string.timer_options),
                    subtitle = stringResource(R.string.sounds_haptics),
                    position = ItemPosition.Top,
                    onClick = onNavigateToTimerOptions
                )
                FaceDownListItem(
                    icon = R.drawable.settings_palette,
                    title = stringResource(R.string.app_theme),
                    subtitle = when (currentTheme) {
                        AppTheme.SYSTEM -> stringResource(R.string.system_theme)
                        AppTheme.LIGHT -> stringResource(R.string.light)
                        AppTheme.DARK -> stringResource(R.string.dark)
                    },
                    position = ItemPosition.Middle,
                    onClick = { showThemeDialog = true },
                    trailingContent = null
                )
                FaceDownListItem(
                    icon = R.drawable.settings_language,
                    title = stringResource(R.string.app_language),
                    subtitle = getLanguageDisplayName(currentLanguage),
                    position = ItemPosition.Bottom,
                    onClick = { showLanguageSheet = true },
                    trailingContent = null
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                FaceDownListItem(
                    topText = stringResource(R.string.support),
                    icon = R.drawable.settings_bug,
                    title = stringResource(R.string.report_a_bug),
                    subtitle = stringResource(R.string.found_an_issue_let_me_know),
                    position = ItemPosition.Top,
                    onClick = {
                        uriHandler.openUri("https://forms.gle/acgUU9GxcHfQTuEG8")
                    },
                    trailingContent = null
                )
                FaceDownListItem(
                    icon = R.drawable.settings_translate,
                    title = stringResource(R.string.help_with_translation),
                    subtitle = stringResource(R.string.make_facedown_accessible_to_everyone),
                    position = ItemPosition.Bottom,
                    onClick = {
                        @Suppress("HardCodedStringLiteral")
                        launchEmailIntent(
                            context = context,
                            email = "arek.siwak44@gmail.com",
                            subject = "Contributing to FaceDown Translation"
                        )
                    },
                    trailingContent = null
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                FaceDownListItem(
                    topText = stringResource(R.string.data_management),
                    icon = R.drawable.settings_data,
                    title = stringResource(R.string.data),
                    subtitle = stringResource(R.string.export_delete),
                    position = ItemPosition.Single,
                    onClick = onNavigateToData
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                FaceDownListItem(
                    topText = stringResource(R.string.about_facedown),
                    icon = R.drawable.settings_info,
                    title = stringResource(R.string.about),
                    subtitle = stringResource(R.string.version, BuildConfig.VERSION_NAME),
                    position = ItemPosition.Single,
                    onClick = onNavigateToAbout
                )
            }
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

    // --- LANGUAGE SHEET ---
    if (showLanguageSheet) {
        LanguageSelectionSheet(
            currentLanguageCode = currentLanguage,
            onLanguageSelected = { code ->
                viewModel.setAppLocale(code)
                showLanguageSheet = false
            },
            onDismissRequest = { showLanguageSheet = false }
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
        title = { Text(stringResource(R.string.choose_theme)) },
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
                                AppTheme.SYSTEM -> stringResource(R.string.system_theme)
                                AppTheme.LIGHT -> stringResource(R.string.light)
                                AppTheme.DARK -> stringResource(R.string.dark)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        }
    )
}