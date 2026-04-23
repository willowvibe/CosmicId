package com.willowvibe.agereveal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.data.preferences.UserPreferencesRepository
import com.willowvibe.agereveal.ui.viewmodel.SettingsViewModel

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
    content: @Composable () -> Unit,
) {
    // User-selected theme override (system / light / dark) from DataStore
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        UserPreferencesRepository.THEME_LIGHT -> false
        UserPreferencesRepository.THEME_DARK -> true
        else -> systemDark
    }
    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content,
    )
}
