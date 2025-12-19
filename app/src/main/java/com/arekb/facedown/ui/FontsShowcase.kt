package com.arekb.facedown.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun TypographyShowcase(
    contentPadding: PaddingValues
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Typography System",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider()

            // Display Styles (Roboto Flex - Black/Expanded)
            TypeGroup("Display") {
                TypeRow("Display Large", MaterialTheme.typography.displayLarge, "57/64")
                TypeRow("Display Medium", MaterialTheme.typography.displayMedium, "45/52")
                TypeRow("Display Small", MaterialTheme.typography.displaySmall, "36/44")
            }

            // Headline Styles (FaceDown Rounded - SemiBold)
            TypeGroup("Headline") {
                TypeRow("Headline Large", MaterialTheme.typography.headlineLarge, "32/40")
                TypeRow("Headline Medium", MaterialTheme.typography.headlineMedium, "28/36")
                TypeRow("Headline Small", MaterialTheme.typography.headlineSmall, "24/32")
            }

            // Title Styles (FaceDown Rounded - Medium/SemiBold)
            TypeGroup("Title") {
                TypeRow("Title Large", MaterialTheme.typography.titleLarge, "22/28")
                TypeRow("Title Medium", MaterialTheme.typography.titleMedium, "16/24")
                TypeRow("Title Small", MaterialTheme.typography.titleSmall, "14/20")
            }

            // Body Styles (FaceDown Square - Regular)
            TypeGroup("Body") {
                TypeRow("Body Large", MaterialTheme.typography.bodyLarge, "16/24")
                TypeRow("Body Medium", MaterialTheme.typography.bodyMedium, "14/20")
                TypeRow("Body Small", MaterialTheme.typography.bodySmall, "12/16")
            }

            // Label Styles (FaceDown Rounded - Medium/SemiBold)
            TypeGroup("Label") {
                TypeRow("Label Large", MaterialTheme.typography.labelLarge, "14/20")
                TypeRow("Label Medium", MaterialTheme.typography.labelMedium, "12/16")
                TypeRow("Label Small", MaterialTheme.typography.labelSmall, "11/16")
            }
        }
    }
}

@Composable
fun TypeGroup(name: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        content()
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun TypeRow(name: String, style: TextStyle, meta: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = style
            )
        }
        Text(
            text = meta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}