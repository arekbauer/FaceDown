package com.arekb.facedown.ui.stats.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arekb.facedown.R
import com.arekb.facedown.ui.theme.FaceDownTheme
import com.arekb.facedown.ui.theme.googleSansFlex


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
                        weight = 700,
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