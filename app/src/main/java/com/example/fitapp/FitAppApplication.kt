package com.example.fitapp

import android.app.Application
import android.util.Log
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.ui.nutrition.EnhancedCookingValidator
import com.example.fitapp.services.SmartNotificationManager
import com.example.fitapp.services.DailyMotivationWorker
import com.example.fitapp.services.DigitalCoachWorker
import com.example.fitapp.services.NutritionReminderWorker
import com.example.fitapp.services.WaterReminderWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FitAppApplication : Application() {
    
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    
    // Application-level coroutine scope with SupervisorJob for safe background operations
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        Log.d("FitApp", "Application initialized")
        
        // Initialize notification channels
        SmartNotificationManager.createNotificationChannels(this)
        
        // Migrate SharedPreferences to DataStore
        applicationScope.launch {
            try {
                val migrated = userPreferencesRepository.migrateFromSharedPreferences()
                if (migrated) {
                    Log.i("FitApp", "Successfully migrated user preferences to DataStore")
                } else {
                    Log.d("FitApp", "User preferences already migrated to DataStore")
                }
            } catch (e: Exception) {
                Log.e("FitApp", "Error during DataStore migration", e)
            }
        }
        
        // Schedule default notification workers
        DailyMotivationWorker.scheduleWork(this)
        DigitalCoachWorker.schedule(this)
        NutritionReminderWorker.scheduleMealReminders(this)
        WaterReminderWorker.scheduleWaterReminders(this)
        
        // Validate enhanced cooking features integration
        if (BuildConfig.DEBUG) {
            EnhancedCookingValidator.validateAsync(this)
        }
    }
}