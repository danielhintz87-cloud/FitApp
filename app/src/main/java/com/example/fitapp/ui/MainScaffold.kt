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
import com.example.fitapp.ui.screens.EnhancedTrainingHubScreen
import com.example.fitapp.ui.screens.EnhancedNutritionHubScreen
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

// Simplified navigation structure - no more bottom navigation
// Main destinations are managed through drawer only

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalGetImage
@Composable
fun MainScaffold() {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var showOverflowMenu by remember { mutableStateOf(false) }
    
    // Get current route for title updates
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Dynamic title based on current screen
    val currentTitle = when {
        currentRoute?.startsWith("today") == true -> "Heute"
        currentRoute?.startsWith("plan") == true -> "Training & Pläne"
        currentRoute?.startsWith("nutrition") == true || currentRoute?.startsWith("recipe") == true || currentRoute?.startsWith("cooking") == true -> "Ernährung"
        currentRoute?.startsWith("progress") == true || currentRoute?.startsWith("enhanced_analytics") == true -> "Fortschritt"
        currentRoute?.startsWith("weight") == true || currentRoute?.startsWith("bmi") == true -> "Gesundheit"
        currentRoute?.startsWith("food") == true -> "Ernährungstagebuch"
        currentRoute?.startsWith("shopping") == true -> "Einkaufsliste"
        currentRoute?.startsWith("ai_personal_trainer") == true -> "KI Personal Trainer"
        currentRoute?.startsWith("hiit") == true -> "HIIT Training"
        currentRoute?.startsWith("apikeys") == true || currentRoute?.contains("settings") == true -> "Einstellungen"
        else -> "FitApp"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    // Header with app name
                    Text(
                        "FitApp", 
                        style = MaterialTheme.typography.headlineSmall, 
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Main Features - Primary Navigation
                    NavigationDrawerItem(
                        label = { Text("🏠 Dashboard") }, 
                        selected = currentRoute == "today", 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("today") },
                        icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("🎯 Training & Pläne") }, 
                        selected = currentRoute?.startsWith("plan") == true || currentRoute?.startsWith("ai_personal_trainer") == true, 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("plan") },
                        icon = { Icon(Icons.Filled.FitnessCenter, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("🍽️ Ernährung & Rezepte") }, 
                        selected = currentRoute?.startsWith("nutrition") == true || currentRoute?.startsWith("recipe") == true || currentRoute?.startsWith("enhanced_recipes") == true, 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("nutrition") },
                        icon = { Icon(Icons.Filled.Restaurant, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("📊 Fortschritt & Analytics") }, 
                        selected = currentRoute?.startsWith("progress") == true || currentRoute?.startsWith("enhanced_analytics") == true, 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("enhanced_analytics") },
                        icon = { Icon(Icons.Filled.Insights, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                    
                    // Quick Access Tools
                    Text(
                        text = "Schnellzugriff", 
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("📱 Lebensmittel Scanner") }, 
                        selected = false, 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("foodscan") },
                        icon = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("🛒 Einkaufsliste") }, 
                        selected = false, 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("shopping_list") },
                        icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("📖 Ernährungstagebuch") }, 
                        selected = currentRoute?.startsWith("food_diary") == true, 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("food_diary") },
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("⚖️ Gewichtsverfolgung") }, 
                        selected = currentRoute?.startsWith("weight_tracking") == true, 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("weight_tracking") },
                        icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                    
                    // Settings & Configuration
                    Text(
                        text = "Einstellungen", 
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("🔧 App-Einstellungen") }, 
                        selected = currentRoute?.startsWith("apikeys") == true, 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("apikeys") },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("🔔 Benachrichtigungen") }, 
                        selected = false, 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("notification_settings") },
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("☁️ Cloud Sync") }, 
                        selected = false, 
                        onClick = { scope.launch { drawerState.close() }; nav.navigate("cloud_sync_settings") },
                        icon = { Icon(Icons.Filled.Cloud, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(currentTitle) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menü")
                        }
                    },
                    actions = {
                        // Context-sensitive quick actions based on current screen
                        when {
                            currentRoute?.startsWith("nutrition") == true || currentRoute?.startsWith("recipe") == true -> {
                                IconButton(onClick = { nav.navigate("enhanced_recipes") }) {
                                    Icon(Icons.Filled.Restaurant, contentDescription = "Alle Rezepte")
                                }
                                IconButton(onClick = { nav.navigate("shopping_list") }) {
                                    Icon(Icons.Filled.ShoppingCart, contentDescription = "Einkaufsliste")
                                }
                            }
                            currentRoute?.startsWith("plan") == true || currentRoute?.startsWith("today") == true -> {
                                IconButton(onClick = { nav.navigate("ai_personal_trainer") }) {
                                    Icon(Icons.Filled.Psychology, contentDescription = "KI Trainer")
                                }
                                IconButton(onClick = { nav.navigate("hiit_builder") }) {
                                    Icon(Icons.Filled.Timer, contentDescription = "HIIT Builder")
                                }
                            }
                            else -> {
                                IconButton(onClick = { nav.navigate("food_search") }) {
                                    Icon(Icons.Filled.Search, contentDescription = "Lebensmittel suchen")
                                }
                            }
                        }
                        
                        // Settings dropdown
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Mehr Optionen")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Einstellungen") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("apikeys")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Settings, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Health Connect") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("health_connect_settings")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.HealthAndSafety, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hilfe & Support") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("help")
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Über die App") },
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
            }
            // REMOVED: Bottom Navigation Bar - Navigation is now purely drawer-based
        ) { padding ->
            NavHost(navController = nav, startDestination = "today", modifier = Modifier.fillMaxSize()) {
                // Main Dashboard - Starting point
                composable("today") {
                    TodayScreen(
                        contentPadding = padding,
                        onNavigateToTodayTraining = { nav.navigate("todaytraining") },
                        onNavigateToDailyWorkout = { goal, minutes -> nav.navigate("daily_workout/$goal/$minutes") },
                        onNavigateToHiitBuilder = { nav.navigate("hiit_builder") }
                    )
                }
                
                // Training & Plans Section
                composable("plan") {
                    EnhancedTrainingHubScreen(
                        navController = nav,
                        contentPadding = padding
                    )
                }
                
                // Nutrition & Recipes Section  
                composable("nutrition") {
                    EnhancedNutritionHubScreen(
                        navController = nav,
                        contentPadding = padding
                    )
                }
                
                // Progress & Analytics
                composable("progress") { ProgressScreen(padding) }
                composable("foodscan") {
                    FoodScanScreen(padding)
                }
                composable("logs") {
                    AiLogsScreen(padding)
                }
                composable("apikeys") { ApiKeysScreen(padding) }
                composable("notification_settings") { NotificationSettingsScreen(onBack = { nav.popBackStack() }) }
                composable("health_connect_settings") {
                    HealthConnectSettingsScreen()
                }
                composable("help") { com.example.fitapp.ui.settings.HelpScreen(onBack = { nav.popBackStack() }) }
                composable("about") { com.example.fitapp.ui.settings.AboutScreen(onBack = { nav.popBackStack() }) }
                composable("cloud_sync_settings") { CloudSyncSettingsScreen(onNavigateBack = { nav.popBackStack() }) }
                composable("equipment") { EquipmentSelectionScreen(selectedEquipment = emptyList(), onEquipmentChanged = { }, onBackPressed = { nav.popBackStack() }) }
                composable("todaytraining") {
                    TodayTrainingScreen(
                        onBackPressed = { nav.popBackStack() },
                        onNavigateToDailyWorkout = { goal, minutes -> nav.navigate("daily_workout/$goal/$minutes") }
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
                        },
                        
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
                        },
                        
                    )
                }
                composable("recipe_detail/{recipeId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                    RecipeDetailFromId(
                        recipeId = recipeId,
                        onBackPressed = { nav.popBackStack() },
                        onCookRecipe = { nav.navigate("cooking_mode/$recipeId") },
                        onEditRecipe = { nav.navigate("recipe_edit/$recipeId") },
                        
                    )
                }
                composable("recipe_edit/{recipeId?}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId")
                    RecipeEditFromId(
                        recipeId = recipeId,
                        onBackPressed = { nav.popBackStack() },
                        onSaveRecipe = { nav.popBackStack() },
                        
                    )
                }
                composable("cooking_mode/{recipeId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                    // We need to get the recipe from the database
                    CookingModeFromId(
                        recipeId = recipeId,
                        onBackPressed = { nav.popBackStack() },
                        onFinishCooking = { nav.popBackStack() },
                        
                    )
                }
                composable("shopping_list") {
                    com.example.fitapp.ui.nutrition.EnhancedShoppingListScreen(
                        
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
                        onBackPressed = { nav.popBackStack() },
                        
                    )
                }
                composable("hiit_builder") {
                    HIITBuilderScreen(
                        onBackPressed = { nav.popBackStack() },
                        onWorkoutCreated = { workout ->
                            // Store workout temporarily for execution
                            // For now, navigate directly to execution
                            nav.navigate("hiit_execution")
                        },
                        
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
                        onBackPressed = { nav.popBackStack() },
                        
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
                        
                        navController = nav
                    )
                }
                composable("bmi_calculator") {
                    BMICalculatorScreen(
                        onBack = { nav.popBackStack() },
                        onWeightLossProgramSuggested = { bmi, targetWeight -> nav.navigate("weight_loss_program/$bmi/$targetWeight") }
                    )
                }
                composable("weight_loss_program/{bmi}/{targetWeight}") { backStackEntry ->
                    val bmi = backStackEntry.arguments?.getString("bmi")?.toFloatOrNull()
                    val targetWeight = backStackEntry.arguments?.getString("targetWeight")?.toFloatOrNull()
                    WeightLossProgramScreen(onBack = { nav.popBackStack() }, initialBMI = bmi, initialTargetWeight = targetWeight)
                }
                composable("weight_loss_program") { WeightLossProgramScreen(onBack = { nav.popBackStack() }) }
                composable("ai_personal_trainer") {
                    AIPersonalTrainerScreen(onBack = { nav.popBackStack() })
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
