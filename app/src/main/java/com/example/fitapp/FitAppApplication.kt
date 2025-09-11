package com.example.fitapp

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.example.fitapp.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class FitAppApplication : Application() {
    
    // Application-level coroutine scope with SupervisorJob for safe background operations
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Enable StrictMode for debug builds to catch NetworkOnMainThreadException
        if (BuildConfig.DEBUG) {
            enableStrictModeForDebugging()
        }
        
        // Basic logging - safe to run at startup
        Log.d("FitApp", "Application initialized")
        
        // Initialize notification channels immediately as they're needed for the app to function
        try {
            initializeNotificationChannels()
        } catch (e: Exception) {
            Log.e("FitApp", "Failed to initialize notification channels", e)
        }
        
        // Schedule background initialization for complex operations
        // This prevents blocking the main thread and causing startup crashes
        scheduleBackgroundInitialization()
    }
    
    private fun initializeNotificationChannels() {
        // Import here to avoid class loading issues at startup
        val notificationManager = try {
            Class.forName("com.example.fitapp.services.SmartNotificationManager")
                .getDeclaredMethod("createNotificationChannels", Application::class.java)
            // If class exists, call the method reflectively to avoid hard dependency
            val method = Class.forName("com.example.fitapp.services.SmartNotificationManager")
                .getDeclaredMethod("createNotificationChannels", Application::class.java)
            method.invoke(null, this)
        } catch (e: Exception) {
            Log.w("FitApp", "SmartNotificationManager not available during startup", e)
        }
    }
    
    private fun scheduleBackgroundInitialization() {
        // Schedule complex initialization in background to avoid startup crashes
        // This includes WorkManager scheduling, DataStore migration, etc.
        applicationScope.launch {
            try {
                initializeBackgroundServices()
            } catch (e: Exception) {
                Log.e("FitApp", "Background initialization failed", e)
            }
        }
    }
    
    private suspend fun initializeBackgroundServices() {
        try {
            // Initialize meal logging achievements
            val streakManager = com.example.fitapp.services.StreakManager(this)
            streakManager.initializeMealLoggingAchievements()
            Log.d("FitApp", "Meal logging achievements initialized")
        } catch (e: Exception) {
            Log.e("FitApp", "Failed to initialize achievements", e)
        }
        
        // This method will be implemented when other dependent services are available
        Log.d("FitApp", "Background services initialization completed")
    }
    
    private fun enableStrictModeForDebugging() {
        try {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .penaltyLog()
                    .penaltyFlashScreen() // Visual indicator in debug
                    .build()
            )
            
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects() // Detect resource leaks early
                    .penaltyLog()
                    .build()
            )
            
            Log.d("FitApp", "StrictMode enabled for debug build with leak detection")
        } catch (e: Exception) {
            Log.w("FitApp", "Failed to enable StrictMode", e)
        }
    }
}