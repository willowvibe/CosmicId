package com.willowvibe.agereveal.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Premium theme packs — each defines a full Material3 ColorScheme for both dark and light modes.
 * Locked behind the premium subscription; free-tier users see the default Warm dark theme.
 */
enum class PremiumTheme(val id: Int, val label: String, val description: String) {
    DEFAULT(0, "Cosmic Warm", "The original warm, earthy palette — dark and cozy."),
    VAPORWAVE(1, "Vaporwave", "Neon pinks, cyans, and purples against deep indigo."),
    COTTAGECORE(2, "Cottagecore", "Soft greens, creams, and warm browns — nature-inspired."),
    Y2K(3, "Y2K", "Hot pink, electric blue, and chrome silver — millennium vibes."),
    DARK_ACADEMIA(4, "Dark Academia", "Deep browns, golds, and parchment tones — scholarly warmth."),
    CYBERPUNK(5, "Cyberpunk", "Neon green, electric yellow, and dark steel — high-tech dystopia.");

    companion object {
        fun fromId(id: Int): PremiumTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/** Returns a dark ColorScheme for the given premium theme. */
fun premiumDarkColorScheme(theme: PremiumTheme): androidx.compose.material3.ColorScheme {
    return when (theme) {
        PremiumTheme.DEFAULT -> darkColorScheme(
            primary = WarmTeal,
            onPrimary = WarmInk,
            primaryContainer = WarmTealDeep,
            secondary = WarmAmber,
            onSecondary = WarmBlack,
            background = WarmBlack,
            onBackground = WarmInk,
            surface = WarmSurface,
            onSurface = WarmInk,
            surfaceVariant = WarmSurfaceSoft,
            onSurfaceVariant = WarmInkDim,
            error = WarmError,
        )
        PremiumTheme.VAPORWAVE -> darkColorScheme(
            primary = Color(0xFFFF6B9D),       // neon pink
            onPrimary = Color(0xFF1A0033),
            primaryContainer = Color(0xFF4A0066),
            secondary = Color(0xFF00E5FF),      // cyan
            onSecondary = Color(0xFF001A1A),
            background = Color(0xFF0D0221),     // deep indigo
            onBackground = Color(0xFFF0E6FF),
            surface = Color(0xFF1A0A33),
            onSurface = Color(0xFFF0E6FF),
            surfaceVariant = Color(0xFF2D1352),
            onSurfaceVariant = Color(0xFFB8A9D4),
            error = Color(0xFFFF5252),
        )
        PremiumTheme.COTTAGECORE -> darkColorScheme(
            primary = Color(0xFF7CB342),       // leaf green
            onPrimary = Color(0xFF1A1A0A),
            primaryContainer = Color(0xFF33691E),
            secondary = Color(0xFFA1887F),      // warm brown
            onSecondary = Color(0xFF1A1410),
            background = Color(0xFF1B1A10),     // dark earth
            onBackground = Color(0xFFF5F0E0),
            surface = Color(0xFF252318),
            onSurface = Color(0xFFF5F0E0),
            surfaceVariant = Color(0xFF353025),
            onSurfaceVariant = Color(0xFFC4B8A8),
            error = Color(0xFFE57373),
        )
        PremiumTheme.Y2K -> darkColorScheme(
            primary = Color(0xFFFF4081),       // hot pink
            onPrimary = Color(0xFF1A0A10),
            primaryContainer = Color(0xFF880E4F),
            secondary = Color(0xFF40C4FF),      // electric blue
            onSecondary = Color(0xFF0A1A2A),
            background = Color(0xFF121212),     // near black
            onBackground = Color(0xFFF5F5F5),
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFF5F5F5),
            surfaceVariant = Color(0xFF2C2C2C),
            onSurfaceVariant = Color(0xFFBDBDBD),
            error = Color(0xFFFF1744),
        )
        PremiumTheme.DARK_ACADEMIA -> darkColorScheme(
            primary = Color(0xFFC9A84C),       // antique gold
            onPrimary = Color(0xFF1A1505),
            primaryContainer = Color(0xFF6B4C1E),
            secondary = Color(0xFF8D6E63),      // warm brown
            onSecondary = Color(0xFF1A1410),
            background = Color(0xFF1A1510),     // dark parchment
            onBackground = Color(0xFFF0E8D8),
            surface = Color(0xFF221D16),
            onSurface = Color(0xFFF0E8D8),
            surfaceVariant = Color(0xFF322920),
            onSurfaceVariant = Color(0xFFC4B098),
            error = Color(0xFFB71C1C),
        )
        PremiumTheme.CYBERPUNK -> darkColorScheme(
            primary = Color(0xFF00E676),       // neon green
            onPrimary = Color(0xFF001A0A),
            primaryContainer = Color(0xFF006633),
            secondary = Color(0xFFFFEA00),      // electric yellow
            onSecondary = Color(0xFF1A1A00),
            background = Color(0xFF0A0E0A),     // dark steel
            onBackground = Color(0xFFE0FFE0),
            surface = Color(0xFF121812),
            onSurface = Color(0xFFE0FFE0),
            surfaceVariant = Color(0xFF1E241E),
            onSurfaceVariant = Color(0xFFB0C0B0),
            error = Color(0xFFFF1744),
        )
    }
}

/** Returns a light ColorScheme for the given premium theme (used when theme mode is Light). */
fun premiumLightColorScheme(theme: PremiumTheme): androidx.compose.material3.ColorScheme {
    return when (theme) {
        PremiumTheme.DEFAULT -> lightColorScheme(
            primary = BrandGreen,
            onPrimary = Color.White,
            primaryContainer = BrandGreenLight,
            secondary = BrandGold,
            onSecondary = Color.White,
            background = LightBackground,
            onBackground = LightOnSurface,
            surface = LightSurface,
            onSurface = LightOnSurface,
            surfaceVariant = Color(0xFFE8E0D0),
            onSurfaceVariant = Color(0xFF5C5548),
            error = WarmError,
        )
        PremiumTheme.VAPORWAVE -> lightColorScheme(
            primary = Color(0xFFE91E63),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFCE4EC),
            secondary = Color(0xFF00BCD4),
            onSecondary = Color.White,
            background = Color(0xFFFAF5FF),
            onBackground = Color(0xFF1A0033),
            surface = Color.White,
            onSurface = Color(0xFF1A0033),
            surfaceVariant = Color(0xFFEDE7F6),
            onSurfaceVariant = Color(0xFF5C4F6B),
            error = Color(0xFFE53935),
        )
        PremiumTheme.COTTAGECORE -> lightColorScheme(
            primary = Color(0xFF558B2F),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDCEDC8),
            secondary = Color(0xFF8D6E63),
            onSecondary = Color.White,
            background = Color(0xFFFDF8F0),
            onBackground = Color(0xFF1A1410),
            surface = Color(0xFFF5EDE0),
            onSurface = Color(0xFF1A1410),
            surfaceVariant = Color(0xFFE8DCC8),
            onSurfaceVariant = Color(0xFF5C4F40),
            error = Color(0xFFC62828),
        )
        PremiumTheme.Y2K -> lightColorScheme(
            primary = Color(0xFFF50057),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFCDD2),
            secondary = Color(0xFF0288D1),
            onSecondary = Color.White,
            background = Color(0xFFFAFAFA),
            onBackground = Color(0xFF121212),
            surface = Color.White,
            onSurface = Color(0xFF121212),
            surfaceVariant = Color(0xFFF0F0F0),
            onSurfaceVariant = Color(0xFF606060),
            error = Color(0xFFD50000),
        )
        PremiumTheme.DARK_ACADEMIA -> lightColorScheme(
            primary = Color(0xFF8B6914),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFF5E6C8),
            secondary = Color(0xFF5D4037),
            onSecondary = Color.White,
            background = Color(0xFFFDF6EC),
            onBackground = Color(0xFF1A1510),
            surface = Color(0xFFF5E8D0),
            onSurface = Color(0xFF1A1510),
            surfaceVariant = Color(0xFFE8D8C0),
            onSurfaceVariant = Color(0xFF5C4C38),
            error = Color(0xFFB71C1C),
        )
        PremiumTheme.CYBERPUNK -> lightColorScheme(
            primary = Color(0xFF00C853),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFB9F6CA),
            secondary = Color(0xFFFFD600),
            onSecondary = Color.White,
            background = Color(0xFFF5FFF5),
            onBackground = Color(0xFF0A1A0A),
            surface = Color(0xFFE8F5E8),
            onSurface = Color(0xFF0A1A0A),
            surfaceVariant = Color(0xFFD0E8D0),
            onSurfaceVariant = Color(0xFF3C503C),
            error = Color(0xFFD50000),
        )
    }
}
