package com.example.fitapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * YAZIO-style bottom navigation destinations
 * 
 * Provides a clean 4-tab navigation structure matching modern fitness apps
 */
enum class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    DIARY(
        route = "diary",
        label = "Tagebuch",
        icon = Icons.AutoMirrored.Filled.MenuBook
    ),
    FASTING(
        route = "fasting", 
        label = "Fasten",
        icon = Icons.Default.Schedule
    ),
    RECIPES(
        route = "recipes",
        label = "Rezepte", 
        icon = Icons.Default.Restaurant
    ),
    PROFILE(
        route = "profile",
        label = "Profil",
        icon = Icons.Default.Person
    );
    
    companion object {
        fun fromRoute(route: String?): BottomNavDestination? {
            return values().find { it.route == route }
        }
        
        fun getAllRoutes(): List<String> {
            return values().map { it.route }
        }
    }
}

/**
 * Navigation routes for sub-screens within each main destination
 */
object NavigationRoutes {
    // Diary sub-routes
    const val FOOD_DIARY = "diary/food"
    const val FOOD_SEARCH = "diary/search"
    const val BARCODE_SCANNER = "diary/scanner"
    const val NUTRITION_ANALYTICS = "diary/analytics"
    const val WATER_TRACKING = "diary/water"
    
    // Fasting sub-routes
    const val FASTING_TIMER = "fasting/timer"
    const val FASTING_HISTORY = "fasting/history"
    const val FASTING_SETTINGS = "fasting/settings"
    
    // Recipes sub-routes
    const val RECIPE_BROWSE = "recipes/browse"
    const val RECIPE_FAVORITES = "recipes/favorites"
    const val RECIPE_SEARCH = "recipes/search"
    const val COOKING_MODE = "recipes/cooking"
    const val SHOPPING_LIST = "recipes/shopping"
    
    // Profile sub-routes
    const val PROFILE_OVERVIEW = "profile/overview"
    const val PROFILE_GOALS = "profile/goals"
    const val PROFILE_ACHIEVEMENTS = "profile/achievements"
    const val PROFILE_SETTINGS = "profile/settings"
    const val WEIGHT_TRACKING = "profile/weight"
    const val TODAY_WORKOUT = "profile/workout"
    
    // Navigation helper functions
    fun isMainDestination(route: String?): Boolean {
        return BottomNavDestination.fromRoute(route) != null
    }
    
    fun getParentDestination(route: String?): BottomNavDestination? {
        return when {
            route?.startsWith("diary") == true -> BottomNavDestination.DIARY
            route?.startsWith("fasting") == true -> BottomNavDestination.FASTING
            route?.startsWith("recipes") == true -> BottomNavDestination.RECIPES
            route?.startsWith("profile") == true -> BottomNavDestination.PROFILE
            else -> null
        }
    }
}