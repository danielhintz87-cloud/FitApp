package com.example.fitapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitapp.ui.coach.CoachScreen
import com.example.fitapp.ui.screens.HomeScreen
import com.example.fitapp.ui.screens.NutritionScreenRoot
import com.example.fitapp.ui.screens.ProgressScreen
import com.example.fitapp.ui.screens.TrainingScreen
import com.example.fitapp.ui.ShoppingListScreen

enum class RootDest(val route: String) {
    Home("home"),
    Training("training"),
    Nutrition("nutrition"),
    Progress("progress"),
    Coach("coach")
}

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    var overflowOpen by remember { mutableStateOf(false) }
    val currentRoute = currentRoute(navController) ?: ""
    val titleText = when (currentRoute) {
        RootDest.Home.route -> "Heute"
        RootDest.Training.route -> "Training"
        RootDest.Nutrition.route -> "Ernährung"
        RootDest.Progress.route -> "Fortschritt"
        "shopping" -> "Einkaufsliste"
        else -> ""
    }

    Scaffold(
        topBar = {
            if (currentRoute != RootDest.Coach.route) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = { Text(titleText) },
                    actions = {
                        IconButton(onClick = { overflowOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menü öffnen")
                        }
                        DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Setup & Einstellungen") },
                                onClick = {
                                    overflowOpen = false
                                    navController.safeNavigate(RootDest.Training.route)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Einkaufsliste") },
                                onClick = {
                                    overflowOpen = false
                                    navController.safeNavigate("shopping")
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Über/Datenschutz") },
                                onClick = { overflowOpen = false }
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                val current = currentRoute(navController)
                NavigationBarItem(
                    selected = current == RootDest.Home.route,
                    onClick = { navController.safeNavigate(RootDest.Home.route) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = null,
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = current == RootDest.Training.route,
                    onClick = { navController.safeNavigate(RootDest.Training.route) },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Training") },
                    label = null,
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = current == RootDest.Nutrition.route,
                    onClick = { navController.safeNavigate(RootDest.Nutrition.route) },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = "Ernährung") },
                    label = null,
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = current == RootDest.Progress.route,
                    onClick = { navController.safeNavigate(RootDest.Progress.route) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Fortschritt") },
                    label = null,
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = current == RootDest.Coach.route,
                    onClick = { navController.safeNavigate(RootDest.Coach.route) },
                    icon = { Icon(Icons.Default.Message, contentDescription = "Coach") },
                    label = null,
                    alwaysShowLabel = false
                )
            }
        }
    ) { innerPadding ->
        // NavHost für die Hauptbereiche der App
        NavHost(
            navController = navController,
            startDestination = RootDest.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(RootDest.Home.route) { HomeScreen() }
            composable(RootDest.Training.route) { TrainingScreen() }
            composable(RootDest.Nutrition.route) { NutritionScreenRoot() }
            composable(RootDest.Progress.route) { ProgressScreen() }
            composable(RootDest.Coach.route) { CoachScreen(navController = navController) }
            // Zusätzliches Ziel für die Einkaufsliste (über das Menü)
            composable("shopping") { ShoppingListScreen() }
        }
    }
}

// Hilfsfunktion: aktuelle Route ermitteln
private fun currentRoute(navController: NavHostController): String? =
    navController.currentBackStackEntry?.destination?.route

// Extension: Navigation ohne duplizierten Back-Stack
private fun NavHostController.safeNavigate(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
