package com.example.fitapp.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun FitAppWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = wearColorPalette,
        typography = Typography,
        content = content
    )
}

private val wearColorPalette = androidx.wear.compose.material.Colors(
    primary = androidx.compose.ui.graphics.Color(0xFFBB86FC),
    primaryVariant = androidx.compose.ui.graphics.Color(0xFF3700B3),
    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC6),
    secondaryVariant = androidx.compose.ui.graphics.Color(0xFF018786),
    background = androidx.compose.ui.graphics.Color(0xFF121212),
    surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
    error = androidx.compose.ui.graphics.Color(0xFFCF6679),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF000000),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF000000),
    onBackground = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onSurface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFBBBBBB),
    onError = androidx.compose.ui.graphics.Color(0xFF000000)
)