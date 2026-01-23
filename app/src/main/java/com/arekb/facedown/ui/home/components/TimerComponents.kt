package com.arekb.facedown.ui.home.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arekb.facedown.R


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerDisplay(
    progress: Float,
    mainText: String,
    secondaryText: String,
    progressAnimationSpec: AnimationSpec<Float>,
    mainTextSize: TextUnit = 96.sp,
    colour: Color = MaterialTheme.colorScheme.primary,
    trackColour : Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .widthIn(max = 280.dp)
            .aspectRatio(1f)
    ) {
        CustomWavyIndicator(
            progressFactor = progress,
            animationSpec = progressAnimationSpec,
            colour = colour,
            trackColour = trackColour
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = mainText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = mainTextSize,
                maxLines = 1
            )
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.offset(y = -(8.dp))
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

// TODO: Look into making it take up less height
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomWavyIndicator(progressFactor: Float, animationSpec: AnimationSpec<Float>, colour: Color, trackColour: Color) {
    val thickStrokeWidth = with(LocalDensity.current) { 12.dp.toPx() }
    val thickStroke =
        remember(thickStrokeWidth) {
            Stroke(
                width = thickStrokeWidth,
                cap = StrokeCap.Round
            )
        }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFactor,
        animationSpec = animationSpec,
        label = "WaveProgress"
    )

    // 1. The Timer
    CircularWavyProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxSize(),
        wavelength = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
        waveSpeed = WavyProgressIndicatorDefaults.LinearDeterminateWavelength / 2,
        stroke = thickStroke,
        trackStroke = thickStroke,
        color = colour,
        trackColor = trackColour
    )
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PresetButtonGroup(
    presets: List<Int>,
    currentDuration: Int,
    onDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Calculate height once based on screen size (or pass it in)
    val config = LocalConfiguration.current
    val responsiveHeight = (config.screenHeightDp.dp * 0.05f).coerceIn(56.dp, 88.dp)
    val interactionSources = remember(presets) { presets.map { MutableInteractionSource() } }

    ButtonGroup(
        modifier = modifier
            .widthIn(max = 500.dp),
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        }
    ) {
        presets.forEachIndexed { index, mins ->
            val interactionSource = interactionSources[index]

            customItem(
                {
                    ToggleButton(
                        checked = (currentDuration == mins),
                        onCheckedChange = { onDurationChange(mins) },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .weight(1f)
                            .height(responsiveHeight)
                            .semantics { role = Role.RadioButton }
                            .animateWidth(interactionSource = interactionSource),
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            presets.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                    ) {
                        Text("$mins", maxLines = 1)
                    }
                }
            ) {
                // State ->
            }
        }
    }
}

@Composable
fun InfoPill(
    icon: Int,
    string : String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = string,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SimpleFlowingArrows(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant // Subtle Gray
) {
    Box(
        modifier = modifier.height(60.dp), // Fixed height container
        contentAlignment = Alignment.TopCenter
    ) {
        val transition = rememberInfiniteTransition(label = "Flow")

        // We create 3 arrows, staggered by time to create the "wave"
        // Delay 0ms, 400ms, 800ms
        SingleFlowingArrow(transition, delayMillis = 0, color = color)
        SingleFlowingArrow(transition, delayMillis = 660, color = color)
        SingleFlowingArrow(transition, delayMillis = 1320, color = color)
    }
}

@Composable
fun SingleFlowingArrow(
    transition: InfiniteTransition,
    delayMillis: Int,
    color: Color
) {
    // 1. Movement: Slide DOWN from -15dp to +15dp
    val offsetY by transition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing // Constant speed for smooth flow
            ),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(delayMillis)
        ),
        label = "OffsetY"
    )

    // 2. Opacity: Fade In (Top) -> Full (Middle) -> Fade Out (Bottom)
    // This hides the "teleporting" when the loop resets
    val alpha by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0 // Start invisible
                1f at 1000 // Full visible in middle
                0f at 2000 // End invisible
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(delayMillis)
        ),
        label = "Alpha"
    )

    Icon(
        imageVector = Icons.Rounded.KeyboardArrowDown,
        contentDescription = null,
        tint = color,
        modifier = Modifier
            .offset(y = offsetY.dp)
            .alpha(alpha)
            .size(32.dp) // Standard, non-flashy size
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerControlBar(
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenHeightDp = remember(windowInfo, density) {
        with(density) { windowInfo.containerSize.height.toDp() }
    }
    val responsiveHeight = (screenHeightDp * 0.10f).coerceIn(56.dp, 88.dp)
    val interactionSources = remember { listOf(MutableInteractionSource(), MutableInteractionSource()) }

    // We use a high-contrast container for the controls
    ButtonGroup(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 32.dp),
        overflowIndicator = {}
    ) {

        customItem(
            {
                FilledIconToggleButton(
                    checked = isPaused,
                    onCheckedChange  = { onPauseResume() },
                    interactionSource = interactionSources[0],
                    modifier = Modifier
                        .weight(1f)
                        .height(responsiveHeight)
                        .fillMaxHeight()
                        .animateWidth(interactionSource = interactionSources[0]),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        // Active (Paused) Color
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        // Inactive (Running) Color
                        checkedContainerColor = MaterialTheme.colorScheme.tertiary,
                        checkedContentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shapes = IconButtonDefaults.toggleableShapes(),
                ) {
                    Icon(
                        painter = if (isPaused) painterResource(R.drawable.icon_play_filled)
                                    else painterResource(R.drawable.icon_pause_filled),
                        contentDescription = if (isPaused)
                            stringResource(R.string.resume)
                        else stringResource(
                            R.string.pause
                        )
                    )
                }
            }
        ) {
            // state
        }

        customItem(
            {
                FilledIconToggleButton(
                    checked = false,
                    onCheckedChange  = { onStop() },
                    interactionSource = interactionSources[1],
                    modifier = Modifier
                        .weight(0.3f)
                        .height(responsiveHeight)
                        .fillMaxHeight()
                        .animateWidth(interactionSource = interactionSources[1]),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_stop_filled),
                        contentDescription = stringResource(R.string.stop_session)
                    )
                }
            }
        )
        {
            // state
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EndTimerDisplay(
    colour: Color,
    icon: ImageVector,
    iconColour: Color
) {
    val thickStrokeWidth = with(LocalDensity.current) { 12.dp.toPx() }
    val thickStroke =
        remember(thickStrokeWidth) {
            Stroke(
                width = thickStrokeWidth,
                cap = StrokeCap.Round
            )
        }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .widthIn(max = 200.dp)
            .aspectRatio(1f)
    ) {
        CircularWavyProgressIndicator(
            progress = { 1f },
            modifier = Modifier
                .fillMaxSize(),
            wavelength = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
            waveSpeed = WavyProgressIndicatorDefaults.LinearDeterminateWavelength / 2,
            stroke = thickStroke,
            trackStroke = thickStroke,
            color = colour,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = if (icon == Icons.Rounded.Close) stringResource(R.string.failed)
                else stringResource(
                    R.string.complete
                ),
                tint = iconColour,
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}