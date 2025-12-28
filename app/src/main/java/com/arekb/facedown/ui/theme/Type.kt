package com.arekb.facedown.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.arekb.facedown.R

@OptIn(ExperimentalTextApi::class)
val RobotoFlexDisplay = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(900), // Black
            FontVariation.width(125f), // Expanded
            FontVariation.slant(0f)    // Upright
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexCustom = FontFamily(
    Font(
        resId = R.font.roboto_flex_family,
        variationSettings = FontVariation.Settings(
            // 1. Standard Axes
            FontVariation.slant(-10f),
            FontVariation.width(150f),
            FontVariation.weight(900),
        )
    )
)

// For Titles, Headers, and Buttons (Labels)
@OptIn(ExperimentalTextApi::class)
val FaceDownRounded = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.Setting("ROND", 100f)
        )
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

// The Reading/Data Font -> SQUARE (ROND 0)
// For Body text only
@OptIn(ExperimentalTextApi::class)
val FaceDownSquare = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.Setting("ROND", 0f)
        )
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.Setting("ROND", 0f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun googleSansFlex(
    weight: Int = 400,      // Standard Weight (100-1000)
    width: Float = 100f,    // Width (wdth)
    slant: Float = 0f,      // Slant (slnt) - usually negative like -10
    roundness: Float = 0f,  // Roundness (ROND) - 0 to 100
    size: TextUnit = 16.sp, // Font Size
    lineHeight: TextUnit = TextUnit.Unspecified
): TextStyle {
    // We use 'remember' so this expensive font creation only happens
    // when one of your parameters actually changes.
    val customFontFamily = remember(weight, width, slant, roundness) {
        FontFamily(
            Font(
                resId = R.font.google_sans_flex,
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(weight),
                    FontVariation.width(width),
                    FontVariation.slant(slant),
                    FontVariation.Setting("ROND", roundness)
                )
            )
        )
    }

    return TextStyle(
        fontFamily = customFontFamily,
        fontSize = size,
        lineHeight = lineHeight
    )
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val Typography = Typography(
    // --- DISPLAY (Big Numbers) ---
    displayLarge = TextStyle(
        fontFamily = RobotoFlexDisplay,
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = RobotoFlexDisplay,
        fontWeight = FontWeight.Black,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = RobotoFlexDisplay,
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),

    // --- HEADLINES (Brand Personality) -> ROUNDED ---
    headlineLarge = TextStyle(
        fontFamily = FaceDownSquare,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FaceDownSquare,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FaceDownSquare,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),

    headlineLargeEmphasized = TextStyle(
        fontFamily = RobotoFlexCustom,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMediumEmphasized = TextStyle(
        fontFamily = RobotoFlexCustom,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmallEmphasized = TextStyle(
        fontFamily = RobotoFlexCustom,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),


    // --- TITLES (Section Headers) -> ROUNDED ---
    titleLarge = TextStyle(
        fontFamily = FaceDownRounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FaceDownRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FaceDownRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // --- BODY (Reading)
    bodyLarge = TextStyle(
        fontFamily = FaceDownSquare,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FaceDownSquare,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FaceDownSquare,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // --- LABELS (Buttons) -> ROUNDED
    labelLarge = TextStyle(
        fontFamily = FaceDownRounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FaceDownRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FaceDownRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)