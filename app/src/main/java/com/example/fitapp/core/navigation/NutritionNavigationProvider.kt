package com.example.fitapp.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fitapp.core.navigation.NavigationProvider
import com.example.fitapp.core.navigation.RouteDefinition
import com.example.fitapp.ui.food.FoodScanScreen
import com.example.fitapp.ui.nutrition.FoodSearchScreen

class NutritionNavigationProvider : NavigationProvider {
    
    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        // Food Scan
        builder.composable("foodscan") {
            FoodScanScreen(
                contentPadding = it.padding(),
                onLogged = { 
                    navController.popBackStack()
                },
                onNavigateToApiKeys = { navController.navigate("api_keys") }
            )
        }

        // Food Search
        builder.composable("food_search") {
            FoodSearchScreen(
                contentPadding = it.padding(),
                onBackPressed = { navController.popBackStack() },
                onFoodAdded = {
                    navController.popBackStack()
                }
            )
        }
    }

    override fun getMainRoutes(): List<RouteDefinition> {
        return listOf(
            RouteDefinition("foodscan", "Food Scan"),
            RouteDefinition("food_search", "Food Search")
        )
    }
}
