package com.arekb.facedown.ui.settings.subscreens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arekb.facedown.R
import com.arekb.facedown.ui.getRingtoneName
import com.arekb.facedown.ui.settings.SettingsViewModel
import com.arekb.facedown.ui.settings.components.FaceDownListItem
import com.arekb.facedown.ui.settings.components.FaceDownSwitchItem
import com.arekb.facedown.ui.settings.components.ItemPosition
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerOptionsScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe Data
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsStateWithLifecycle()
    val isHapticsEnabled by viewModel.isHapticsEnabled.collectAsStateWithLifecycle()
    val currentSoundUri by viewModel.currentSoundUri.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // State for the Sound Name text (e.g. "Oxygen")
    var soundName by remember { mutableStateOf("Default") }

    // Load sound name whenever URI changes
    LaunchedEffect(currentSoundUri) {
        soundName = getRingtoneName(context, currentSoundUri)
    }

    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? =
                result.data?.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                    Uri::class.java
                )

            if (uri != null) {
                try {
                    // Remember that I have permission to read this file after reboot
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // We ignore errors here because system ringtones don't need it
                }
            }

            viewModel.updateTimerSound(uri?.toString())
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = { Spacer(Modifier.height(contentPadding.calculateBottomPadding())) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Timer options") },
                subtitle = { Text(stringResource(R.string.toolbar_settings_label)) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = contentPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Master Switch
            FaceDownSwitchItem(
                topText = "Configurations",
                title = "Play sound",
                subtitle = "When timer finishes",
                icon = R.drawable.settings_timer_sound,
                checked = isSoundEnabled,
                onCheckedChange = { viewModel.toggleSound(it) },
                position = ItemPosition.Top
            )

            // 2. Ringtone Picker (Disabled if sound is off)
            // We use standard ListItem if we want the chevron, or customize it
            FaceDownListItem(
                title = "Alarm tone",
                subtitle = if (isSoundEnabled) soundName else "Sound disabled",
                icon = R.drawable.settings_alarm,
                position = ItemPosition.Bottom,
                onClick = {
                    if (isSoundEnabled) {
                        try {
                            // Launch System Picker
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                                    RingtoneManager.TYPE_ALARM
                                )
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)

                                // Pre-select current sound
                                val existingUri = currentSoundUri?.toUri()
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
                            }
                            ringtoneLauncher.launch(intent)
                        } catch (_: ActivityNotFoundException) {
                            // The user tried to do something, and the system failed
                            scope.launch {
                                snackbarHostState.showSnackbar("Cannot open ringtone picker")
                            }
                        } catch (_: Exception) {
                            // Generic safety net
                            scope.launch {
                                snackbarHostState.showSnackbar("Something went wrong")
                            }
                        }
                    }
                    else {
                        Toast.makeText(context, "Turn on sound to pick a tone", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            FaceDownSwitchItem(
                topText = "Haptics",
                title = "Vibration",
                subtitle = "Vibrate when timer finishes",
                icon = R.drawable.settings_timer_vibrate,
                checked = isHapticsEnabled,
                onCheckedChange = { viewModel.toggleHaptics(it) },
                position = ItemPosition.Single
            )
        }
    }
}