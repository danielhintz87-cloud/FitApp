package com.example.fitapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class FitAppApplication : Application() {
    
    // Application-level coroutine scope with SupervisorJob for safe background operations
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        // Keep Application onCreate minimal to avoid startup crashes
        // Complex initialization should be done lazily when needed
    }
}