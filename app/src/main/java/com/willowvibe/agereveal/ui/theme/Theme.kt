package com.willowvibe.agereveal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary          = WarmTeal,
    onPrimary        = WarmInk,
    primaryContainer = WarmTealDeep,
    onPrimaryContainer = WarmInk,
    secondary        = WarmAmber,
    onSecondary      = WarmBlack,
    secondaryContainer = WarmAmberDeep,
    onSecondaryContainer = WarmInk,
    tertiary         = WarmAmber,
    onTertiary       = WarmBlack,
    background       = WarmBlack,
    surface          = WarmSurface,
    surfaceVariant   = WarmSurfaceSoft,
    onBackground     = WarmInk,
    onSurface        = WarmInk,
    onSurfaceVariant = WarmInkMute,
    error            = WarmError,
    outline          = WarmInkDim,
)

private val LightColorScheme = lightColorScheme(
    primary          = BrandGreen,
    onPrimary        = LightBackground,
    primaryContainer = AccentMint,
    secondary        = BrandGold,
    onSecondary      = LightBackground,
    background       = LightBackground,
    surface          = LightSurface,
    onBackground     = LightOnSurface,
    onSurface        = LightOnSurface,
)

@Composable
fun AgeRevealTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Dynamic color disabled — warm dark palette is the brand identity.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content,
    )
}
