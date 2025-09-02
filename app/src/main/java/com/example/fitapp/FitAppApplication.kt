package com.example.fitapp

import android.app.Application
import android.util.Log
import com.example.fitapp.ui.nutrition.EnhancedCookingValidator

class FitAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("FitApp", "Application initialized")
        
        // Validate enhanced cooking features integration
        if (BuildConfig.DEBUG) {
            EnhancedCookingValidator.validateAsync(this)
        }
    }
}