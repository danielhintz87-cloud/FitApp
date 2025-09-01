package com.example.fitapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitapp.ui.AiLogsScreen
import com.example.fitapp.ui.food.FoodScanScreen
import com.example.fitapp.ui.nutrition.CookingModeScreen
import com.example.fitapp.ui.nutrition.FoodDiaryScreen
import com.example.fitapp.ui.nutrition.FoodSearchScreen
import com.example.fitapp.ui.nutrition.NutritionAnalyticsScreen
import com.example.fitapp.ui.nutrition.NutritionScreen
import com.example.fitapp.ui.nutrition.SavedRecipesScreen
import com.example.fitapp.ui.screens.PlanScreen
import com.example.fitapp.ui.screens.ProgressScreen
import com.example.fitapp.ui.screens.TodayScreen
import com.example.fitapp.ui.screens.EquipmentSelectionScreen
import com.example.fitapp.ui.screens.TodayTrainingScreen
import com.example.fitapp.ui.screens.DailyWorkoutScreen
import com.example.fitapp.ui.screens.TrainingExecutionScreen
import com.example.fitapp.ui.screens.WeightTrackingScreen
import com.example.fitapp.ui.settings.ApiKeysScreen
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

data class Destination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var showOverflowMenu by remember { mutableStateOf(false) }

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
                NavigationDrawerItem(label = { Text("Gespeicherte Rezepte") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("saved_recipes") })
                NavigationDrawerItem(label = { Text("Einkaufsliste") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("shopping_list") })
                NavigationDrawerItem(label = { Text("Food Scan") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("foodscan") })
                NavigationDrawerItem(label = { Text("Ernährungstagbuch") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("food_diary") })
                NavigationDrawerItem(label = { Text("Lebensmittel suchen") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("food_search") })
                NavigationDrawerItem(label = { Text("Ernährungs-Analytics") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("nutrition_analytics") })
                NavigationDrawerItem(label = { Text("AI-Logs") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("logs") })
                NavigationDrawerItem(label = { Text("API-Schlüssel") }, selected = false, onClick = { scope.launch { drawerState.close() }; nav.navigate("apikeys") })
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
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Mehr Optionen")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Gespeicherte Rezepte") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("saved_recipes")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Bookmark, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Einkaufsliste") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("shopping_list")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Essen fotografieren") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("foodscan")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ernährungstagbuch") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("food_diary")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Fastfood, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ernährungs-Analytics") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("nutrition_analytics")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Insights, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gewicht tracken") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("weight_tracking")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Timeline, contentDescription = null)
                                }
                            )
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
                composable("plan") { PlanScreen(padding, nav) }
                composable("today") { TodayScreen(padding, nav) }
                composable("nutrition") { NutritionScreen(nav) }
                composable("progress") { ProgressScreen(padding) }
                composable("foodscan") {
                    FoodScanScreen(contentPadding = padding)
                }
                composable("logs") {
                    AiLogsScreen(padding)
                }
                composable("apikeys") {
                    ApiKeysScreen(padding)
                }
                composable("equipment") { 
                    EquipmentSelectionScreen(
                        selectedEquipment = emptyList(), // Let the screen load from UserPreferences
                        onEquipmentChanged = { }, // Equipment is saved automatically in UserPreferences
                        onBackPressed = { nav.popBackStack() }
                    )
                }
                composable("todaytraining") {
                    TodayTrainingScreen(
                        navController = nav,
                        onBackPressed = { nav.popBackStack() }
                    )
                }
                composable("saved_recipes") {
                    SavedRecipesScreen(
                        onBackPressed = { nav.popBackStack() },
                        onRecipeClick = { recipe ->
                            // Navigate to recipe details or cooking mode
                            nav.navigate("cooking_mode/${recipe.id}")
                        },
                        onCookRecipe = { recipe ->
                            nav.navigate("cooking_mode/${recipe.id}")
                        }
                    )
                }
                composable("cooking_mode/{recipeId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                    // We need to get the recipe from the database
                    CookingModeFromId(
                        recipeId = recipeId,
                        onBackPressed = { nav.popBackStack() },
                        onFinishCooking = { nav.popBackStack() }
                    )
                }
                composable("shopping_list") {
                    com.example.fitapp.ui.nutrition.EnhancedShoppingListScreen(
                        padding = padding,
                        onBackPressed = { nav.popBackStack() }
                    )
                }
                composable("training_execution/{planId}") { backStackEntry ->
                    val planId = backStackEntry.arguments?.getString("planId")?.toLongOrNull() ?: 0L
                    TrainingExecutionScreen(
                        planId = planId,
                        onBackPressed = { nav.popBackStack() },
                        onTrainingCompleted = { 
                            scope.launch {
                                try {
                                    val today = LocalDate.now()
                                    val dateIso = today.toString()
                                    val repo = NutritionRepository(AppDatabase.get(ctx))
                                    
                                    // Mark today's workout as completed
                                    repo.setWorkoutStatus(
                                        dateIso = dateIso,
                                        status = "completed",
                                        completedAt = System.currentTimeMillis() / 1000
                                    )
                                    
                                    // Trigger workout streak tracking
                                    val streakManager = com.example.fitapp.services.PersonalStreakManager(
                                        ctx, 
                                        com.example.fitapp.data.repo.PersonalMotivationRepository(AppDatabase.get(ctx))
                                    )
                                    streakManager.trackWorkoutCompletion(today)
                                } catch (e: Exception) {
                                    android.util.Log.e("MainScaffold", "Error saving training completion", e)
                                }
                            }
                            nav.popBackStack()
                        }
                    )
                }
                composable("daily_workout/{goal}/{minutes}") { backStackEntry ->
                    val goal = backStackEntry.arguments?.getString("goal") ?: "Muskelaufbau"
                    val minutes = backStackEntry.arguments?.getString("minutes")?.toIntOrNull() ?: 60
                    DailyWorkoutScreen(
                        goal = goal,
                        minutes = minutes,
                        onBackPressed = { nav.popBackStack() }
                    )
                }
                composable("weight_tracking") {
                    WeightTrackingScreen(
                        onBackPressed = { nav.popBackStack() }
                    )
                }
                composable("food_diary") {
                    FoodDiaryScreen(
                        contentPadding = padding,
                        onBackPressed = { nav.popBackStack() },
                        onAddFoodClick = { nav.navigate("food_search") }
                    )
                }
                composable("food_search") {
                    FoodSearchScreen(
                        contentPadding = padding,
                        onBackPressed = { nav.popBackStack() },
                        onFoodAdded = { nav.popBackStack() }
                    )
                }
                composable("nutrition_analytics") {
                    NutritionAnalyticsScreen(
                        contentPadding = padding,
                        onBackPressed = { nav.popBackStack() }
                    )
                }

            }
        }
    }
}

@Composable
private fun CookingModeFromId(
    recipeId: String,
    onBackPressed: () -> Unit,
    onFinishCooking: () -> Unit
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    var recipe by remember { mutableStateOf<com.example.fitapp.data.db.SavedRecipeEntity?>(null) }
    
    LaunchedEffect(recipeId) {
        recipe = db.savedRecipeDao().getRecipe(recipeId)
    }
    
    recipe?.let {
        CookingModeScreen(
            recipe = it,
            onBackPressed = onBackPressed,
            onFinishCooking = onFinishCooking
        )
    } ?: run {
        // Loading or error state
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
