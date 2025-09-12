package de.hhn.fitapp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Theme Farben
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00796B),
    secondary = Color(0xFF4CAF50),
    tertiary = Color(0xFF7E57C2),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121)
)

// Dark Theme Farben
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF26A69A),
    secondary = Color(0xFF66BB6A),
    tertiary = Color(0xFF9575CD),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0)
)

// Benutzerdefinierte Farben für die Anwendung
data class FitAppColors(
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val hydration: Color,
    val nutrition: Color,
    val workout: Color,
    val health: Color
)

// Light Theme spezifische Farben
private val LightFitAppColors = FitAppColors(
    success = Color(0xFF4CAF50),
    warning = Color(0xFFFFC107),
    error = Color(0xFFF44336),
    info = Color(0xFF2196F3),
    hydration = Color(0xFF03A9F4),
    nutrition = Color(0xFF8BC34A),
    workout = Color(0xFFFF5722),
    health = Color(0xFFE91E63)
)

// Dark Theme spezifische Farben
private val DarkFitAppColors = FitAppColors(
    success = Color(0xFF66BB6A),
    warning = Color(0xFFFFD54F),
    error = Color(0xFFE57373),
    info = Color(0xFF64B5F6),
    hydration = Color(0xFF4FC3F7),
    nutrition = Color(0xFF9CCC65),
    workout = Color(0xFFFF8A65),
    health = Color(0xFFF06292)
)

// CompositionLocal für den Zugriff auf die benutzerdefinierten Farben
private val LocalFitAppColors = staticCompositionLocalOf<FitAppColors> {
    error("FitAppColors wurden nicht bereitgestellt")
}

/**
 * Theme-Wrapper für die FitApp-Anwendung.
 * Stellt Material3-Theme und benutzerdefinierte Farben bereit.
 */
@Composable
fun FitAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val fitAppColors = if (darkTheme) DarkFitAppColors else LightFitAppColors

    CompositionLocalProvider(LocalFitAppColors provides fitAppColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Objekt für den Zugriff auf die benutzerdefinierten Farben.
 */
object FitAppTheme {
    /**
     * Zugriff auf die benutzerdefinierten Farben innerhalb einer Composition.
     */
    val colors: FitAppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFitAppColors.current
}