package com.example.fitapp.ui

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import com.example.fitapp.ui.screens.CoachScreen
import com.example.fitapp.ui.screens.HomeScreen
import com.example.fitapp.ui.screens.NutritionScreenRoot
import com.example.fitapp.ui.screens.ProgressScreen
import com.example.fitapp.ui.screens.TrainingScreen

enum class RootDest(val route: String) {
    Home("home"),
    Training("training"),
    Nutrition("nutrition"),
    Progress("progress"),
    Coach("coach")
}

@Composable
fun MainScaffold(
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenShopping: () -> Unit,
    onOpenModelPicker: () -> Unit
) {
    val nav = rememberNavController()
    var overflowOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                elevation = 0.dp,
                title = { Text("") },
                actions = {
                    ProfileAvatar(onClick = onOpenProfile)
                    IconButton(onClick = { overflowOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mehr")
                    }
                    DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
                        DropdownMenuItem(onClick = { overflowOpen = false; onOpenSettings() }) {
                            Text("Setup & Einstellungen")
                        }
                        DropdownMenuItem(onClick = { overflowOpen = false; onOpenShopping() }) {
                            Text("Einkaufsliste")
                        }
                        DropdownMenuItem(onClick = { overflowOpen = false; onOpenModelPicker() }) {
                            Text("KI‑Modell wählen")
                        }
                        androidx.compose.material.Divider()
                        DropdownMenuItem(onClick = { overflowOpen = false }) { Text("Über/Datenschutz") }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation {
                val current = currentRoute(nav)
                BottomItem(RootDest.Home, current) { nav.safeNavigate(RootDest.Home.route) }
                BottomItem(RootDest.Training, current) { nav.safeNavigate(RootDest.Training.route) }
                BottomItem(RootDest.Nutrition, current) { nav.safeNavigate(RootDest.Nutrition.route) }
                BottomItem(RootDest.Progress, current) { nav.safeNavigate(RootDest.Progress.route) }
                BottomItem(RootDest.Coach, current) { nav.safeNavigate(RootDest.Coach.route) }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = RootDest.Home.route, modifier = Modifier.padding(padding)) {
            composable(RootDest.Home.route) { HomeScreen() }
            composable(RootDest.Training.route) { TrainingScreen() }
            composable(RootDest.Nutrition.route) { NutritionScreenRoot() }
            composable(RootDest.Progress.route) { ProgressScreen() }
            composable(RootDest.Coach.route) { CoachScreen() }
        }
    }
}

@Composable
private fun BottomItem(dest: RootDest, current: String?, onClick: () -> Unit) {
    val (icon, _) = when (dest) {
        RootDest.Home -> Icons.Default.Home to "Home"
        RootDest.Training -> Icons.Default.FitnessCenter to "Training"
        RootDest.Nutrition -> Icons.Default.Restaurant to "Ernährung"
        RootDest.Progress -> Icons.Default.BarChart to "Fortschritt"
        RootDest.Coach -> Icons.Default.Message to "Coach"
    }
    BottomNavigationItem(
        selected = current == dest.route,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        label = null,
        alwaysShowLabel = false
    )
}

@Composable
private fun ProfileAvatar(onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text("Ich") }
}

private fun currentRoute(nav: NavHostController): String? =
    nav.currentBackStackEntry?.destination?.route

private fun NavHostController.safeNavigate(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

