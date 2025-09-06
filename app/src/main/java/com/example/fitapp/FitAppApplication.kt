package com.example.fitapp

import android.app.Application
import android.util.Log
import com.example.fitapp.ui.nutrition.EnhancedCookingValidator
import com.example.fitapp.services.SmartNotificationManager
import com.example.fitapp.services.DailyMotivationWorker
import com.example.fitapp.services.DigitalCoachWorker
import com.example.fitapp.services.NutritionReminderWorker
import com.example.fitapp.services.WaterReminderWorker

class FitAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("FitApp", "Application initialized")
        
        // Initialize notification channels
        SmartNotificationManager.createNotificationChannels(this)
        
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