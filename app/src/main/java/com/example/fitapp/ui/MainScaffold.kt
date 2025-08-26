package com.example.fitapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitapp.ui.food.FoodScanScreen
import com.example.fitapp.ui.nutrition.NutritionScreen
import com.example.fitapp.ui.screens.PlanScreen
import com.example.fitapp.ui.screens.ProgressScreen
import com.example.fitapp.ui.screens.TodayScreen
import kotlinx.coroutines.launch

data class Destination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    val destinations = listOf(
        Destination("plan", "Plan", Icons.Filled.Timeline),
        Destination("today", "Heute", Icons.Filled.Today),
        Destination("nutrition", "Rezepte", Icons.Filled.Fastfood),
        Destination("progress", "Progress", Icons.Filled.Insights)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Navigation", style = MaterialTheme.typography.titleMedium, modifier = Modifier.statusBarsPadding())
                NavigationDrawerItem(label = { Text("Trainingsplan") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("plan") })
                NavigationDrawerItem(label = { Text("Heute") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("today") })
                NavigationDrawerItem(label = { Text("Rezepte") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("nutrition") })
                NavigationDrawerItem(label = { Text("Progress") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("progress") })
                NavigationDrawerItem(label = { Text("Food Scan") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("foodscan") })
                NavigationDrawerItem(label = { Text("AI-Logs") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("logs") })
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("FitApp") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menü")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mehr")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Food Scan") }, onClick = { showMenu = false; nav.navigate("foodscan") })
                            DropdownMenuItem(text = { Text("AI-Logs") }, onClick = { showMenu = false; nav.navigate("logs") })
                            DropdownMenuItem(text = { Text("Über") }, onClick = { showMenu = false })
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
                    destinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                nav.navigate(dest.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(navController = nav, startDestination = "plan", modifier = Modifier.fillMaxSize()) {
                composable("plan") { PlanScreen(padding) }
                composable("today") { TodayScreen(padding) }
                composable("nutrition") { NutritionScreen() }
                composable("progress") { ProgressScreen(padding) }
                composable("foodscan") { FoodScanScreen(padding) }
                composable("logs") { AiLogsScreen(padding) }
            }
        }
    }
}
