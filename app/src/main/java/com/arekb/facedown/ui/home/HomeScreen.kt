package com.arekb.facedown.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arekb.facedown.R
import com.arekb.facedown.data.timer.ServiceConstants
import com.arekb.facedown.data.timer.ServiceConstants.GRACE_LIMIT
import com.arekb.facedown.data.timer.ServiceConstants.STARTING_COUNTDOWN
import com.arekb.facedown.domain.model.TimerState
import com.arekb.facedown.ui.formatTime
import com.arekb.facedown.ui.home.components.InfoPill
import com.arekb.facedown.ui.home.components.PresetButtonGroup
import com.arekb.facedown.ui.home.components.SimpleFlowingArrows
import com.arekb.facedown.ui.home.components.TimerControlBar
import com.arekb.facedown.ui.home.components.TimerDisplay
import com.arekb.facedown.ui.sendTimerCommand
import com.arekb.facedown.ui.theme.FaceDownTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val layoutDirection = LocalLayoutDirection.current

    // --- 1. STATE TRACKING ---
    var isDndGranted by remember { mutableStateOf(false) }
    var isNotificationGranted by remember { mutableStateOf(false) }
    var showDndDialog by remember { mutableStateOf(false) }

    // Helper to refresh permission state
    fun checkPermissions() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        isDndGranted = notificationManager.isNotificationPolicyAccessGranted

        isNotificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission implicitly granted on older Android versions
        }
    }

    // --- 2. THE PERMISSION LAUNCHERS ---

    // Launcher for Notification Permission (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            isNotificationGranted = isGranted
            // Optional: If granted, you could auto-trigger the next check,
            // but letting the user tap "Start" again is safer/simpler UX.
        }
    )

    // Lifecycle Observer: Re-check permissions when app resumes (returning from Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val timerState by viewModel.timerState.collectAsState()
    val selectedDuration by viewModel.selectedDuration.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.toolbar_timer_label), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineMediumEmphasized) },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent, // Let the Timer background show through

    ) { innerPadding ->

        val effectivePadding = remember(timerState, contentPadding) {
            val bottomPadding = if (timerState is TimerState.Idle) {
                contentPadding.calculateBottomPadding() + innerPadding.calculateBottomPadding()
            } else {
                0.dp
            }
            PaddingValues(
                top = contentPadding.calculateTopPadding() + innerPadding.calculateTopPadding(),
                start = contentPadding.calculateStartPadding(layoutDirection) + 32.dp,
                end = contentPadding.calculateEndPadding(layoutDirection) + 32.dp,
                bottom = bottomPadding
            )
        }

        TimerSessionView(
            state = timerState,
            layoutPadding = effectivePadding,
            selectedDuration = selectedDuration,
            onDurationChange = { viewModel.setDuration(it) },
            onStartClicked = { minutes ->
                // --- THE GAUNTLET ---

                // Check 1: Notifications (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isNotificationGranted) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@TimerSessionView // Stop here
                }

                // Check 2: Do Not Disturb
                if (!isDndGranted) {
                    showDndDialog = true
                    return@TimerSessionView // Stop here
                }

                // Check 3: All Good -> Start!
                sendTimerCommand(context, ServiceConstants.ACTION_START, minutes)
            },
            onReset = {
                sendTimerCommand(context, ServiceConstants.ACTION_RESET)
            },
            onSaveClicked = { minutes, tag, note ->
                viewModel.saveSession(minutes, tag, note)
            }
        )
    }

    // --- 4. THE DND DIALOG ---
    if (showDndDialog) {
        AlertDialog(
            onDismissRequest = { showDndDialog = false },
            title = { Text("Permission Required") },
            text = {
                Text("To automatically silence notifications while you focus, FaceDown needs 'Do Not Disturb' access.\n\nPlease grant this permission on the next screen.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDndDialog = false
                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDndDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerSessionView(
    state: TimerState,
    layoutPadding: PaddingValues = PaddingValues(0.dp),
    selectedDuration: Int = 15,
    onDurationChange: (Int) -> Unit = {},
    onStartClicked: (Int) -> Unit,
    onReset: () -> Unit,
    onSaveClicked: (Int, String, String?) -> Unit
) {
    val context = LocalContext.current

    val formattedEndTime = remember(selectedDuration) {
        val now = LocalTime.now()
        val endTime = now.plusMinutes(selectedDuration.toLong())
        endTime.format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    var showStopDialog by remember { mutableStateOf(false) }
    val onDialogDismiss = { showStopDialog = false
        if (state is TimerState.GracePeriod) sendTimerCommand(context, ServiceConstants.ACTION_CANCEL_STOP)
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text(stringResource(R.string.end_session_title)) },
            text = { Text(stringResource(R.string.end_session_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        showStopDialog = false
                        sendTimerCommand(context, ServiceConstants.ACTION_RESET)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.end_session_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDialogDismiss
                ) {
                    Text(stringResource(R.string.end_session_cancel))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(layoutPadding)
        ){
            // --- STATE MACHINE UI ---
            when (state) {
                is TimerState.Idle -> {
                    val progress = (selectedDuration / 60f).coerceIn(0f, 1f)
                    TimerDisplay(
                        progress = progress,
                        mainText = "$selectedDuration",
                        secondaryText = stringResource(R.string.minutes),
                        progressAnimationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    InfoPill(
                        icon = R.drawable.icons_schedule_outline,
                        string = stringResource(R.string.ends_at) + " " + formattedEndTime
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    PresetButtonGroup(
                        presets = listOf(5, 10, 15, 25),
                        currentDuration = selectedDuration,
                        onDurationChange = onDurationChange,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PresetButtonGroup(
                        presets = listOf(30, 45, 50, 60),
                        currentDuration = selectedDuration,
                        onDurationChange = onDurationChange,
                    )

                    Spacer(modifier = Modifier.height(52.dp))

                    FilledTonalButton(
                        onClick = { onStartClicked(selectedDuration) },
                        modifier = Modifier
                            .height(80.dp)
                            .widthIn(min = 224.dp, max = 300.dp),
                        shape = MaterialTheme.shapes.extraLargeIncreased
                    ) {
                        Text(
                            text = stringResource(R.string.start_focus),
                            fontSize = 18.sp
                        )
                    }
                }

                is TimerState.Startup -> {
                    val progress = (state.countdownSeconds / STARTING_COUNTDOWN.toFloat()).coerceIn(0f, 1f)
                    TimerDisplay(
                        progress = progress,
                        mainText = "${state.countdownSeconds}",
                        secondaryText = stringResource(R.string.seconds),
                        progressAnimationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    InfoPill(
                        icon = R.drawable.icons_schedule_outline,
                        string = selectedDuration.toString() + " " + stringResource(R.string.min_session)
                    )

                    Spacer(modifier = Modifier.height(64.dp))

                    Text(
                        text = stringResource(R.string.start_focus),
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    SimpleFlowingArrows(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is TimerState.Running -> {
                    val progress = (state.remainingSeconds / (selectedDuration * 60).toFloat()).coerceIn(0f, 1f)

                    TimerDisplay(
                        progress = progress,
                        mainText = formatTime(state.remainingSeconds),
                        secondaryText = stringResource(R.string.remaining),
                        mainTextSize = 72.sp,
                        progressAnimationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    InfoPill(
                        icon = R.drawable.icon_pause_filled,
                        string = stringResource(R.string.lift_to_pause)
                    )

                    Spacer(modifier = Modifier.height(64.dp))

                    Text(
                        text = stringResource(R.string.timer_in_progress),
                        style = MaterialTheme.typography.titleLarge,
                    )

//                    Spacer(modifier = Modifier.weight(1f))
//
//                    OutlinedButton(
//                        onClick = onReset,
//                        colors = ButtonDefaults.outlinedButtonColors(
//                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
//                            containerColor = MaterialTheme.colorScheme.errorContainer
//                        ),
//                        modifier = Modifier
//                            .navigationBarsPadding()
//                            .padding(bottom = 48.dp)
//                    ) {
//                        Text("Stop session")
//                    }
                }

                is TimerState.GracePeriod -> {
                    val progress = (state.remainingGraceSeconds / GRACE_LIMIT.toFloat()).coerceIn(0f, 1f)
                    TimerDisplay(
                        progress = progress,
                        mainText = "${state.remainingGraceSeconds}",
                        secondaryText = stringResource(R.string.fail_in),
                        progressAnimationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                        colour = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(120.dp))

                    Text(
                        text = stringResource(R.string.keep_focusing),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.flip_phone_resume),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(48.dp))

                    SimpleFlowingArrows(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    TimerControlBar(
                        isPaused = false ,
                        onPauseResume = { sendTimerCommand(context, ServiceConstants.ACTION_PAUSE) },
                        onStop = { showStopDialog = true
                            sendTimerCommand(context, ServiceConstants.ACTION_TEMP_FREEZE)
                        }
                    )
                }

                is TimerState.Paused -> {
                    val progress = (state.remainingSeconds / (selectedDuration * 60).toFloat()).coerceIn(0f, 1f)
                    TimerDisplay(
                        progress = progress,
                        mainText = formatTime(state.remainingSeconds),
                        secondaryText = stringResource(R.string.remaining),
                        progressAnimationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                        mainTextSize = 72.sp,
                        colour = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    InfoPill(
                        icon = R.drawable.icon_dnd_off_filled,
                        string = stringResource(R.string.dnd_off)
                    )

                    Spacer(modifier = Modifier.height(64.dp))

                    Text(
                        text = stringResource(R.string.session_paused),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.continue_ready),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    TimerControlBar(
                        isPaused = true ,
                        onPauseResume = { sendTimerCommand(context, ServiceConstants.ACTION_RESUME) },
                        onStop = { showStopDialog = true }
                    )
                }

                is TimerState.Failed -> {
                    Text(
                        text = "Session Broken",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onReset,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Return Home", color = Color.Red)
                    }
                }

                is TimerState.Completed -> {
                    // 1. Form State (Temporary, lives only while on this screen)
                    var noteText by remember { mutableStateOf("") }
                    var selectedTag by remember { mutableStateOf("Zen") }
                    val tags = listOf("Work", "Study", "Code", "Read", "Zen")

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = "Session Complete!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Well done.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // 2. The Note Input
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("Add a note (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 3. The Tag Cloud (FlowRow wraps items to next line)
                        // Note: FlowRow is experimental in some versions, simple Row/Column works for MVP too
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tags.forEach { tag ->
                                val isSelected = (tag == selectedTag)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTag = tag },
                                    label = { Text(tag) },
                                    modifier = Modifier.padding(4.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color.White,
                                        selectedLabelColor = Color(0xFF2196F3), // Match Blue Theme
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // 4. The Save Button
                        Button(
                            onClick = {
                                val minutes = state.totalDurationMinutes
                                onSaveClicked(minutes, selectedTag, noteText.ifBlank { null })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Save Session",
                                color = Color(0xFF2196F3),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Option to discard
                        TextButton(onClick = onReset) {
                            Text("Discard", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerPreviewWrapper(content: @Composable () -> Unit) {
    FaceDownTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

// 1. IDLE STATE (The Setup Screen)
@Preview(name = "1. Idle State", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewTimerSession_Idle() {
    TimerPreviewWrapper {
        TimerSessionView(
            state = TimerState.Idle,
            selectedDuration = 25,
            layoutPadding = PaddingValues(16.dp),
            onDurationChange = {},
            onStartClicked = {},
            onReset = {},
            onSaveClicked = { _, _, _ -> }
        )
    }
}

// 2. STARTUP STATE (The Countdown/Flip Instruction)
@Preview(name = "2. Startup State", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewTimerSession_Startup() {
    TimerPreviewWrapper {
        TimerSessionView(
            // Assuming Startup takes a countdown integer
            state = TimerState.Startup(countdownSeconds = 3),
            selectedDuration = 25,
            onDurationChange = {},
            onStartClicked = {},
            onReset = {},
            onSaveClicked = { _, _, _ -> }
        )
    }
}

// 3. RUNNING STATE (Focus Mode)
@Preview(name = "3. Running State", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewTimerSession_Running() {
    TimerPreviewWrapper {
        TimerSessionView(
            // Assuming Running takes remaining seconds (e.g., 14m 30s left)
            state = TimerState.Running(remainingSeconds = 870, totalSeconds = 900, currentProgress = 0.7f),
            selectedDuration = 25,
            onDurationChange = {},
            onStartClicked = {},
            onReset = {},
            onSaveClicked = { _, _, _ -> }
        )
    }
}

// 4. COMPLETED STATE (Summary & Tagging)
@Preview(name = "4. Completed State", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewTimerSession_Completed() {
    TimerPreviewWrapper {
        TimerSessionView(
            // Assuming Completed takes the total duration focused
            state = TimerState.Completed(totalDurationMinutes = 25),
            selectedDuration = 25,
            onDurationChange = {},
            onStartClicked = {},
            onReset = {},
            onSaveClicked = { _, _, _ -> }
        )
    }
}

// 5. GRACE PERIOD (Warning State)
@Preview(name = "5. Grace Period", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewTimerSession_GracePeriod() {
    TimerPreviewWrapper {
        TimerSessionView(
            state = TimerState.GracePeriod(remainingGraceSeconds = 4, originalRemainingSeconds = 870),
            selectedDuration = 25,
            onDurationChange = {},
            onStartClicked = {},
            onReset = {},
            onSaveClicked = { _, _, _ -> }
        )
    }
}

// 5. PAUSE STATE
@Preview(name = "6. Paused Period", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewTimerSession_Paused() {
    TimerPreviewWrapper {
        TimerSessionView(
            state = TimerState.Paused(remainingSeconds = 870, totalSeconds = 900, currentProgress = 0.7f),
            selectedDuration = 25,
            onDurationChange = {},
            onStartClicked = {},
            onReset = {},
            onSaveClicked = { _, _, _ -> }
        )
    }
}