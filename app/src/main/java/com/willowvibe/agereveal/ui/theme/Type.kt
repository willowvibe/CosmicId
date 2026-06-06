package com.willowvibe.agereveal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.willowvibe.agereveal.R

/**
 * Inter (sans-serif) for body / UI text.
 * Serif (Georgia fallback) for display numerals and headings — mirrors the Fraunces
 * intent in the design prototype.
 *
 * `KoreanFamily` is used for Hangul body text on the Korean Saju tab. It
 * uses the system sans-serif typeface, which on Android 8+ resolves to
 * Noto Sans CJK (covering Hangul + Kanji). This keeps the APK small (no
 * bundled font) and the rendering consistent with system Hangul across
 * the OS.
 *
 * If a future design pass wants pixel-exact Noto Sans KR typography, swap
 * this for AndroidX Downloadable Fonts:
 *   implementation(libs.androidx.compose.ui.text.google.fonts)
 *   implementation(libs.google.services.fonts)
 *   and use GoogleFont.Provider with the `com.google.android.gms` cert.
 */

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

val SerifFamily = FontFamily.Serif   // Georgia on Android; swap for Fraunces once bundled

/**
 * Font family for Hangul + Kanji body text. Resolves to the system
 * sans-serif (Noto Sans CJK on Android 8+, system-supplied Noto Sans KR
 * on API 33+). Used by the Korean Saju tab.
 */
val KoreanFamily: FontFamily = FontFamily.SansSerif

val AppTypography = Typography(
    // Hero display — age numerals (e.g. "27")
    displayLarge = TextStyle(
        fontFamily = SerifFamily,
        fontWeight = FontWeight.Light,
        fontSize = 78.sp,
        lineHeight = 74.sp,
        letterSpacing = (-2).sp,
    ),
    // Secondary numeral (months / days)
    displayMedium = TextStyle(
        fontFamily = SerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 46.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.5).sp,
    ),
    // Small display (compare result)
    displaySmall = TextStyle(
        fontFamily = SerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 38.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1).sp,
    ),
    // Screen / section heading
    titleLarge = TextStyle(
        fontFamily = SerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp,
    ),
    // App name header
    titleMedium = TextStyle(
        fontFamily = SerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
    ),
    // Card stat value / person age
    titleSmall = TextStyle(
        fontFamily = SerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.3).sp,
    ),
    // Row label (e.g. "Total days lived")
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // Secondary row value
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // Tertiary text
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // ALL-CAPS micro labels (e.g. "BORN", "SECONDS ALIVE")
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 1.5.sp,
    ),
    // Standard label
    labelMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,
    ),
    // Small chips / badges
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp,
    ),
)
