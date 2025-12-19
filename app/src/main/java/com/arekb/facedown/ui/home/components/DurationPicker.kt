package com.arekb.facedown.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DurationPicker(
    currentDuration: Int,
    onDurationChange: (Int) -> Unit
) {
    // Slider Logic (Range 5m to 60m)
    val sliderPosition = remember(currentDuration) { currentDuration.toFloat() }

    // Normalize progress for the indicator (0.0 to 1.0) based on max 60m
    val progressFactor = (currentDuration / 60f).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        // --- THE HERO DISPLAY ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .aspectRatio(1f)
                .widthIn(max = 320.dp)
        ) {
            val thickStrokeWidth = with(LocalDensity.current) { 8.dp.toPx() }
            val thickStroke =
                remember(thickStrokeWidth) {
                    Stroke(
                        width = thickStrokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            val animatedProgress by
            animateFloatAsState(
                targetValue = progressFactor,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            )


            // 1. The Wavy Indicator (The "Halo")
            CircularWavyProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxSize()
                    .size(52.dp),
                wavelength = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
                waveSpeed = WavyProgressIndicatorDefaults.LinearDeterminateWavelength / 2,
                stroke = thickStroke,
                trackStroke = thickStroke,
                color = MaterialTheme.colorScheme.primary,
            )

            // 2. The Text (Centered)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$currentDuration",
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "minutes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- THE SLIDER ---
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                onDurationChange(newValue.roundToInt())
            },
            valueRange = 5f..60f,
            steps = 0, // Continuous smooth sliding
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val presets = listOf(15, 30, 45, 60)
        val interactionSources = remember { presets.map { MutableInteractionSource() } }
        ButtonGroup(
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
                                .semantics { role = Role.RadioButton }
                                .animateWidth(interactionSource = interactionSource),
                            shapes =
                                when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    presets.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                        ) {
                            Text("${mins}m")
                        }
                    }
                ) { // State - can do
                }
            }
        }
    }
}