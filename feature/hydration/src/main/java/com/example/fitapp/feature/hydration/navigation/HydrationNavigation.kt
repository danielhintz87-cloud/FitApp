package com.example.fitapp.feature.hydration.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Navigation graph extension for the Hydration feature.
 * This sets up the navigation destinations within the hydration feature.
 */
fun NavGraphBuilder.hydrationGraph(navController: NavController) {
    composable(HydrationRoute.HOME) {
        HydrationPlaceholderScreen(
            title = "Hydration Feature",
            description = "TODO: Implement hydration tracking screens\n\nThis is a placeholder for the hydration feature that will be migrated from the main app module in a future PR."
        )
    }
    
    composable(HydrationRoute.GOALS) {
        HydrationPlaceholderScreen(
            title = "Hydration Goals",
            description = "TODO: Implement hydration goals configuration"
        )
    }
    
    composable(HydrationRoute.HISTORY) {
        HydrationPlaceholderScreen(
            title = "Hydration History",
            description = "TODO: Implement hydration history and analytics"
        )
    }
}

@Composable
private fun HydrationPlaceholderScreen(
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title\n\n$description",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}