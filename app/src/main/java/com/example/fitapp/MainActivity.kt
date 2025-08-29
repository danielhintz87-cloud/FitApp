package com.example.fitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.services.*
import com.example.fitapp.ui.MainScaffold
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        
        // Initialize default data
        initializePersonalMotivationData()
        
        setContent { MainScaffold() }
    }
    
    private fun initializePersonalMotivationData() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.get(this@MainActivity)
                val repository = PersonalMotivationRepository(database)
                val achievementManager = PersonalAchievementManager(this@MainActivity, repository)
                val streakManager = PersonalStreakManager(this@MainActivity, repository)
                
                // Initialize default achievements and streaks
                achievementManager.initializeDefaultAchievements()
                streakManager.initializeDefaultStreaks()
            } catch (e: Exception) {
                // Log error but don't crash the app
                android.util.Log.e("MainActivity", "Failed to initialize personal motivation data", e)
            }
        }
    }
}
