package com.arekb.facedown.ui.stats.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arekb.facedown.R
import com.arekb.facedown.data.database.FocusSession
import com.arekb.facedown.ui.theme.FaceDownTheme
import com.arekb.facedown.ui.theme.googleSansFlex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class WeeklyBarData(
    val dayLabel: String, // "M", "T", "W"...
    val minutes: Int,     // Total focus time
    val ratio: Float,     // 0.0 to 1.0 (relative to the biggest bar)
    val isToday: Boolean
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true, widthDp = 400)
@Composable
fun PreviewSessionCard() {
    FaceDownTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SessionCard(
                session = FocusSession(
                    id = 1,
                    durationMinutes = 30,
                    timestamp = Instant.now().toEpochMilli(),
                    tag = "Work",
                    note = "Finished the Q3 report draft."
                ),
                shape = MaterialShapes.SoftBurst.toShape()
            )
            // yesterday
            SessionCard(
                session = FocusSession(
                    id = 1,
                    durationMinutes = 25,
                    timestamp = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli(),
                    tag = "Read",
                    note = null
                ),
                shape = MaterialShapes.SoftBurst.toShape()
            )
            SessionCard(
                session = FocusSession(
                    id = 1,
                    durationMinutes = 30,
                    timestamp = Instant.now().minus(3, ChronoUnit.DAYS).toEpochMilli(),
                    tag = "Focus",
                    note = null
                ),
                shape = MaterialShapes.SoftBurst.toShape()
            )
            SessionCard(
                session = FocusSession(
                    id = 1,
                    durationMinutes = 30,
                    timestamp = Instant.now().minus(31, ChronoUnit.DAYS).toEpochMilli(),
                    tag = "Study",
                    note = "Revising biology"
                ),
                shape = MaterialShapes.SoftBurst.toShape()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeeklyChart() {
    // Mock Data Scenario:
    // User crushed it on Tuesday (Spotlight), missed Wednesday, and it is currently Thursday.
    val mockData = listOf(
        WeeklyBarData("Mon", 45, 0.37f, false),
        WeeklyBarData("Tue", 120, 1.0f, false), // Best Day (Ratio 1.0)
        WeeklyBarData("Wed", 0, 0f, false),   // Zero Day
        WeeklyBarData("Thu", 60, 0.5f, false),   // TODAY
        WeeklyBarData("Fri", 76, 0.75f, true),   // Future
        WeeklyBarData("Sat", 0, 0.0f, false),   // Future
        WeeklyBarData("Sun", 0, 0.0f, false)    // Future
    )

    FaceDownTheme {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Weekly snapshot",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedWeeklyChart(weekData = mockData)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(name = "Stats Hero Card", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun StatsHeroCardPreview() {
    FaceDownTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            StatsHeroCard(
                icon = R.drawable.icon_fire_filled,
                value = "34",
                label = "Day streak",
                containerColour = MaterialTheme.colorScheme.primaryContainer,
                contentColour = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialShapes.Pill.toShape(),
                modifier = Modifier.weight(1f)
            )

            StatsHeroCard(
                icon = R.drawable.icon_hourglass_filled,
                value = "7h 44m",
                label = "Total focus",
                containerColour = MaterialTheme.colorScheme.secondaryContainer,
                contentColour = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = MaterialShapes.SoftBurst.toShape(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AnimatedWeeklyChart(
    weekData: List<WeeklyBarData>,
    modifier: Modifier = Modifier
) {
    // Safety check
    if (weekData.isEmpty()) return

    // Colours
    val spotlightColor = MaterialTheme.colorScheme.primary
    val secondaryColor = spotlightColor.copy(alpha = 0.35f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val emptyDotColor = MaterialTheme.colorScheme.surfaceVariant
    val futureColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    // Fonts
    val textMeasurer = rememberTextMeasurer()
    val labelLarge = MaterialTheme.typography.labelLarge

    // --- ANIMATION STATE ---
    // Create one unique animation property for each bar
    val barAnimations = remember(weekData.size) {
        List(weekData.size) { Animatable(0f) }
    }

    // Trigger the "Wave" effect
    LaunchedEffect(weekData) {
        barAnimations.forEachIndexed { index, animatable ->
            launch {
                delay(index * 100L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(top = 32.dp, bottom = 12.dp)
    ) {
        // --- PRE-CALCULATIONS ---
        val slotWidth = size.width / weekData.size
        val barWidth = slotWidth * 0.85f
        val chartMaxHeight = size.height - 8.dp.toPx() // Leave room for bottom text

        // Find the index of "Today" so we know what is "Future"
        val todayIndex = weekData.indexOfFirst { it.isToday }

        weekData.forEachIndexed { index, day ->
            val xOffset = (index * slotWidth) + (slotWidth - barWidth) / 2

            // Logic Flags
            val isFuture = todayIndex != -1 && index > todayIndex
            val isSpotlight = day.ratio >= 0.99f && !isFuture && day.minutes > 0
            val isZero = day.minutes == 0

            // Drawing logic

            if (isFuture) {
                // CASE 1: Future Day (Empty space + Faint label)
                val textLayout = textMeasurer.measure(
                    text = day.dayLabel,
                    style = labelLarge.copy(color = futureColor, fontWeight = FontWeight.Normal)
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x = xOffset + (barWidth - textLayout.size.width) / 2,
                        y = chartMaxHeight + 8.dp.toPx()
                    )
                )
            }
            else if (isZero) {
                // CASE 2: Zero Minutes (Ghost Dot + Normal label)
                drawCircle(
                    color = emptyDotColor,
                    radius = 3.dp.toPx(),
                    center = Offset(
                        x = xOffset + barWidth / 2,
                        y = chartMaxHeight - 4.dp.toPx() // Sit on baseline
                    )
                )

                val textLayout = textMeasurer.measure(
                    text = day.dayLabel,
                    style = labelLarge.copy(color = labelColor)
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x = xOffset + (barWidth - textLayout.size.width) / 2,
                        y = chartMaxHeight + 8.dp.toPx()
                    )
                )
            }
            else {
                // CASE 3: Active Day (Animated Bar + Value Label + Day Label)

                // Calculate height based on ratio
                val targetHeight = day.ratio * chartMaxHeight
                val currentHeight = targetHeight * barAnimations[index].value
                val barTopY = chartMaxHeight - currentHeight

                // Draw Bar
                if (currentHeight > 0) {
                    drawRoundRect(
                        color = if (isSpotlight) spotlightColor else secondaryColor,
                        topLeft = Offset(x = xOffset, y = barTopY),
                        size = Size(width = barWidth, height = currentHeight),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )

                    // Draw Value Label (e.g. "2h") riding on top
                    // Only if bar is tall enough to not overlap bottom text
                    if (currentHeight > 20.dp.toPx()) {
                        val valueText = formatMinutes(day.minutes)
                        val valueLayout = textMeasurer.measure(
                            text = valueText,
                            style = labelLarge.copy(
                                color = labelColor
                            )
                        )
                        drawText(
                            textLayoutResult = valueLayout,
                            topLeft = Offset(
                                x = xOffset + (barWidth - valueLayout.size.width) / 2,
                                y = barTopY - valueLayout.size.height - 2.dp.toPx()
                            )
                        )
                    }
                }

                // Draw Day Label (M, T, W...)
                val dayLayout = textMeasurer.measure(
                    text = day.dayLabel,
                    style = labelLarge.copy(
                        color = if (isSpotlight) spotlightColor else labelColor
                    )
                )
                drawText(
                    textLayoutResult = dayLayout,
                    topLeft = Offset(
                        x = xOffset + (barWidth - dayLayout.size.width) / 2,
                        y = chartMaxHeight + 8.dp.toPx()
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsHeroCard(
    modifier: Modifier = Modifier,
    icon: Int,
    value: String,
    label: String,
    containerColour: Color,
    contentColour: Color,
    shape: Shape
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = containerColour,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = shape,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = contentColour
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Bottom: The Data
            Column {
                Text(
                    text = value,
                    style = googleSansFlex(
                        weight = 800,
                        slant = -10f,
                        width = 112.5f,
                        roundness = 100f,
                        size = 32.sp
                    ),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColour
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SessionCard(
    session: FocusSession,
    shape: Shape
) {
    val baseColor = getTagColor(session.tag)
    val faintColor = baseColor.copy(alpha = 0.1f)

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), // More breathing room
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = faintColor,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = session.tag.uppercase(),
                    style = googleSansFlex(
                        weight = 900,
                        slant = -10f,
                        size = 14.sp
                    ),
                    color = baseColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f) // Fills available space
            ) {
                // Note
                Text(
                    text = if (session.note.isNullOrBlank()) "${session.tag} session" else session.note,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Time
                Text(
                    text = formatSessionTime(session.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            // Duration Badge
            Surface(
                color = faintColor,
                shape = shape,
                modifier = Modifier.size(70.dp)

            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "${session.durationMinutes}m",
                        style = googleSansFlex(
                            weight = 800,
                            slant = -10f,
                            width = 112.5f,
                            roundness = 100f,
                            size = 18.sp
                        ),
                        color = baseColor,
                    )
                }
            }
        }
    }
}


// Helper to keep labels short
fun formatMinutes(minutes: Int): String {
    if (minutes < 60) return "${minutes}m"
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0) "${h}h" else "${h}h ${m}m"
}

fun getTagColor(tag: String): Color {
    return when (tag.uppercase()) {
        "WORK" -> Color(0xFF5E60CE) // Purple
        "STUDY" -> Color(0xFF43A047) // Green
        "READ" -> Color(0xFFE65100) // Orange
        else -> Color(0xFF00796B) // Teal default
    }
}

@Composable
fun formatSessionTime(timestamp: Long): String {
    return remember(timestamp) {
        val instant = Instant.ofEpochMilli(timestamp)
        val zoneId = ZoneId.systemDefault()
        val sessionDate = instant.atZone(zoneId).toLocalDate()
        val today = java.time.LocalDate.now()

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())

        val timeStr = instant.atZone(zoneId).format(timeFormatter)

        when {
            sessionDate.isEqual(today) -> "$timeStr"
            sessionDate.isEqual(today.minusDays(1)) -> "Yesterday"
            else -> instant.atZone(zoneId).format(dateFormatter)
        }
    }
}