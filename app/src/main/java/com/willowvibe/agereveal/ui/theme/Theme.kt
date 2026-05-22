package com.willowvibe.agereveal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.data.preferences.UserPreferencesRepository
import com.willowvibe.agereveal.ui.viewmodel.SettingsViewModel

@Composable
fun AgeRevealTheme(
    content: @Composable () -> Unit,
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val themePackId by settingsViewModel.themePack.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        UserPreferencesRepository.THEME_LIGHT -> false
        UserPreferencesRepository.THEME_DARK -> true
        else -> systemDark
    }
    val pack = PremiumTheme.fromId(themePackId)
    val colorScheme = if (useDark) premiumDarkColorScheme(pack) else premiumLightColorScheme(pack)

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content,
    )
}
