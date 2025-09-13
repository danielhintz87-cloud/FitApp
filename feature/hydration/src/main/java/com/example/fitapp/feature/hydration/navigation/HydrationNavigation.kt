package com.example.fitapp.feature.hydration.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/**
 * Hydration feature navigation graph
 */
fun NavGraphBuilder.hydrationGraph(navController: NavController) {
    navigation(
        startDestination = HydrationRoute.HydrationDashboard.route,
        route = HydrationRoute.HYDRATION_GRAPH_ROUTE
    ) {
        composable(HydrationRoute.HydrationDashboard.route) {
            HydrationDashboardPlaceholder()
        }
        
        composable(HydrationRoute.WaterTracker.route) {
            WaterTrackerPlaceholder()
        }
        
        composable(HydrationRoute.HydrationSettings.route) {
            HydrationSettingsPlaceholder()
        }
        
        composable(HydrationRoute.HydrationHistory.route) {
            HydrationHistoryPlaceholder()
        }
    }
}

// Placeholder composables - will be replaced with actual screens during migration
@Composable
private fun HydrationDashboardPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Hydration Dashboard - Coming Soon")
    }
}

@Composable
private fun WaterTrackerPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Water Tracker - Coming Soon")
    }
}

@Composable
private fun HydrationSettingsPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Hydration Settings - Coming Soon")
    }
}

@Composable
private fun HydrationHistoryPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Hydration History - Coming Soon")
    }
}