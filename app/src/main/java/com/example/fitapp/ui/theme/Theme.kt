package com.example.fitapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

/**
 * Enhanced FitApp Theme with Custom Theme Engine
 * Supports both Material You dynamic colors and custom user themes
 */
@Composable
fun FitAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true,
    customTheme: CustomTheme? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Use custom theme if provided, otherwise use dynamic/default colors
    val colorScheme = customTheme?.toColorScheme(darkTheme) ?: when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    
    // Use custom typography and shapes if custom theme is provided
    val typography = customTheme?.toTypography() ?: AppTypography
    val shapes = customTheme?.toShapes() ?: AppShapes

    if (customTheme != null) {
        ProvideCustomTheme(customTheme) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = typography,
                shapes = shapes,
                content = content
            )
        }
    } else {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

/**
 * Enhanced dark mode theme for OLED optimization
 */
@Composable
fun FitAppOledTheme(
    customTheme: CustomTheme = ThemePresets.OLED_OPTIMIZED,
    content: @Composable () -> Unit
) {
    FitAppTheme(
        darkTheme = true,
        useDynamicColor = false,
        customTheme = customTheme,
        content = content
    )
}

/**
 * High contrast theme for accessibility
 */
@Composable
fun FitAppHighContrastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val highContrastTheme = if (darkTheme) {
        ThemePresets.OLED_OPTIMIZED.copy(highContrast = true)
    } else {
        ThemePresets.BEGINNER_FRIENDLY.copy(highContrast = true)
    }
    
    FitAppTheme(
        darkTheme = darkTheme,
        useDynamicColor = false,
        customTheme = highContrastTheme,
        content = content
    )
}
