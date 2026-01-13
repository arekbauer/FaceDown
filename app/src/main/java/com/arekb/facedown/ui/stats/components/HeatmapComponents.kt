package com.arekb.facedown.ui.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.arekb.facedown.R
import com.arekb.facedown.data.stats.HeatmapLevel
import com.arekb.facedown.ui.stats.HeatmapWeek
import com.arekb.facedown.ui.theme.FaceDownTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HeatmapStyle(
    val gutterWidth: Dp = 4.dp,
    val gutterHeight: Dp = 4.dp,
    val cornerRadiusFraction: Float = 0.20f,
    val emptyColor: Color = Color.Gray.copy(alpha = 0.10f),
)

// --- MOCK DATA GENERATOR ---
// Generates 52 weeks (1 year) of data ending today
private fun generateMockData(): List<HeatmapWeek> {
    val today = LocalDate.now()
    val totalWeeks = 52
    val weeks = mutableListOf<HeatmapWeek>()
    // Bias towards empty/low days for realism
    val weightedLevels = listOf(
        HeatmapLevel.NONE, HeatmapLevel.NONE, HeatmapLevel.NONE,
        HeatmapLevel.LOW, HeatmapLevel.LOW,
        HeatmapLevel.MEDIUM,
        HeatmapLevel.HIGH,
        HeatmapLevel.EXTREME
    )

    for (i in 0 until totalWeeks) {
        // Calculate backwards so index (totalWeeks-1) is this week
        val weeksAgo = (totalWeeks - 1) - i
        val weekStart = today.minusWeeks(weeksAgo.toLong())

        val days = (0 until 7).map { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            // Pick a random level with bias
            val level = weightedLevels.random()
            date to level
        }
        weeks.add(HeatmapWeek(weekIndex = i, days = days))
    }
    return weeks
}

// --- THE PREVIEW ---
@Preview(showBackground = true, widthDp = 411)
@Composable
fun PreviewDynamicHeatmapVariations() {
    // Generate 1 year of data once
    val mockWeeks = remember { generateMockData() }

    FaceDownTheme {
        ConsistencyCard(weeks = mockWeeks)
    }
}

@Composable
fun ConsistencyCard(
    weeks: List<HeatmapWeek>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.consistency_grid),
                    style = MaterialTheme.typography.titleLarge
                )
                // --- LEGEND ---
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    HeatmapLegend()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            ResponsiveHeatmap(
                weeks = weeks,
                targetSquareSize = 18.dp
            )
        }
    }
}

@Composable
fun ResponsiveHeatmap(
    weeks: List<HeatmapWeek>,
    modifier: Modifier = Modifier,
    targetSquareSize: Dp = 18.dp,
    style: HeatmapStyle = HeatmapStyle()
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val screenWidth = maxWidth

        // 1. How many columns *can* we fit?
        // Logic: (Screen + 1 Gutter) / (Target + Gutter)
        // We add one gutter to screen width because the last column doesn't have a right-side gap.
        val unitWidth = targetSquareSize + style.gutterWidth
        val rawCount = (screenWidth + style.gutterWidth) / unitWidth
        val visibleWeeks = rawCount.toInt().coerceAtLeast(1)

        // 2. The Precision Fix:
        // Now that we know we want 'visibleWeeks' columns, we recalculate the square size
        // to fill the screen EXACTLY.
        // Formula: (Screen - (N-1)*Gutter) / N
        val totalGapsWidth = (visibleWeeks - 1) * style.gutterWidth
        val availableSpaceForSquares = screenWidth - totalGapsWidth

        // This is the EXACT size needed to ensure no partial cuts at edges
        val preciseSquareSize = availableSpaceForSquares / visibleWeeks

        // 3. Render the Heatmap
        DynamicHeatmap(
            weeks = weeks,
            squareSize = preciseSquareSize,
            style = style
        )
    }
}

@Composable
private fun DynamicHeatmap(
    weeks: List<HeatmapWeek>,
    squareSize: Dp,
    style: HeatmapStyle
) {
    // Calculate the container height automatically
    // Height = (7 squares) + (6 gutters) + (Label Padding)
    val containerHeight = (squareSize * 7) + (style.gutterHeight * 6) + 28.dp

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Auto-scroll to "Today" (End of list)
    LaunchedEffect(weeks.size) {
        if (weeks.isNotEmpty()) listState.scrollToItem(weeks.size - 1)
    }

    LazyRow(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = Modifier
            .fillMaxWidth()
            .height(containerHeight),
        horizontalArrangement = Arrangement.spacedBy(
            space = style.gutterWidth,
            alignment = Alignment.CenterHorizontally
        ),
        contentPadding = PaddingValues(horizontal = 0.dp) // Crucial: No padding interferes with math
    ) {
        items(weeks) { week ->
            WeekCanvasColumn(
                week = week,
                squareSize = squareSize,
                style = style
            )
        }
    }
}

@Composable
private fun WeekCanvasColumn(
    week: HeatmapWeek,
    squareSize: Dp,
    style: HeatmapStyle
) {
    // Label Logic: Only show label for the first week of a month (Days 1-7)
    val firstDate = week.days.firstOrNull()?.first
    val monthFormatter = remember {
        DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
    }
    val labelText = remember(firstDate) {
        if (firstDate != null && firstDate.dayOfMonth <= 7) {
            firstDate.format(monthFormatter)
        } else {
            null
        }
    }
    val gridHeight = (squareSize * 7) + (style.gutterHeight * 6)

    val baseColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .width(squareSize)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top
    ) {

        Canvas(
            modifier = Modifier
                .width(squareSize)
                .height(gridHeight)
        ) {
            val sizePx = size.width // Since we set width(squareSize), this is precise
            val gutterPx = style.gutterHeight.toPx()
            val radius = sizePx * style.cornerRadiusFraction

            // DRAW 1: The 7 Day Squares
            for (i in 0..6) {
                val topOffset = i * (sizePx + gutterPx)
                val level = week.days.getOrNull(i)?.second ?: HeatmapLevel.NONE

                val color = when (level) {
                    HeatmapLevel.NONE -> style.emptyColor
                    HeatmapLevel.LOW -> baseColor.copy(alpha = 0.25f).compositeOver(surfaceColor)
                    HeatmapLevel.MEDIUM -> baseColor.copy(alpha = 0.5f).compositeOver(surfaceColor)
                    HeatmapLevel.HIGH -> baseColor.copy(alpha = 0.75f).compositeOver(surfaceColor)
                    HeatmapLevel.EXTREME -> baseColor
                }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(0f, topOffset),
                    size = Size(sizePx, sizePx),
                    cornerRadius = CornerRadius(radius, radius)
                )
            }
        }

        // DRAW 2: The Month Label
        if (labelText != null) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                modifier = Modifier.offset(x = (-3).dp)
            )
        }
    }
}

@Composable
fun HeatmapLegend(
    modifier: Modifier = Modifier,
    style: HeatmapStyle = HeatmapStyle()
) {

    val baseColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.less),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Draw the 5 levels
        val levels = listOf(
            HeatmapLevel.NONE,
            HeatmapLevel.LOW,
            HeatmapLevel.MEDIUM,
            HeatmapLevel.HIGH,
            HeatmapLevel.EXTREME
        )

        levels.forEach { level ->
            val color = when (level) {
                HeatmapLevel.NONE -> style.emptyColor
                HeatmapLevel.LOW -> baseColor.copy(alpha = 0.25f).compositeOver(surfaceColor)
                HeatmapLevel.MEDIUM -> baseColor.copy(alpha = 0.5f).compositeOver(surfaceColor)
                HeatmapLevel.HIGH -> baseColor.copy(alpha = 0.75f).compositeOver(surfaceColor)
                HeatmapLevel.EXTREME -> baseColor
            }

            Box(
                modifier = Modifier
                    .size(12.dp) // Slightly smaller than graph squares
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }

        Text(
            text = stringResource(R.string.more),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}