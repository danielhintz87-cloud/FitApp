package com.example.fitapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// 5 Top-Level-Destinationen – genau richtig für Bottom Navigation (M3).
// Labels-only ist ok; Icons können wir später ergänzen.
private sealed class Dest(val route: String, val label: String) {
    data object Today : Dest("today", "Heute")
    data object Training : Dest("training", "Training")
    data object Nutrition : Dest("nutrition", "Ernährung")
    data object Shopping : Dest("shopping", "Einkauf")
    data object Progress : Dest("progress", "Fortschritt")
}

@Composable
fun FitApp() {
    val navController = rememberNavController()
    val items = listOf(
        Dest.Today, Dest.Training, Dest.Nutrition, Dest.Shopping, Dest.Progress
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.route == item.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                // Empfohlenes Muster für BottomNav:
                                // - popUpTo Start, um Doppelungen zu vermeiden
                                // - saveState/restoreState für State pro Tab
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { /* Icons optional – wir bleiben bei Labels-only */ },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Dest.Today.route
            ) {
                composable(Dest.Today.route) { TodayScreen() }
                composable(Dest.Training.route) { TrainingSetupScreen() }
                composable(Dest.Nutrition.route) { NutritionScreen() }
                composable(Dest.Shopping.route) { ShoppingListScreen() }
                composable(Dest.Progress.route) { ProgressScreen() }
            }
        }
    }
}
