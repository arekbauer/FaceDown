package com.arekb.facedown.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arekb.facedown.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerProgress(
    currentDuration: Int,
) {
    // Normalize progress for the indicator (0.0 to 1.0) based on max 60m
    val progressFactor = (currentDuration / 60f).coerceIn(0f, 1f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .widthIn(max = 300.dp)
            .aspectRatio(1f)
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

        // 1. The Wavy Indicator
        CircularWavyProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxSize(),
            wavelength = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
            waveSpeed = WavyProgressIndicatorDefaults.LinearDeterminateWavelength / 2,
            stroke = thickStroke,
            trackStroke = thickStroke,
            color = MaterialTheme.colorScheme.primary,
        )

        // 2. The Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$currentDuration",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 92.sp,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.minutes),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

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
        modifier = modifier,
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
                        Text("${mins}m")
                    }
                }
            ) {
                // State ->
            }
        }
    }
}

@Composable
fun TimerConditionsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Condition 1: Do Not Disturb
        ConditionItem(
            icon = R.drawable.icon_dnd_filled,
            text = "Auto Priority Mode"
        )
    }
}

@Composable
fun ConditionItem(
    icon: Int,
    text: String
) {
    val contentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
    }
}