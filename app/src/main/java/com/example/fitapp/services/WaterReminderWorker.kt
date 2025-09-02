package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.services.SmartNotificationManager
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class WaterReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.get(applicationContext)
            
            // Get today's water intake - simplified approach
            val today = LocalDate.now()
            // TODO: Implement proper water tracking in nutrition repository
            // For now, use a simple calculation
            val waterIntake = 800 // Placeholder - would need actual tracking
            
            val targetIntake = 2000 // 2L target
            
            SmartNotificationManager.showWaterReminder(
                applicationContext,
                waterIntake,
                targetIntake
            )
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    companion object {
        private const val WORK_NAME = "water_reminder_work"
        
        fun scheduleWaterReminders(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
                2, TimeUnit.HOURS // Every 2 hours
            )
                .setConstraints(constraints)
                .setInitialDelay(30, TimeUnit.MINUTES) // Start after 30 minutes
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
        
        fun cancelWaterReminders(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}