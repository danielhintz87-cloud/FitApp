package com.example.fitapp.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Einheitliche Anwendung von Content Padding aus Scaffold auf einen Root-Container.
 * Ergänzt optional zusätzlichen Innenabstand (default 16.dp) und berücksichtigt
 * den dynamischen Bottom-Inset (NavBar / IME) ohne doppeltes Padding.
 */
@Composable
fun Modifier.applyContentPadding(
    contentPadding: PaddingValues,
    horizontal: Dp = 16.dp,
    top: Dp = 16.dp,
    extraBottom: Dp = 0.dp,
): Modifier {
    return this.padding(
        start = horizontal,
        end = horizontal,
        top = top,
        bottom = extraBottom,
    ).padding(contentPadding)
}
