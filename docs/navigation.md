# Navigation Standards Guide

This guide covers navigation patterns, route naming conventions, argument passing, and deep linking in the FitApp project.

## Overview

FitApp uses **Jetpack Compose Navigation** with a drawer-based navigation system. The app follows consistent route naming patterns and centralized navigation management.

## Route Naming Conventions

### Primary Routes

```kotlin
// Main sections - use underscore_case for multi-word routes
"unified_dashboard"       // Main dashboard
"plan"                   // Training plans
"nutrition"              // Nutrition hub
"enhanced_analytics"     // Progress analytics
"api_keys"              // Settings
```

### Sub-routes

```kotlin
// Sub-routes use parent_section/sub_feature pattern
"nutrition/food_diary"
"nutrition/recipe_detail/{recipeId}"
"nutrition/cooking_mode/{recipeId}"
"plan/hiit_builder"
"plan/training_execution/{planId}"
"enhanced_analytics/weight_tracking"
```

### Route Naming Rules

- **Primary routes**: `section_name` (snake_case)
- **Sub-routes**: `section/sub_feature` or `section/feature_{param}`
- **Parameters**: `{paramName}` in camelCase
- **Optional params**: `{paramName?}` with default values
- **Query params**: Avoid - use route arguments instead

## Navigation Structure

### NavHost Setup

```kotlin
@Composable
fun MainScaffold() {
    val nav = rememberNavController()
    val startDestination = "unified_dashboard"
    
    NavHost(
        navController = nav,
        startDestination = startDestination
    ) {
        // Primary destinations
        composable("unified_dashboard") { UnifiedDashboardScreen(nav) }
        composable("nutrition") { EnhancedNutritionHubScreen(nav) }
        composable("plan") { PlanScreen(nav) }
        
        // Sub-destinations with parameters
        composable(
            route = "nutrition/recipe_detail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
            RecipeDetailScreen(nav, recipeId)
        }
    }
}
```

### Current Navigation Architecture

```
Main Scaffold (Drawer Navigation)
├── unified_dashboard (Dashboard)
├── plan (Training & Plans)
│   ├── ai_personal_trainer
│   ├── hiit_builder
│   ├── hiit_execution/{workoutId}
│   ├── training_execution/{planId}
│   └── equipment_selection
├── nutrition (Nutrition & Recipes)
│   ├── food_diary
│   ├── food_search
│   ├── food_scan
│   ├── recipe_generation
│   ├── enhanced_recipes
│   ├── recipe_detail/{recipeId}
│   ├── recipe_edit/{recipeId}
│   ├── cooking_mode/{recipeId}
│   └── nutrition_analytics
├── enhanced_analytics (Progress & Analytics)
│   ├── weight_tracking
│   ├── bmi_calculator
│   └── weight_loss_program
└── settings
    ├── api_keys
    ├── notification_settings
    ├── health_connect_settings
    └── cloud_sync_settings
```

## Argument Passing

### Simple Arguments

```kotlin
// Define route with parameter
composable(
    route = "recipe_detail/{recipeId}",
    arguments = listOf(
        navArgument("recipeId") {
            type = NavType.StringType
            nullable = false
        }
    )
) { backStackEntry ->
    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
    RecipeDetailScreen(navController, recipeId)
}

// Navigate with argument
navController.navigate("recipe_detail/recipe_123")
```

### Multiple Arguments

```kotlin
// Route with multiple parameters
composable(
    route = "training_execution/{planId}/{sessionId}",
    arguments = listOf(
        navArgument("planId") { type = NavType.IntType },
        navArgument("sessionId") { type = NavType.StringType }
    )
) { backStackEntry ->
    val planId = backStackEntry.arguments?.getInt("planId") ?: return@composable
    val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
    TrainingExecutionScreen(navController, planId, sessionId)
}

// Navigate with multiple arguments
navController.navigate("training_execution/42/session_abc123")
```

### Optional Arguments

```kotlin
// Route with optional parameter
composable(
    route = "food_diary?date={date}",
    arguments = listOf(
        navArgument("date") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )
) { backStackEntry ->
    val date = backStackEntry.arguments?.getString("date")
    FoodDiaryScreen(navController, date)
}

// Navigate with or without optional parameter
navController.navigate("food_diary") // Uses current date
navController.navigate("food_diary?date=2024-01-15") // Specific date
```

### Complex Objects

For complex objects, use a shared ViewModel or pass IDs and reload:

```kotlin
// ❌ Don't pass complex objects directly
navController.navigate("recipe_edit/$complexRecipeObject")

// ✅ Pass ID and reload in destination
navController.navigate("recipe_edit/${recipe.id}")

// In destination screen
@Composable
fun RecipeEditScreen(navController: NavController, recipeId: String) {
    val viewModel: RecipeViewModel = hiltViewModel()
    val recipe by viewModel.getRecipe(recipeId).collectAsState(initial = null)
    
    recipe?.let { 
        RecipeEditContent(it) { updatedRecipe ->
            viewModel.updateRecipe(updatedRecipe)
            navController.popBackStack()
        }
    }
}
```

## Deep Linking

### Deep Link Setup

```kotlin
// In MainActivity or navigation setup
composable(
    route = "recipe_detail/{recipeId}",
    arguments = listOf(navArgument("recipeId") { type = NavType.StringType }),
    deepLinks = listOf(
        navDeepLink {
            uriPattern = "fitapp://recipe/{recipeId}"
        },
        navDeepLink {
            uriPattern = "https://fitapp.example.com/recipes/{recipeId}"
        }
    )
) { backStackEntry ->
    // Handle deep link navigation
    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
    RecipeDetailScreen(navController, recipeId)
}
```

### Deep Link Patterns

```kotlin
// App-specific deep links
"fitapp://dashboard"
"fitapp://nutrition/recipes"
"fitapp://training/plan/{planId}"
"fitapp://recipe/{recipeId}"

// Web deep links (for sharing)
"https://fitapp.example.com/dashboard"
"https://fitapp.example.com/recipes/{recipeId}"
"https://fitapp.example.com/workouts/{planId}"
```

### Manifest Configuration

```xml
<!-- In AndroidManifest.xml -->
<activity android:name=".MainActivity">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="fitapp" />
    </intent-filter>
    
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https"
              android:host="fitapp.example.com" />
    </intent-filter>
</activity>
```

## Navigation Patterns

### Back Stack Management

```kotlin
// Navigate and clear back stack to specific destination
navController.navigate("dashboard") {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}

// Navigate and replace current screen
navController.navigate("new_screen") {
    popUpTo("current_screen") { inclusive = true }
}

// Navigate up/back
navController.navigateUp()
navController.popBackStack()
```

### Drawer Navigation

```kotlin
@Composable
fun NavigationDrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val currentRoute = navController.currentDestination?.route
    
    NavigationDrawerItem(
        label = { Text("Dashboard") },
        selected = currentRoute == "unified_dashboard",
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("unified_dashboard") {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") }
    )
}
```

### Conditional Navigation

```kotlin
@Composable
fun ConditionalNavigation(navController: NavController) {
    val userExperienceState by userExperienceManager.userExperienceState.collectAsState()
    
    LaunchedEffect(userExperienceState) {
        val destination = when {
            !userExperienceState.hasCompletedOnboarding -> "onboarding"
            userExperienceState.shouldShowWhatsNew -> "whats_new"
            else -> "unified_dashboard"
        }
        
        navController.navigate(destination) {
            popUpTo(0) { inclusive = true }
        }
    }
}
```

## Screen Templates

### Basic Screen Template

```kotlin
@Composable
fun NewFeatureScreen(
    navController: NavController,
    // Include any required parameters
    featureId: String? = null
) {
    // 1. State management
    var isLoading by remember { mutableStateOf(false) }
    val viewModel: NewFeatureViewModel = hiltViewModel()
    
    // 2. Side effects
    LaunchedEffect(featureId) {
        featureId?.let { viewModel.loadFeature(it) }
    }
    
    // 3. UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top app bar
        TopAppBar(
            title = { Text("New Feature") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        // Content
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            NewFeatureContent(
                onNavigateToDetail = { id ->
                    navController.navigate("new_feature/detail/$id")
                }
            )
        }
    }
}
```

### Screen with Parameters Template

```kotlin
@Composable
fun DetailScreen(
    navController: NavController,
    itemId: String,
    editMode: Boolean = false
) {
    val viewModel: DetailViewModel = hiltViewModel()
    val item by viewModel.getItem(itemId).collectAsState(initial = null)
    
    item?.let { currentItem ->
        if (editMode) {
            EditContent(
                item = currentItem,
                onSave = { updatedItem ->
                    viewModel.updateItem(updatedItem)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        } else {
            DetailContent(
                item = currentItem,
                onEdit = { 
                    navController.navigate("detail/${itemId}/edit")
                },
                onDelete = {
                    viewModel.deleteItem(itemId)
                    navController.popBackStack()
                }
            )
        }
    } ?: LoadingIndicator()
}
```

## Best Practices

### DO ✅

- Use descriptive, consistent route names
- Include type information for navigation arguments
- Handle null/missing arguments gracefully
- Use `LaunchedEffect` for navigation side effects
- Implement proper back stack management
- Test deep links on different Android versions
- Use `hiltViewModel()` for dependency injection
- Handle loading states during navigation

### DON'T ❌

- Pass complex objects through navigation arguments
- Use magic strings for routes (create constants)
- Ignore back stack state management
- Navigate from background threads without proper scope
- Create circular navigation loops
- Skip null checks for navigation arguments
- Hardcode deep link URLs

### Navigation Constants

```kotlin
object NavigationRoutes {
    const val DASHBOARD = "unified_dashboard"
    const val NUTRITION = "nutrition"
    const val RECIPE_DETAIL = "nutrition/recipe_detail/{recipeId}"
    const val RECIPE_EDIT = "nutrition/recipe_edit/{recipeId}"
    const val TRAINING_EXECUTION = "plan/training_execution/{planId}"
    
    // Helper functions
    fun recipeDetail(recipeId: String) = "nutrition/recipe_detail/$recipeId"
    fun recipeEdit(recipeId: String) = "nutrition/recipe_edit/$recipeId"
    fun trainingExecution(planId: Int) = "plan/training_execution/$planId"
}
```

### Error Handling

```kotlin
@Composable
fun NavigationErrorBoundary(
    navController: NavController,
    content: @Composable () -> Unit
) {
    try {
        content()
    } catch (e: Exception) {
        // Log error and navigate to safe state
        Log.e("Navigation", "Navigation error", e)
        LaunchedEffect(e) {
            navController.navigate("dashboard") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
```

---

**Next Steps**: When adding new screens, follow these patterns to ensure consistent navigation behavior and user experience.