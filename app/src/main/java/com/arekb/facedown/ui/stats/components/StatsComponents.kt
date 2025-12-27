package com.arekb.facedown.ui.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


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
                        contentDescription = "Streak Flame",
                        tint = contentColour
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Bottom: The Data
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColour
                )
            }
        }
    }
}