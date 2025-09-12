package com.example.fitapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.services.*
import com.example.fitapp.ui.MainScaffold
import com.example.fitapp.ui.theme.FitAppTheme
import com.example.fitapp.util.PerformanceMonitor
import com.example.fitapp.util.StructuredLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isInitialized = false
    private var networkMonitor: NetworkStateMonitor? = null
    private lateinit var navController: NavHostController
    private var pendingDeepLink: String? = null

    // Permission launcher for POST_NOTIFICATIONS
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYSTEM,
                    "MainActivity",
                    "Notification permission granted",
                )
                // Schedule default reminder workers when permission is granted
                scheduleDefaultReminders()
            } else {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.SYSTEM,
                    "MainActivity",
                    "Notification permission denied",
                )
            }
        }

    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Initialize logging system first
            StructuredLogger.initialize(this, enableFileLogging = true, StructuredLogger.LogLevel.INFO)
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "App starting")

            // Handle deep links
            handleDeepLink(intent)

            // Set content immediately for faster UI loading
            setContent {
                navController = rememberNavController()
                    MainScaffold()

                // Navigate to deep link if pending
                LaunchedEffect(pendingDeepLink) {
                    pendingDeepLink?.let { deepLink ->
                        StructuredLogger.info(
                            StructuredLogger.LogCategory.SYSTEM,
                            "MainActivity",
                            "Navigating to deep link: $deepLink",
                        )
                        navController.navigate(deepLink)
                        pendingDeepLink = null
                    }
                }
            }

            // Initialize critical services in background
            initializeCriticalServices()

            // Request notification permission for Android 13+
            requestNotificationPermissionIfNeeded()

            // Initialize remaining data asynchronously
            initializeDataAsync()
        } catch (e: Exception) {
            StructuredLogger.critical(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "Critical error in onCreate",
                exception = e,
            )
            // Fallback UI in case of critical errors
            setContent {
                FitAppTheme {
                    Surface {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Fehler beim Laden der App")
                            Text("Bitte starten Sie die App neu")
                            Button(onClick = { finish() }) {
                                Text("App beenden")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)

        // Navigate immediately if nav controller is ready
        pendingDeepLink?.let { deepLink ->
            if (::navController.isInitialized) {
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYSTEM,
                    "MainActivity",
                    "Navigating to deep link via onNewIntent: $deepLink",
                )
                navController.navigate(deepLink)
                pendingDeepLink = null
            }
        }
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Processing deep link: $uri")
            pendingDeepLink = parseDeepLink(uri)
        }
    }

    private fun parseDeepLink(uri: Uri): String {
        return when {
            uri.scheme == "fitapp" -> {
                when (uri.host) {
                    "dashboard", "today" -> "unified_dashboard"
                    "nutrition" -> "nutrition"
                    "training", "plan" -> "plan"
                    "analytics", "progress" -> "enhanced_analytics"
                    "settings" -> "apikeys"
                    "recipes" -> "enhanced_recipes"
                    "ai_trainer" -> "ai_personal_trainer"
                    "hiit" -> "hiit_builder"
                    "food_search" -> "food_search"
                    "bmi" -> "bmi_calculator"
                    "weight" -> "weight_tracking"
                    "help" -> "help"
                    "about" -> "about"
                    "fasting" -> "fasting"
                    "barcode" -> "barcode_scanner"
                    "shopping" -> "shopping_list"
                    else -> {
                        StructuredLogger.warning(
                            StructuredLogger.LogCategory.SYSTEM,
                            "MainActivity",
                            "Unknown deep link host: ${uri.host}",
                        )
                        "unified_dashboard" // Default fallback
                    }
                }
            }
            else -> {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.SYSTEM,
                    "MainActivity",
                    "Unsupported deep link scheme: ${uri.scheme}",
                )
                "unified_dashboard" // Default fallback
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> {
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYSTEM,
                        "MainActivity",
                        "Notification permission already granted",
                    )
                    // Schedule default reminders if permission already granted
                    scheduleDefaultReminders()
                }
                PackageManager.PERMISSION_DENIED -> {
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYSTEM,
                        "MainActivity",
                        "Requesting notification permission",
                    )
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android < 13, no runtime permission needed - schedule reminders directly
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "No notification permission needed for this Android version",
            )
            scheduleDefaultReminders()
        }
    }

    private fun scheduleDefaultReminders() {
        try {
            // Schedule water reminders by default
            WaterReminderWorker.scheduleWaterReminders(this)
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Water reminders scheduled")
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "Failed to schedule water reminders",
                exception = e,
            )
        }

        // Note: Workout and nutrition reminders are typically scheduled when user enables them in settings
        // Daily motivation is already scheduled in initializeCriticalServices()
    }

    private fun initializeCriticalServices() {
        try {
            // Initialize notification channels with error handling
            SmartNotificationManager.createNotificationChannels(this)
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "Notification channels initialized",
            )
        } catch (e: Exception) {
            // Log error but don't crash the app
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "Failed to create notification channels",
                exception = e,
            )
        }

        try {
            // Schedule daily work with error handling
            DailyMotivationWorker.scheduleWork(this)
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Daily work scheduled")
        } catch (e: Exception) {
            // Log error but don't crash the app
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "Failed to schedule daily work",
                exception = e,
            )
        }

        try {
            // Initialize network monitoring
            networkMonitor =
                NetworkStateMonitor(this).apply {
                    startMonitoring()
                }
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Network monitoring initialized")
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "Failed to initialize network monitoring",
                exception = e,
            )
        }

        try {
            // Schedule periodic sync
            OfflineSyncManager.schedulePeriodicSync(this)
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Offline sync scheduled")
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "Failed to schedule offline sync",
                exception = e,
            )
        }
    }

    private fun initializeDataAsync() {
        if (isInitialized) return

        lifecycleScope.launch {
            try {
                // Start performance monitoring
                PerformanceMonitor.startMonitoring(this@MainActivity, lifecycleScope)

                // Initialize database and repositories with timeout and performance monitoring
                val startTime = System.currentTimeMillis()
                try {
                    withTimeout(10_000) { // 10 second timeout
                        val database = AppDatabase.get(this@MainActivity)
                        val repository = PersonalMotivationRepository(database)
                        val nutritionRepository = NutritionRepository(database, this@MainActivity)
                        val achievementManager = PersonalAchievementManager(this@MainActivity, repository)
                        val streakManager = PersonalStreakManager(this@MainActivity, repository)

                        // Initialize default achievements and streaks
                        achievementManager.initializeDefaultAchievements()
                        streakManager.initializeDefaultStreaks()

                        // Initialize default food database
                        nutritionRepository.initializeDefaultFoodDatabase()

                        isInitialized = true
                        StructuredLogger.info(
                            StructuredLogger.LogCategory.SYSTEM,
                            "MainActivity",
                            "Personal motivation data initialized successfully",
                        )

                        // Record performance
                        val duration = System.currentTimeMillis() - startTime
                        PerformanceMonitor.recordDatabaseOperation("app_initialization", duration)
                    }
                } catch (e: Exception) {
                    val duration = System.currentTimeMillis() - startTime
                    PerformanceMonitor.recordDatabaseOperation("app_initialization_failed", duration)
                    throw e
                }
            } catch (e: TimeoutCancellationException) {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.SYSTEM,
                    "MainActivity",
                    "Timeout initializing personal motivation data",
                    exception = e,
                )
                // Retry with simpler initialization
                retrySimpleInitialization()
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYSTEM,
                    "MainActivity",
                    "Failed to initialize personal motivation data",
                    exception = e,
                )
                // Don't crash, app can still function without this data
                retrySimpleInitialization()
            }
        }
    }

    private suspend fun retrySimpleInitialization() {
        try {
            // Minimal initialization for basic app functionality
            val database = AppDatabase.get(this@MainActivity)
            // Just ensure database is accessible
            database.openHelper.writableDatabase
            isInitialized = true
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "Simple initialization completed",
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "Even simple initialization failed",
                exception = e,
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            // Clean up network monitoring
            networkMonitor?.stopMonitoring()
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Network monitoring stopped")
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                "MainActivity",
                "Error stopping network monitoring",
                exception = e,
            )
        }
    }
}
