package com.arekb.facedown.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Defines where this item sits in a group.
 * Determines which corners should be rounded.
 */
enum class ItemPosition {
    Top,    // Rounded Top, Flat Bottom
    Middle, // Flat Top, Flat Bottom
    Bottom, // Flat Top, Rounded Bottom
    Single  // Rounded all around
}

@Composable
fun FaceDownListItem(
    topText: String? = null,
    title: String,
    subtitle: String? = null,
    icon: Int? = null,
    position: ItemPosition = ItemPosition.Single,
    onClick: () -> Unit
) {
    // 1. Interaction State (Pressed vs Idle)
    val interactionSource = remember { MutableInteractionSource() }

    val topCorner = when (position) {
            ItemPosition.Top,
            ItemPosition.Single -> 24.dp
            else -> 2.dp
    }

    val bottomCorner = when (position) {
            ItemPosition.Bottom,
            ItemPosition.Single -> 24.dp
            else -> 2.dp
    }

    if (position == ItemPosition.Single || position == ItemPosition.Top) {
        if (topText != null) {
            Text(
                text = topText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
    }


    // 3. The List Item
    ListItem(
        modifier = Modifier
            .padding(horizontal = 16.dp) // Outer margin
            // The Shape Clip applies here
            .clip(
                RoundedCornerShape(
                    topStart = topCorner,
                    topEnd = topCorner,
                    bottomStart = bottomCorner,
                    bottomEnd = bottomCorner
                )
            )
            .clickable(
                interactionSource = interactionSource,
            ) { onClick() },

        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = if (subtitle != null) {
            {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        leadingContent = if (icon != null) {
            {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        } else null,
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        // Optional: Make the background slightly distinct to show the grouping
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer, // Slight gray/tone
        )
    )

    // 4. Spacer Logic (Tiny gap between items in a group)
    if (position != ItemPosition.Bottom && position != ItemPosition.Single) {
        Spacer(modifier = Modifier.padding(bottom = 2.dp))
    }
}