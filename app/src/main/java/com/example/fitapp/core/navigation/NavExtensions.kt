package com.example.fitapp.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Hilfsfunktion, um PaddingValues im NavBackStackEntry zu speichern und abrufen.
 * Diese Funktion ermöglicht es, Padding zwischen Screens zu übertragen.
 */
fun NavBackStackEntry.padding(): PaddingValues {
    // Hier könnte in einer erweiterten Version PaddingValues aus einem eigenen 
    // NavType oder aus dem SavedStateHandle gelesen werden.
    // Für einfache Implementierung verwenden wir hier feste PaddingValues.
    return PaddingValues()
}

/**
 * Erweiterungsfunktion für NavGraphBuilder, die die Komposition einer Route 
 * mit zusätzlichen Optionen ermöglicht.
 *
 * @param route Die Route für die Komposition
 * @param deepLinks Optionale Deep-Links für die Route
 * @param content Der Composable-Inhalt der Route
 */
fun NavGraphBuilder.composable(
    route: String,
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    // Diese Funktion ist ein Wrapper um die Standard-composable-Funktion,
    // der in einer erweiterten Version zusätzliche Funktionalität bieten könnte.
    this.composable(route, deepLinks = deepLinks, content = content)
}