package com.example.fitapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Fallback-Farben (wenn Dynamic Color nicht verfügbar ist, z.B. < Android 12).
 * Dezent, sportlich, gut lesbar. Du kannst sie jederzeit mit deinen Markenfarben ersetzen.
 */

// Light
private val md_light_primary = Color(0xFF2E7D32)   // Grün: motivierend, „gesund“
private val md_light_onPrimary = Color(0xFFFFFFFF)
private val md_light_primaryContainer = Color(0xFFA5D6A7)
private val md_light_onPrimaryContainer = Color(0xFF08210A)

private val md_light_secondary = Color(0xFF006A6A) // Türkis-Akzent für UI-Highlights
private val md_light_onSecondary = Color(0xFFFFFFFF)
private val md_light_secondaryContainer = Color(0xFF9CEAEA)
private val md_light_onSecondaryContainer = Color(0xFF002020)

private val md_light_tertiary = Color(0xFF7B5800)  // Warm für Warnungen/Prozente
private val md_light_onTertiary = Color(0xFFFFFFFF)
private val md_light_tertiaryContainer = Color(0xFFFFDEA0)
private val md_light_onTertiaryContainer = Color(0xFF261A00)

private val md_light_error = Color(0xFFB3261E)
private val md_light_onError = Color(0xFFFFFFFF)
private val md_light_background = Color(0xFFFCFFFB)
private val md_light_onBackground = Color(0xFF11140F)
private val md_light_surface = Color(0xFFFCFFFB)
private val md_light_onSurface = Color(0xFF11140F)
private val md_light_surfaceVariant = Color(0xFFE0E4DB)
private val md_light_onSurfaceVariant = Color(0xFF44483F)
private val md_light_outline = Color(0xFF74796F)

// Dark
private val md_dark_primary = Color(0xFF80C883)
private val md_dark_onPrimary = Color(0xFF06320B)
private val md_dark_primaryContainer = Color(0xFF1D5221)
private val md_dark_onPrimaryContainer = Color(0xFFAEE7B0)

private val md_dark_secondary = Color(0xFF79D0D0)
private val md_dark_onSecondary = Color(0xFF003737)
private val md_dark_secondaryContainer = Color(0xFF004F4F)
private val md_dark_onSecondaryContainer = Color(0xFF96F1F1)

private val md_dark_tertiary = Color(0xFFE8C26B)
private val md_dark_onTertiary = Color(0xFF3E2D00)
private val md_dark_tertiaryContainer = Color(0xFF5A4300)
private val md_dark_onTertiaryContainer = Color(0xFFFFE2A8)

private val md_dark_error = Color(0xFFF2B8B5)
private val md_dark_onError = Color(0xFF601410)
private val md_dark_background = Color(0xFF0B0F0B)
private val md_dark_onBackground = Color(0xFFE2E6E1)
private val md_dark_surface = Color(0xFF0B0F0B)
private val md_dark_onSurface = Color(0xFFE2E6E1)
private val md_dark_surfaceVariant = Color(0xFF40463E)
private val md_dark_onSurfaceVariant = Color(0xFFC2C8BE)
private val md_dark_outline = Color(0xFF8C9388)

val LightColors = lightColorScheme(
    primary = md_light_primary,
    onPrimary = md_light_onPrimary,
    primaryContainer = md_light_primaryContainer,
    onPrimaryContainer = md_light_onPrimaryContainer,
    secondary = md_light_secondary,
    onSecondary = md_light_onSecondary,
    secondaryContainer = md_light_secondaryContainer,
    onSecondaryContainer = md_light_onSecondaryContainer,
    tertiary = md_light_tertiary,
    onTertiary = md_light_onTertiary,
    tertiaryContainer = md_light_tertiaryContainer,
    onTertiaryContainer = md_light_onTertiaryContainer,
    error = md_light_error,
    onError = md_light_onError,
    background = md_light_background,
    onBackground = md_light_onBackground,
    surface = md_light_surface,
    onSurface = md_light_onSurface,
    surfaceVariant = md_light_surfaceVariant,
    onSurfaceVariant = md_light_onSurfaceVariant,
    outline = md_light_outline,
)

val DarkColors = darkColorScheme(
    primary = md_dark_primary,
    onPrimary = md_dark_onPrimary,
    primaryContainer = md_dark_primaryContainer,
    onPrimaryContainer = md_dark_onPrimaryContainer,
    secondary = md_dark_secondary,
    onSecondary = md_dark_onSecondary,
    secondaryContainer = md_dark_secondaryContainer,
    onSecondaryContainer = md_dark_onSecondaryContainer,
    tertiary = md_dark_tertiary,
    onTertiary = md_dark_onTertiary,
    tertiaryContainer = md_dark_tertiaryContainer,
    onTertiaryContainer = md_dark_onTertiaryContainer,
    error = md_dark_error,
    onError = md_dark_onError,
    background = md_dark_background,
    onBackground = md_dark_onBackground,
    surface = md_dark_surface,
    onSurface = md_dark_onSurface,
    surfaceVariant = md_dark_surfaceVariant,
    onSurfaceVariant = md_dark_onSurfaceVariant,
    outline = md_dark_outline,
)
