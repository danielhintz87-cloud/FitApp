package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.domain.usecases.HydrationGoalUseCase
import com.example.fitapp.services.SmartNotificationManager
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class WaterReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.get(applicationContext)
            
            val today = LocalDate.now(ZoneId.systemDefault()).toString()
            val repo = NutritionRepository(database)
            val waterIntake = repo.getTotalWaterForDate(today)
            
            // Use unified hydration goal instead of hardcoded value
            val hydrationGoalUseCase = HydrationGoalUseCase.create(applicationContext)
            val targetIntake = hydrationGoalUseCase.getTodaysHydrationGoalMl()
            
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
                // No network requirement for water reminders
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
                2, TimeUnit.HOURS // Every 2 hours
            )
                .setConstraints(constraints)
                .setInitialDelay(30, TimeUnit.MINUTES) // Start after 30 minutes
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
        
        fun cancelWaterReminders(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}