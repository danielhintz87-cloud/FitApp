package com.example.fitapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.fitapp.ui.screens.BMICalculatorScreen
import com.example.fitapp.ui.screens.WeightLossProgramScreen
import com.example.fitapp.ui.screens.AIPersonalTrainerScreen
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
                
                // AI Features Section
                Text(
                    text = "AI Features", 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("AI Personal Trainer") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("ai_personal_trainer") },
                    icon = { Icon(Icons.Filled.Psychology, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("AI Recipe Generator") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("nutrition") },
                    icon = { Icon(Icons.Filled.Fastfood, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("AI Workout Plans") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("plan") },
                    icon = { Icon(Icons.Filled.Timeline, contentDescription = null) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Health & Fitness Section
                Text(
                    text = "Health & Fitness", 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("BMI Calculator") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("bmi_calculator") },
                    icon = { Icon(Icons.Filled.Calculate, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Weight Loss Program") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("weight_loss_program") },
                    icon = { Icon(Icons.Filled.Flag, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Progress Analytics") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("enhanced_analytics") },
                    icon = { Icon(Icons.Filled.Insights, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Weight Tracking") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("weight_tracking") },
                    icon = { Icon(Icons.Filled.TrendingUp, contentDescription = null) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Tools Section
                Text(
                    text = "Tools", 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Shopping List") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("shopping_list") },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Food Scanner") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("foodscan") },
                    icon = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Food Diary") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("food_diary") },
                    icon = { Icon(Icons.Filled.MenuBook, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Saved Recipes") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("saved_recipes") },
                    icon = { Icon(Icons.Filled.Bookmark, contentDescription = null) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Settings Section
                Text(
                    text = "Settings", 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("API Key Configuration") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("apikeys") },
                    icon = { Icon(Icons.Filled.Key, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("AI Logs") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("logs") },
                    icon = { Icon(Icons.Filled.History, contentDescription = null) }
                )
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
                                text = { Text("Quick Settings") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("apikeys")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Settings, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Food Search") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("food_search")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Search, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Help & Support") },
                                onClick = {
                                    showOverflowMenu = false
                                    // TODO: Add help/support screen
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Help, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = {
                                    showOverflowMenu = false
                                    // TODO: Add about screen
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Info, contentDescription = null)
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
                composable("enhanced_analytics") {
                    com.example.fitapp.ui.screens.EnhancedAnalyticsScreen(
                        contentPadding = padding,
                        navController = nav
                    )
                }
                composable("bmi_calculator") {
                    BMICalculatorScreen(
                        navController = nav,
                        onWeightLossProgramSuggested = { bmi, targetWeight ->
                            nav.navigate("weight_loss_program/$bmi/$targetWeight")
                        }
                    )
                }
                composable("weight_loss_program/{bmi}/{targetWeight}") { backStackEntry ->
                    val bmi = backStackEntry.arguments?.getString("bmi")?.toFloatOrNull()
                    val targetWeight = backStackEntry.arguments?.getString("targetWeight")?.toFloatOrNull()
                    WeightLossProgramScreen(
                        navController = nav,
                        initialBMI = bmi,
                        initialTargetWeight = targetWeight
                    )
                }
                composable("weight_loss_program") {
                    WeightLossProgramScreen(navController = nav)
                }
                composable("ai_personal_trainer") {
                    AIPersonalTrainerScreen(navController = nav)
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
