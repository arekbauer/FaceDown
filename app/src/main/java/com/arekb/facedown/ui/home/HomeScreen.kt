package com.arekb.facedown.ui.home

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arekb.facedown.data.timer.FocusTimerService
import com.arekb.facedown.data.timer.ServiceConstants
import com.arekb.facedown.domain.model.OrientationState
import com.arekb.facedown.domain.model.TimerState

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    // --- 3. THE UI ---

    val timerState by viewModel.timerState.collectAsState()

    // Note: We removed the "if (permission) { ... } else { Block }" check.
    // The UI is always visible now. Permissions are checked only on interaction.
    TimerSessionView(
        state = timerState,
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

@Composable
fun TimerSessionView(
    state: TimerState,
    onStartClicked: (Int) -> Unit,
    onReset: () -> Unit,
    onSaveClicked: (Int, String, String?) -> Unit
) {
    val context = LocalContext.current

    // Smooth color transitions based on state
    val targetColor = when (state) {
        is TimerState.Idle -> MaterialTheme.colorScheme.surface
        is TimerState.Startup -> MaterialTheme.colorScheme.secondary
        is TimerState.Running -> Color(0xFF4CAF50) // Calm Green
        is TimerState.GracePeriod -> Color(0xFFFF9800) // Panic Orange
        is TimerState.Paused -> Color(0xFF81C784) // Resume Green
        is TimerState.Failed -> Color(0xFFF44336) // Failure Red
        is TimerState.Completed -> Color(0xFF2196F3) // Success Blue
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "BgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // --- STATE MACHINE UI ---
            when (state) {
                is TimerState.Idle -> {
                    Text(
                        text = "FaceDown",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { onStartClicked(1) },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Start 1 Min Focus Test")
                    }
                }

                is TimerState.Startup -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Get Ready",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // A visual instruction
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Black.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Place phone face down in:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black.copy(alpha = 0.8f)
                        )

                        Text(
                            text = state.countdownSeconds.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                is TimerState.Running -> {
                    Text(
                        text = formatTime(state.remainingSeconds),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Focusing...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                is TimerState.GracePeriod -> {
                    Text(
                        text = "PUT IT DOWN!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Big Red Warning Circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.remainingGraceSeconds.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White
                        )
                    }
                    // Add Pause Button
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            sendTimerCommand(context, ServiceConstants.ACTION_PAUSE)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Emergency Pause", color = Color.Black)
                    }
                }
                is TimerState.Paused -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Session Paused",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Black
                        )
                        Text(
                            text = "DND is OFF. You can take calls.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = formatTime(state.remainingSeconds),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                sendTimerCommand(context, ServiceConstants.ACTION_RESUME)
                            }
                        ) {
                            Text("Resume Session")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = onReset) {
                            Text("Abandon Session")
                        }
                    }
                }

                is TimerState.Failed -> {
                    Text(
                        text = "Session Broken",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
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
                            modifier = Modifier.fillMaxWidth().height(56.dp)
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

// Helper: Format Seconds to MM:SS
fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

fun sendTimerCommand(context: Context, action: String, minutes: Int = 0) {
    val intent = Intent(context, FocusTimerService::class.java).apply {
        this.action = action
        if (minutes > 0) putExtra("DURATION", minutes)
    }
    // Only use startForegroundService for the actual START command
    if (action == ServiceConstants.ACTION_START && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

// --- Sub-Component: Permission Request ---
@Composable
fun PermissionRequestView(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Permission Needed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To function as a strict focus timer, FaceDown needs permission to control 'Do Not Disturb' automatically.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                // Launch System Settings for DND Access
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
        ) {
            Text("Grant Access")
        }
    }
}

// --- Sub-Component: Sensor Status (Debug UI) ---
@Composable
fun SensorStatusView(state: OrientationState) {
    // Dynamic background color based on state for immediate visual feedback
    val backgroundColor = when (state) {
        OrientationState.FACE_DOWN -> Color(0xFF4CAF50) // Green
        OrientationState.FACE_UP -> Color(0xFFF44336)   // Red
        OrientationState.UNKNOWN -> Color.Gray
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "CURRENT STATE",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.name,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (state == OrientationState.FACE_DOWN)
                    "DND is ON" else "DND is OFF",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}