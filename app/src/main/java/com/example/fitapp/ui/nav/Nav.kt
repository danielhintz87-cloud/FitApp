package com.example.fitapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fitapp.ui.coach.CoachScreen
import com.example.fitapp.ui.screens.PlanBuilderScreen
import com.example.fitapp.ui.screens.TodayScreen
import com.example.fitapp.ui.screens.FoodScanScreen
import com.example.fitapp.ui.nutrition.NutritionScreen


sealed class Dest(val route: String, val label: String) {
    data object Plan : Dest("plan", "Plan")
    data object Today : Dest("today", "Heute")
    data object Recipes : Dest("recipes", "Rezepte")
    data object FoodScan : Dest("foodscan", "Food")
    data object Coach : Dest("coach", "Coach")
}

@Composable
fun AppNavHost(nav: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = nav, startDestination = Dest.Plan.route, modifier = modifier) {
        composable(Dest.Plan.route) { PlanBuilderScreen() }
        composable(Dest.Today.route) { TodayScreen() }
        composable(Dest.Recipes.route) { NutritionScreen() }
        composable(Dest.FoodScan.route) { FoodScanScreen() }
        composable(Dest.Coach.route) { CoachScreen() }
    }
}
