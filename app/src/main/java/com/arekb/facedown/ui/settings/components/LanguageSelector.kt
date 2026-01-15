package com.arekb.facedown.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arekb.facedown.R
import com.arekb.facedown.ui.settings.data.SupportedLanguages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionSheet(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.choose_language), // Ensure you have this string
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // 1. System Default
            LanguageItem(
                name = stringResource(R.string.system), // "System Default"
                code = "", // Empty string represents system default
                currentCode = currentLanguageCode,
                position = ItemPosition.Top,
                onSelect = onLanguageSelected
            )

            SupportedLanguages.forEachIndexed { index, language ->
                LanguageItem(
                    name = language.name,
                    code = language.code,
                    currentCode = currentLanguageCode,
                    // Smart Corner Logic: Last item gets rounded bottom
                    position = if (index == SupportedLanguages.lastIndex) ItemPosition.Bottom else ItemPosition.Middle,
                    onSelect = onLanguageSelected
                )
            }
        }
    }
}

// Private helper to avoid repeating the FaceDownListItem boilerplate
@Composable
private fun LanguageItem(
    name: String,
    code: String,
    currentCode: String,
    position: ItemPosition,
    onSelect: (String) -> Unit
) {
    val isSelected = if (code.isEmpty()) currentCode == "system" || currentCode.isEmpty() else code == currentCode

    FaceDownListItem(
        title = name,
        position = position,
        onClick = { onSelect(code) },
        trailingContent = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.selected_lang),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            null
        }
    )
}