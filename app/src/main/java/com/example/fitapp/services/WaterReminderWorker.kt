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
            val repo = NutritionRepository(database, applicationContext)
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
        
        /**
         * Schedules water reminders to run periodically.
         * 
         * This method integrates with HydrationGoalUseCase to ensure reminders
         * are based on the latest unified hydration goals, providing a consistent
         * user experience across all water tracking features.
         * 
         * @param context Application context for WorkManager
         */
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
        
        /**
         * Reschedules water reminders when hydration goals change.
         * 
         * This ensures that the reminder system stays synchronized with
         * the user's current hydration preferences and daily goals.
         * Call this method whenever hydration goals are updated.
         * 
         * @param context Application context for WorkManager
         */
        fun rescheduleOnGoalChange(context: Context) {
            // Cancel existing work and reschedule with updated goals
            // This ensures the next reminder uses the latest goal
            cancelWaterReminders(context)
            scheduleWaterReminders(context)
        }
        
        fun cancelWaterReminders(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}