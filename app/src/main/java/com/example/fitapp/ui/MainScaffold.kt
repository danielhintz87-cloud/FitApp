package com.example.fitapp.ui

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.fitapp.R
import com.example.fitapp.feature.hydration.navigation.hydrationGraph
import com.example.fitapp.feature.tracking.navigation.trackingGraph
import com.example.fitapp.ui.AiLogsScreen
import com.example.fitapp.ui.food.FoodScanScreen
import com.example.fitapp.ui.nutrition.*
import com.example.fitapp.ui.screens.*
import com.example.fitapp.ui.settings.*
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * MainScaffold - Central navigation hub for FitApp
 * 
 * Provides:
 * - ModalNavigationDrawer with unified dashboard navigation
 * - Context-sensitive top bar with drawer toggle
 * - NavHost with all app routes including feature modules
 * - Integration points for hydration and tracking graphs
 */
@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalGetImage
@Composable
fun MainScaffold(navController: NavHostController? = null) {
    val nav = navController ?: rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var showOverflowMenu by remember { mutableStateOf(false) }

    // User Experience Management
    val userExperienceManager = remember { com.example.fitapp.services.UserExperienceManager.getInstance(ctx) }
    val userExperienceState by userExperienceManager.userExperienceState.collectAsState()

    // Determine start destination based on user experience
    val startDestination = remember(userExperienceState) {
        when {
            !userExperienceState.hasCompletedOnboarding -> "onboarding"
            else -> "unified_dashboard"
        }
    }

    // Get current route for title updates
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Dynamic title based on current route
    val currentTitle = when {
        currentRoute == "unified_dashboard" -> "FitApp"
        currentRoute?.startsWith("plan") == true -> ctx.getString(R.string.training_plans)
        currentRoute?.startsWith("nutrition") == true -> ctx.getString(R.string.nutrition_recipes)
        currentRoute?.startsWith("enhanced_analytics") == true -> ctx.getString(R.string.progress_analytics)
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

                    // Main Dashboard - Consolidated
                    NavigationDrawerItem(
                        label = { Text("🏠 ${ctx.getString(R.string.dashboard)}") },
                        selected = currentRoute == "unified_dashboard" || currentRoute == "today",
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate("unified_dashboard")
                        },
                        icon = {
                            Icon(
                                Icons.Filled.Dashboard,
                                contentDescription = ctx.getString(R.string.icon_dashboard)
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("� ${ctx.getString(R.string.training_plans)}") },
                        selected = currentRoute?.startsWith("plan") == true || currentRoute?.startsWith("ai_personal_trainer") == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate("plan")
                        },
                        icon = {
                            Icon(
                                Icons.Filled.FitnessCenter,
                                contentDescription = ctx.getString(R.string.icon_fitness_center)
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("�️ ${ctx.getString(R.string.nutrition_recipes)}") },
                        selected = currentRoute?.startsWith("nutrition") == true || currentRoute?.startsWith("recipe") == true || currentRoute?.startsWith("enhanced_recipes") == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate("nutrition")
                        },
                        icon = {
                            Icon(
                                Icons.Filled.Restaurant,
                                contentDescription = ctx.getString(R.string.icon_restaurant)
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("📊 ${ctx.getString(R.string.progress_analytics)}") },
                        selected = currentRoute?.startsWith("progress") == true || currentRoute?.startsWith("enhanced_analytics") == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate("enhanced_analytics")
                        },
                        icon = {
                            Icon(
                                Icons.Filled.Insights,
                                contentDescription = ctx.getString(R.string.icon_insights)
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))

                    // Quick Actions and Tools
                    NavigationDrawerItem(
                        label = { Text("⚡ Schnellaktionen") },
                        selected = currentRoute == "quick_actions",
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate("quick_actions")
                        },
                        icon = { Icon(Icons.Filled.Bolt, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))

                    // Settings - Consolidated
                    NavigationDrawerItem(
                        label = { Text("⚙️ ${ctx.getString(R.string.settings)}") },
                        selected = currentRoute?.startsWith("apikeys") == true || currentRoute?.contains("settings") == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate("apikeys")
                        },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = ctx.getString(R.string.settings)) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
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
                            Icon(Icons.Filled.Menu, contentDescription = ctx.getString(R.string.menu))
                        }
                    },
                    actions = {
                        // Context-sensitive quick actions based on current screen
                        when {
                            currentRoute?.startsWith("nutrition") == true || currentRoute?.startsWith("recipe") == true -> {
                                IconButton(onClick = { nav.navigate("enhanced_recipes") }) {
                                    Icon(
                                        Icons.Filled.Restaurant,
                                        contentDescription = ctx.getString(R.string.all_recipes)
                                    )
                                }
                                IconButton(onClick = { nav.navigate("shopping_list") }) {
                                    Icon(
                                        Icons.Filled.ShoppingCart,
                                        contentDescription = ctx.getString(R.string.shopping_list)
                                    )
                                }
                            }
                            currentRoute?.startsWith("plan") == true || currentRoute?.startsWith("today") == true -> {
                                IconButton(onClick = { nav.navigate("ai_personal_trainer") }) {
                                    Icon(
                                        Icons.Filled.Psychology,
                                        contentDescription = ctx.getString(R.string.ai_trainer)
                                    )
                                }
                                IconButton(onClick = { nav.navigate("hiit_builder") }) {
                                    Icon(Icons.Filled.Timer, contentDescription = ctx.getString(R.string.hiit_builder))
                                }
                            }
                            else -> {
                                IconButton(onClick = { nav.navigate("food_search") }) {
                                    Icon(Icons.Filled.Search, contentDescription = ctx.getString(R.string.search_food))
                                }
                                IconButton(onClick = { nav.navigate("quick_actions") }) {
                                    Icon(Icons.Filled.Bolt, contentDescription = "Schnellaktionen")
                                }
                            }
                        }

                        // Settings dropdown
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = ctx.getString(R.string.more_options))
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(ctx.getString(R.string.settings)) },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("apikeys")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Settings, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(ctx.getString(R.string.health_connect)) },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("health_connect_settings")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.HealthAndSafety, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(ctx.getString(R.string.help_support)) },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("help")
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Feedback senden") },
                                onClick = {
                                    showOverflowMenu = false
                                    nav.navigate("feedback")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Feedback, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(ctx.getString(R.string.about_app)) },
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
        ) { padding ->
            NavHost(
                navController = nav, 
                startDestination = startDestination, 
                modifier = Modifier.fillMaxSize()
            ) {
                // Smart Onboarding Experience
                composable("onboarding") {
                    com.example.fitapp.ui.onboarding.SmartOnboardingScreen(
                        onOnboardingComplete = {
                            userExperienceManager.completeOnboarding()
                            nav.navigate("unified_dashboard") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }

                // UNIFIED DASHBOARD - Revolutionary Experience
                composable("unified_dashboard") {
                    UnifiedDashboardScreen(
                        contentPadding = padding,
                        onNavigateToFeature = { feature ->
                            // Track feature discovery
                            userExperienceManager.markFeatureDiscovered(feature)

                            when (feature) {
                                "nutrition" -> nav.navigate("nutrition")
                                "bmi_calculator" -> nav.navigate("bmi_calculator")
                                "fasting" -> nav.navigate("fasting")
                                "ai_personal_trainer" -> nav.navigate("ai_personal_trainer")
                                "barcode_scanner" -> nav.navigate("barcode_scanner")
                                "recipes" -> nav.navigate("recipes_enhanced")
                                "today_training" -> nav.navigate("todaytraining")
                                "enhanced_analytics" -> nav.navigate("enhanced_analytics")
                                "progress" -> nav.navigate("progress")
                                "health_sync" -> nav.navigate("health_connect_settings")
                                else -> nav.navigate(feature)
                            }
                        }
                    )
                }

                // Legacy Today Screen - Still available
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
                    FoodScanScreen(
                        contentPadding = padding,
                        onNavigateToApiKeys = { nav.navigate("apikeys") }
                    )
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

                // Enhanced Analytics
                composable("enhanced_analytics") {
                    EnhancedAnalyticsScreen(
                        navController = nav
                    )
                }

                // Quick Actions
                composable(
                    "quick_actions",
                    deepLinks = listOf(navDeepLink { uriPattern = "fitapp://quick_actions" })
                ) {
                    QuickActionsScreen(
                        onBack = { nav.popBackStack() },
                        onNavigateToAction = { route ->
                            nav.navigate(route)
                        }
                    )
                }

                // Feature module navigation graphs
                hydrationGraph(nav)
                trackingGraph(nav)
            }
        }
    }
}