package com.willowvibe.agereveal.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary          = BrandGreen,
    onPrimary        = DarkOnSurface,
    primaryContainer = CosmosNavyShell,
    secondary        = AccentMint,
    onSecondary      = CosmosNavyDeep,
    tertiary         = BrandGold,
    background       = DarkBackground,
    surface          = DarkSurface,
    surfaceVariant   = DarkSurfaceVar,
    onBackground     = DarkOnSurface,
    onSurface        = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVar,
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
    // Enable Material You dynamic colors on Android 12+ — falls back to our palette on older devices
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content,
    )
}
