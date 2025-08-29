package com.example.fitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.services.*
import com.example.fitapp.ui.MainScaffold
import com.example.fitapp.ui.theme.FitAppTheme
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import com.example.fitapp.util.PerformanceMonitor
import kotlinx.coroutines.withTimeout
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private var isInitialized = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Set content immediately for faster UI loading
            setContent { MainScaffold() }
            
            // Initialize critical services in background
            initializeCriticalServices()
            
            // Initialize remaining data asynchronously
            initializeDataAsync()
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Critical error in onCreate", e)
            // Fallback UI in case of critical errors
            setContent { 
                FitAppTheme {
                    Surface {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
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
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e("MainActivity", "Failed to create notification channels", e)
        }
        
        try {
            // Schedule daily work with error handling
            DailyMotivationWorker.scheduleWork(this)
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e("MainActivity", "Failed to schedule daily work", e)
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
                        val achievementManager = PersonalAchievementManager(this@MainActivity, repository)
                        val streakManager = PersonalStreakManager(this@MainActivity, repository)
                        
                        // Initialize default achievements and streaks
                        achievementManager.initializeDefaultAchievements()
                        streakManager.initializeDefaultStreaks()
                        
                        isInitialized = true
                        android.util.Log.i("MainActivity", "Personal motivation data initialized successfully")
                        
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
                android.util.Log.w("MainActivity", "Timeout initializing personal motivation data", e)
                // Retry with simpler initialization
                retrySimpleInitialization()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to initialize personal motivation data", e)
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
            android.util.Log.i("MainActivity", "Simple initialization completed")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Even simple initialization failed", e)
        }
    }
}
