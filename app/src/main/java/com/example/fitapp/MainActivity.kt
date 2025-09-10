package com.example.fitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.services.*
import com.example.fitapp.ui.MainScaffold
import com.example.fitapp.ui.theme.FitAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import com.example.fitapp.util.PerformanceMonitor
import kotlinx.coroutines.withTimeout
import com.example.fitapp.util.StructuredLogger

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isInitialized = false
    private var networkMonitor: NetworkStateMonitor? = null
    
    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Initialize logging system first
            StructuredLogger.initialize(this, enableFileLogging = true, StructuredLogger.LogLevel.INFO)
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "App starting")
            
            // Set content immediately for faster UI loading
            setContent { MainScaffold() }
            
            // Initialize critical services in background
            initializeCriticalServices()
            
            // Initialize remaining data asynchronously
            initializeDataAsync()
            
        } catch (e: Exception) {
            StructuredLogger.critical(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Critical error in onCreate", exception = e)
            // Fallback UI in case of critical errors
            setContent { 
                FitAppTheme {
                    Surface {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
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
    
    private fun initializeCriticalServices() {
        try {
            // Initialize notification channels with error handling
            SmartNotificationManager.createNotificationChannels(this)
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Notification channels initialized")
        } catch (e: Exception) {
            // Log error but don't crash the app
            StructuredLogger.error(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Failed to create notification channels", exception = e)
        }
        
        try {
            // Schedule daily work with error handling
            DailyMotivationWorker.scheduleWork(this)
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Daily work scheduled")
        } catch (e: Exception) {
            // Log error but don't crash the app
            StructuredLogger.error(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Failed to schedule daily work", exception = e)
        }
        
        try {
            // Initialize network monitoring
            networkMonitor = NetworkStateMonitor(this).apply {
                startMonitoring()
            }
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Network monitoring initialized")
        } catch (e: Exception) {
            StructuredLogger.error(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Failed to initialize network monitoring", exception = e)
        }
        
        try {
            // Schedule periodic sync
            OfflineSyncManager.schedulePeriodicSync(this)
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Offline sync scheduled")
        } catch (e: Exception) {
            StructuredLogger.error(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Failed to schedule offline sync", exception = e)
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
                        val nutritionRepository = NutritionRepository(database)
                        val achievementManager = PersonalAchievementManager(this@MainActivity, repository)
                        val streakManager = PersonalStreakManager(this@MainActivity, repository)
                        
                        // Initialize default achievements and streaks
                        achievementManager.initializeDefaultAchievements()
                        streakManager.initializeDefaultStreaks()
                        
                        // Initialize default food database
                        nutritionRepository.initializeDefaultFoodDatabase()
                        
                        isInitialized = true
                        StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Personal motivation data initialized successfully")
                        
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
                StructuredLogger.warning(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Timeout initializing personal motivation data", exception = e)
                // Retry with simpler initialization
                retrySimpleInitialization()
            } catch (e: Exception) {
                StructuredLogger.error(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Failed to initialize personal motivation data", exception = e)
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
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Simple initialization completed")
        } catch (e: Exception) {
            StructuredLogger.error(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Even simple initialization failed", exception = e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            // Clean up network monitoring
            networkMonitor?.stopMonitoring()
            StructuredLogger.info(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Network monitoring stopped")
        } catch (e: Exception) {
            StructuredLogger.error(StructuredLogger.LogCategory.SYSTEM, "MainActivity", "Error stopping network monitoring", exception = e)
        }
    }
}
