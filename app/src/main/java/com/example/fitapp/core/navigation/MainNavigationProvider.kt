package com.example.fitapp.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Restaurant
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fitapp.ui.screens.EnhancedAnalyticsScreen
import com.example.fitapp.ui.screens.EnhancedNutritionHubScreen
import com.example.fitapp.ui.screens.EnhancedTrainingHubScreen
import com.example.fitapp.ui.screens.ProgressScreen
import com.example.fitapp.ui.screens.UnifiedDashboardScreen

/**
 * NavigationProvider für die Hauptnavigation der App.
 * Enthält die wichtigsten Einstiegspunkte wie Dashboard, Training, Ernährung und Fortschritt.
 */
class MainNavigationProvider : NavigationProvider {

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        // Unified Dashboard
        builder.composable("unified_dashboard") {
            UnifiedDashboardScreen(
                contentPadding = it.padding(),
                onNavigateToFeature = { feature ->
                    navController.navigate(feature)
                }
            )
        }

        // Training & Plans
        builder.composable("plan") {
            EnhancedTrainingHubScreen(
                navController = navController,
                contentPadding = it.padding()
            )
        }

        // Nutrition Hub
        builder.composable("nutrition") {
            EnhancedNutritionHubScreen(
                navController = navController,
                contentPadding = it.padding()
            )
        }

        // Progress & Analytics
        builder.composable("progress") { 
            ProgressScreen(it.padding()) 
        }
        
        // Enhanced Analytics
        builder.composable("enhanced_analytics") {
            EnhancedAnalyticsScreen(
                contentPadding = it.padding(),
                navController = navController
            )
        }
    }

    override fun getMainRoutes(): List<RouteDefinition> {
        return listOf(
            RouteDefinition(
                route = "unified_dashboard",
                title = "Dashboard",
                icon = Icons.Filled.Dashboard,
                contentDescription = "Dashboard Icon",
                category = "main"
            ),
            RouteDefinition(
                route = "plan",
                title = "Training & Pläne",
                icon = Icons.Filled.FitnessCenter,
                contentDescription = "Training Icon",
                category = "main"
            ),
            RouteDefinition(
                route = "nutrition",
                title = "Ernährung & Rezepte",
                icon = Icons.Filled.Restaurant,
                contentDescription = "Nutrition Icon",
                category = "main"
            ),
            RouteDefinition(
                route = "enhanced_analytics",
                title = "Fortschritt & Analytics",
                icon = Icons.Filled.Insights,
                contentDescription = "Analytics Icon",
                category = "main"
            )
        )
    }
}