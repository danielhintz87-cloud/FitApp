package de.hhn.fitapp.core.navigation

/**
 * Zentrale Klasse für Navigationsrouten in der Anwendung.
 * Alle Navigationsrouten sollten hier als Konstanten definiert werden.
 */
object NavigationRoutes {
    // Main Screens
    const val HOME = "home"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    
    // Feature: Hydration
    object Hydration {
        const val MAIN = "hydration"
        const val TRACKER = "hydration/tracker"
        const val GOALS = "hydration/goals"
        const val HISTORY = "hydration/history"
    }
    
    // Feature: Nutrition
    object Nutrition {
        const val MAIN = "nutrition"
        const val DIARY = "nutrition/diary"
        const val RECIPES = "nutrition/recipes"
        const val MEAL_PLANNER = "nutrition/meal_planner"
        const val RECIPE_DETAILS = "nutrition/recipe/{recipeId}"
    }
    
    // Feature: Workout
    object Workout {
        const val MAIN = "workout"
        const val PLANS = "workout/plans"
        const val HISTORY = "workout/history"
        const val ACTIVE = "workout/active"
        const val PLAN_DETAILS = "workout/plan/{planId}"
    }
    
    // Feature: Health
    object Health {
        const val MAIN = "health"
        const val METRICS = "health/metrics"
        const val SYNC = "health/sync"
    }
    
    // Auth
    object Auth {
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val FORGOT_PASSWORD = "auth/forgot_password"
    }
}