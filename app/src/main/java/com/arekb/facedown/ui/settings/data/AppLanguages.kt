package com.arekb.facedown.ui.settings.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arekb.facedown.R
import java.util.Locale

data class AppLanguage(
    val code: String,
    val name: String
)

// 1. You ONLY maintain this simple list of codes.
// Order matters! (Usually you put English first, then others alphabetically)
@Suppress("HardCodedStringLiteral")
private val SupportedLanguageCodes = listOf(
    "en", // English
    "es", // Spanish
    "pt", // Portuguese
    "de", // German
    "fr", // French
    "hi", // Hindi
    "ru", // Russian
)

// 2. We auto-generate the full objects dynamically
val SupportedLanguages: List<AppLanguage> by lazy {
    SupportedLanguageCodes.map { code ->
        val locale = Locale.forLanguageTag(code)
        AppLanguage(
            code = code,
            // .getDisplayName(locale) returns the name IN that language.
            // e.g. "es" -> "Español", "de" -> "Deutsch"
            // .replaceFirstChar { it.uppercase() } fixes lowercase issues on some devices
            name = locale.getDisplayName(locale).replaceFirstChar { it.uppercase() }
        )
    }
}

@Composable
fun getLanguageDisplayName(code: String): String {
    if (code.isEmpty() || code == "system") {
        return stringResource(R.string.system) // Reusing "System Default" string
    }

    // Find the name in our list, or fallback to the code itself
    return SupportedLanguages.find { it.code == code }?.name
        ?: Locale.forLanguageTag(code).displayName
}