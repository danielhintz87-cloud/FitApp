package com.example.fitapp.ui

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.example.fitapp.ui.nutrition.EnhancedRecipeListScreen
import com.example.fitapp.ui.nutrition.RecipeDetailScreen
import com.example.fitapp.ui.nutrition.RecipeEditScreen
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
import com.example.fitapp.ui.screens.HIITBuilderScreen
import com.example.fitapp.ui.screens.HIITExecutionScreen
import com.example.fitapp.ui.screens.SocialChallengesScreen
import com.example.fitapp.ui.settings.ApiKeysScreen
import com.example.fitapp.ui.settings.NotificationSettingsScreen
import com.example.fitapp.ui.settings.HealthConnectSettingsScreen
import com.example.fitapp.ui.settings.CloudSyncSettingsScreen
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.domain.entities.HIITWorkout
import kotlinx.coroutines.launch
import java.time.LocalDate

data class Destination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalGetImage
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp) // Add bottom padding for better scrolling
                ) {
                    Text(
                        "Navigation", 
                        style = MaterialTheme.typography.titleMedium, 
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                
                // AI Features Section
                Text(
                    text = "KI-Funktionen", 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("KI Personal Trainer") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("ai_personal_trainer") },
                    icon = { Icon(Icons.Filled.Psychology, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("KI Rezept Generator") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("nutrition") },
                    icon = { Icon(Icons.Filled.Fastfood, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("KI Trainingspläne") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("plan") },
                    icon = { Icon(Icons.Filled.Timeline, contentDescription = null) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Health & Fitness Section
                Text(
                    text = "Gesundheit & Fitness", 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("BMI Rechner") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("bmi_calculator") },
                    icon = { Icon(Icons.Filled.Calculate, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Abnehmprogramm") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("weight_loss_program") },
                    icon = { Icon(Icons.Filled.Flag, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Fortschritts-Analyse") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("enhanced_analytics") },
                    icon = { Icon(Icons.Filled.Insights, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Gewichtsverfolgung") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("weight_tracking") },
                    icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Social Challenges") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("social_challenges") },
                    icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Tools Section
                Text(
                    text = "Werkzeuge", 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Einkaufsliste") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("shopping_list") },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Lebensmittel Scanner") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("foodscan") },
                    icon = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Ernährungstagebuch") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("food_diary") },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Rezepte (Neu)") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("enhanced_recipes") },
                    icon = { Icon(Icons.Filled.Restaurant, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Gespeicherte Rezepte") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("saved_recipes") },
                    icon = { Icon(Icons.Filled.Bookmark, contentDescription = null) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Settings Section
                Text(
                    text = "Einstellungen", 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("API-Schlüssel Konfiguration") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("apikeys") },
                    icon = { Icon(Icons.Filled.Key, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Benachrichtigungen") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("notification_settings") },
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Health Connect") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("health_connect_settings") },
                    icon = { Icon(Icons.Filled.HealthAndSafety, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Cloud-Synchronisation") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("cloud_sync_settings") },
                    icon = { Icon(Icons.Filled.Cloud, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("KI Protokolle") }, 
                    selected = false, 
                    onClick = { scope.launch { drawerState.close() }; nav.navigate("logs") },
                    icon = { Icon(Icons.Filled.History, contentDescription = null) }
                )
                }
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
                                    nav.navigate("help")
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("about")
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
                val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
                val isFullscreenMode = currentRoute?.let { route ->
                    route.startsWith("cooking_mode") || route.startsWith("training_execution")
                } ?: false
                
                if (!isFullscreenMode) {
                    NavigationBar(modifier = Modifier.navigationBarsPadding()) {
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
                composable("notification_settings") {
                    NotificationSettingsScreen(navController = nav)
                }
                composable("health_connect_settings") {
                    HealthConnectSettingsScreen(navController = nav)
                }
                composable("help") { com.example.fitapp.ui.settings.HelpScreen(onBack = { nav.popBackStack() }) }
                composable("about") { com.example.fitapp.ui.settings.AboutScreen(onBack = { nav.popBackStack() }) }
                composable("cloud_sync_settings") {
                    CloudSyncSettingsScreen(
                        onNavigateBack = { nav.popBackStack() }
                    )
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
                composable("enhanced_recipes") {
                    EnhancedRecipeListScreen(
                        onBackPressed = { nav.popBackStack() },
                        onRecipeClick = { recipe ->
                            nav.navigate("recipe_detail/${recipe.id}")
                        },
                        onCookRecipe = { recipe ->
                            nav.navigate("cooking_mode/${recipe.id}")
                        },
                        onCreateRecipe = {
                            nav.navigate("recipe_edit")
                        }
                    )
                }
                composable("recipe_detail/{recipeId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                    RecipeDetailFromId(
                        recipeId = recipeId,
                        onBackPressed = { nav.popBackStack() },
                        onCookRecipe = { nav.navigate("cooking_mode/$recipeId") },
                        onEditRecipe = { nav.navigate("recipe_edit/$recipeId") }
                    )
                }
                composable("recipe_edit/{recipeId?}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId")
                    RecipeEditFromId(
                        recipeId = recipeId,
                        onBackPressed = { nav.popBackStack() },
                        onSaveRecipe = { nav.popBackStack() }
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
                composable("hiit_builder") {
                    HIITBuilderScreen(
                        onBackPressed = { nav.popBackStack() },
                        onWorkoutCreated = { workout ->
                            // Store workout temporarily for execution
                            // For now, navigate directly to execution
                            nav.navigate("hiit_execution")
                        }
                    )
                }
                composable("hiit_execution") {
                    // For demo purposes, create a sample workout
                    // In a real app, this would get the workout from state/database
                    val workoutManager = remember { com.example.fitapp.services.WorkoutManager(ctx) }
                    var sampleWorkout by remember { mutableStateOf<HIITWorkout?>(null) }
                    
                    LaunchedEffect(Unit) {
                        sampleWorkout = workoutManager.createDefaultHIITWorkouts().firstOrNull()
                    }
                    
                    sampleWorkout?.let { workout ->
                        HIITExecutionScreen(
                            workout = workout,
                            onBackPressed = { nav.popBackStack() },
                            onWorkoutCompleted = {
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
                                        android.util.Log.e("MainScaffold", "Error saving HIIT completion", e)
                                    }
                                }
                                nav.popBackStack()
                            }
                        )
                    }
                }
                composable("weight_tracking") {
                    WeightTrackingScreen(
                        onBackPressed = { nav.popBackStack() }
                    )
                }
                composable("social_challenges") {
                    SocialChallengesScreen(
                        modifier = Modifier.padding(padding)
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

@Composable
private fun RecipeDetailFromId(
    recipeId: String,
    onBackPressed: () -> Unit,
    onCookRecipe: () -> Unit,
    onEditRecipe: () -> Unit
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    var recipe by remember { mutableStateOf<com.example.fitapp.data.db.SavedRecipeEntity?>(null) }
    
    LaunchedEffect(recipeId) {
        recipe = db.savedRecipeDao().getRecipe(recipeId)
    }
    
    recipe?.let {
        RecipeDetailScreen(
            recipe = it,
            onBackPressed = onBackPressed,
            onCookRecipe = onCookRecipe,
            onEditRecipe = onEditRecipe
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

@Composable
private fun RecipeEditFromId(
    recipeId: String?,
    onBackPressed: () -> Unit,
    onSaveRecipe: () -> Unit
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val scope = rememberCoroutineScope()
    var recipe by remember { mutableStateOf<com.example.fitapp.data.db.SavedRecipeEntity?>(null) }
    var isLoaded by remember { mutableStateOf(recipeId == null) }
    
    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            recipe = db.savedRecipeDao().getRecipe(recipeId)
            isLoaded = true
        }
    }
    
    if (isLoaded) {
        RecipeEditScreen(
            recipe = recipe,
            onBackPressed = onBackPressed,
            onSaveRecipe = { savedRecipe ->
                scope.launch {
                    if (recipe != null) {
                        db.savedRecipeDao().update(savedRecipe)
                    } else {
                        db.savedRecipeDao().insert(savedRecipe)
                    }
                    onSaveRecipe()
                }
            }
        )
    } else {
        // Loading state
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
