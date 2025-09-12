package com.example.fitapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom Theme Engine for Ultimate Pro-Features
 * Provides advanced theming capabilities with user customization
 */

/**
 * Custom theme data class that extends Material 3
 */
data class CustomTheme(
    val primaryColor: Color = Color(0xFF2E7D32),
    val accentColor: Color = Color(0xFF006A6A),
    val backgroundColor: Color = Color(0xFFFCFFFB),
    val surfaceColor: Color = Color(0xFFFFFFFF),
    val errorColor: Color = Color(0xFFB3261E),
    val fontFamily: FontFamily = FontFamily.Default,
    val iconStyle: IconStyle = IconStyle.ROUNDED,
    val cardStyle: CardStyle = CardStyle.ELEVATED,
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    val cornerRadius: CornerRadius = CornerRadius.MEDIUM,
    val isOledOptimized: Boolean = false,
    val highContrast: Boolean = false,
    val adaptiveBrightness: Boolean = true,
)

/**
 * Custom theme styles and enums
 */
enum class IconStyle {
    OUTLINED,
    FILLED,
    ROUNDED,
    SHARP,
}

enum class CardStyle {
    ELEVATED,
    OUTLINED,
    FILLED,
}

enum class AnimationSpeed {
    SLOW,
    NORMAL,
    FAST,
    INSTANT,
}

enum class CornerRadius {
    SHARP,
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE,
}

/**
 * Pre-defined theme presets for different user preferences
 */
object ThemePresets {
    val FITNES_POWERHOUSE =
        CustomTheme(
            primaryColor = Color(0xFF1B5E20), // Deep green
            accentColor = Color(0xFFFF6F00), // Energetic orange
            backgroundColor = Color(0xFF121212), // Dark background
            surfaceColor = Color(0xFF1E1E1E),
            iconStyle = IconStyle.FILLED,
            cardStyle = CardStyle.ELEVATED,
            animationSpeed = AnimationSpeed.FAST,
            cornerRadius = CornerRadius.LARGE,
            isOledOptimized = true,
        )

    val PROFESSIONAL_ATHLETE =
        CustomTheme(
            primaryColor = Color(0xFF0D47A1), // Professional blue
            accentColor = Color(0xFF37474F), // Steel gray
            backgroundColor = Color(0xFFFAFAFA),
            surfaceColor = Color(0xFFFFFFFF),
            iconStyle = IconStyle.OUTLINED,
            cardStyle = CardStyle.OUTLINED,
            animationSpeed = AnimationSpeed.NORMAL,
            cornerRadius = CornerRadius.SMALL,
        )

    val BEGINNER_FRIENDLY =
        CustomTheme(
            primaryColor = Color(0xFF388E3C), // Friendly green
            accentColor = Color(0xFF9C27B0), // Motivational purple
            backgroundColor = Color(0xFFF3E5F5),
            surfaceColor = Color(0xFFFFFFFF),
            iconStyle = IconStyle.ROUNDED,
            cardStyle = CardStyle.ELEVATED,
            animationSpeed = AnimationSpeed.SLOW,
            cornerRadius = CornerRadius.EXTRA_LARGE,
            highContrast = true,
        )

    val OLED_OPTIMIZED =
        CustomTheme(
            primaryColor = Color(0xFF4CAF50),
            accentColor = Color(0xFF03DAC6),
            backgroundColor = Color(0xFF000000), // Pure black for OLED
            surfaceColor = Color(0xFF0A0A0A),
            iconStyle = IconStyle.FILLED,
            cardStyle = CardStyle.FILLED,
            animationSpeed = AnimationSpeed.NORMAL,
            cornerRadius = CornerRadius.MEDIUM,
            isOledOptimized = true,
        )
}

/**
 * Convert custom theme to Material 3 ColorScheme
 */
fun CustomTheme.toColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) {
        createDarkColorScheme()
    } else {
        createLightColorScheme()
    }
}

private fun CustomTheme.createLightColorScheme(): ColorScheme {
    return androidx.compose.material3.lightColorScheme(
        primary = primaryColor,
        onPrimary = if (highContrast) Color.Black else Color.White,
        primaryContainer = primaryColor.copy(alpha = 0.12f),
        onPrimaryContainer = primaryColor,
        secondary = accentColor,
        onSecondary = Color.White,
        secondaryContainer = accentColor.copy(alpha = 0.12f),
        onSecondaryContainer = accentColor,
        background = if (isOledOptimized) Color.Black else backgroundColor,
        onBackground = if (highContrast) Color.Black else Color(0xFF1C1B1F),
        surface = if (isOledOptimized) Color.Black else surfaceColor,
        onSurface = if (highContrast) Color.Black else Color(0xFF1C1B1F),
        error = errorColor,
        onError = Color.White,
    )
}

private fun CustomTheme.createDarkColorScheme(): ColorScheme {
    return androidx.compose.material3.darkColorScheme(
        primary = primaryColor.lighten(0.2f),
        onPrimary = Color.Black,
        primaryContainer = primaryColor.darken(0.3f),
        onPrimaryContainer = primaryColor.lighten(0.4f),
        secondary = accentColor.lighten(0.2f),
        onSecondary = Color.Black,
        secondaryContainer = accentColor.darken(0.3f),
        onSecondaryContainer = accentColor.lighten(0.4f),
        background = if (isOledOptimized) Color.Black else backgroundColor.darken(0.8f),
        onBackground = if (highContrast) Color.White else Color(0xFFE6E1E5),
        surface = if (isOledOptimized) Color.Black else surfaceColor.darken(0.8f),
        onSurface = if (highContrast) Color.White else Color(0xFFE6E1E5),
        error = errorColor.lighten(0.2f),
        onError = Color.Black,
    )
}

/**
 * Custom typography based on font family choice
 */
fun CustomTheme.toTypography(): Typography {
    val baseTextStyle = TextStyle(fontFamily = fontFamily)

    return Typography(
        displayLarge =
            baseTextStyle.copy(
                fontSize = 57.sp,
                lineHeight = 64.sp,
                fontWeight = FontWeight.Normal,
            ),
        displayMedium =
            baseTextStyle.copy(
                fontSize = 45.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Normal,
            ),
        displaySmall =
            baseTextStyle.copy(
                fontSize = 36.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Normal,
            ),
        headlineLarge =
            baseTextStyle.copy(
                fontSize = 32.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Normal,
            ),
        headlineMedium =
            baseTextStyle.copy(
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Normal,
            ),
        headlineSmall =
            baseTextStyle.copy(
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Normal,
            ),
        titleLarge =
            baseTextStyle.copy(
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Normal,
            ),
        titleMedium =
            baseTextStyle.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            ),
        titleSmall =
            baseTextStyle.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            ),
        bodyLarge =
            baseTextStyle.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
            ),
        bodyMedium =
            baseTextStyle.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
            ),
        bodySmall =
            baseTextStyle.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Normal,
            ),
        labelLarge =
            baseTextStyle.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            ),
        labelMedium =
            baseTextStyle.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
        labelSmall =
            baseTextStyle.copy(
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
    )
}

/**
 * Custom shapes based on corner radius preference
 */
fun CustomTheme.toShapes(): Shapes {
    val cornerSize =
        when (cornerRadius) {
            CornerRadius.SHARP -> 0.dp
            CornerRadius.SMALL -> 4.dp
            CornerRadius.MEDIUM -> 8.dp
            CornerRadius.LARGE -> 16.dp
            CornerRadius.EXTRA_LARGE -> 24.dp
        }

    return Shapes(
        extraSmall = RoundedCornerShape(cornerSize * 0.5f),
        small = RoundedCornerShape(cornerSize),
        medium = RoundedCornerShape(cornerSize * 1.5f),
        large = RoundedCornerShape(cornerSize * 2f),
        extraLarge = RoundedCornerShape(cornerSize * 3f),
    )
}

/**
 * Color extension functions for theme variations
 */
private fun Color.lighten(factor: Float): Color {
    val hsl = this.toHsl()
    return Color.hsl(
        hue = hsl[0],
        saturation = hsl[1],
        lightness = (hsl[2] + factor).coerceIn(0f, 1f),
    )
}

private fun Color.darken(factor: Float): Color {
    val hsl = this.toHsl()
    return Color.hsl(
        hue = hsl[0],
        saturation = hsl[1],
        lightness = (hsl[2] - factor).coerceIn(0f, 1f),
    )
}

private fun Color.toHsl(): FloatArray {
    val argb = this.toArgb()
    val r = android.graphics.Color.red(argb) / 255f
    val g = android.graphics.Color.green(argb) / 255f
    val b = android.graphics.Color.blue(argb) / 255f

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val diff = max - min

    val lightness = (max + min) / 2f

    val saturation =
        if (diff == 0f) {
            0f
        } else {
            if (lightness < 0.5f) diff / (max + min) else diff / (2f - max - min)
        }

    val hue =
        when {
            diff == 0f -> 0f
            max == r -> ((g - b) / diff) % 6f * 60f
            max == g -> ((b - r) / diff + 2f) * 60f
            else -> ((r - g) / diff + 4f) * 60f
        }

    return floatArrayOf(if (hue < 0) hue + 360f else hue, saturation, lightness)
}

/**
 * Composition local for custom theme access
 */
val LocalCustomTheme = staticCompositionLocalOf { CustomTheme() }

/**
 * Custom theme provider composable
 */
@Composable
fun ProvideCustomTheme(
    theme: CustomTheme,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalCustomTheme provides theme,
        content = content,
    )
}
