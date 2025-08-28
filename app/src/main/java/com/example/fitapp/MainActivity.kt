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
        
        // Initialize notification channels
        SmartNotificationManager.createNotificationChannels(this)
        
        // Schedule daily work
        DailyMotivationWorker.scheduleWork(this)
        
        // Initialize default data
        initializePersonalMotivationData()
        
        setContent { MainScaffold() }
    }
    
    private fun initializePersonalMotivationData() {
        lifecycleScope.launch {
            val database = AppDatabase.get(this@MainActivity)
            val repository = PersonalMotivationRepository(database)
            val achievementManager = PersonalAchievementManager(this@MainActivity, repository)
            val streakManager = PersonalStreakManager(this@MainActivity, repository)
            
            // Initialize default achievements and streaks
            achievementManager.initializeDefaultAchievements()
            streakManager.initializeDefaultStreaks()
        }
    }
}
