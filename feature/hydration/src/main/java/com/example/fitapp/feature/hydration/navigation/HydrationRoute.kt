package com.example.fitapp.feature.hydration.navigation

/**
 * Hydration feature navigation routes
 */
sealed class HydrationRoute(val route: String) {
    object HydrationDashboard : HydrationRoute("hydration_dashboard")
    object WaterTracker : HydrationRoute("water_tracker")
    object HydrationSettings : HydrationRoute("hydration_settings")
    object HydrationHistory : HydrationRoute("hydration_history")
    
    companion object {
        const val HYDRATION_GRAPH_ROUTE = "hydration_graph"
        
        /**
         * All hydration routes for navigation validation
         */
        val allRoutes = listOf(
            HydrationDashboard.route,
            WaterTracker.route,
            HydrationSettings.route,
            HydrationHistory.route
        )
    }
}