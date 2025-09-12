package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.domain.usecases.HydrationGoalUseCase
import com.example.fitapp.services.SmartNotificationManager
import com.example.fitapp.util.time.TimeZoneUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WaterReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val hydrationGoalUseCase: HydrationGoalUseCase
) : CoroutineWorker(context, workerParams) {
    
    @dagger.assisted.AssistedFactory
    interface Factory {
        fun create(context: Context, workerParams: WorkerParameters): WaterReminderWorker
    }
    
    override suspend fun doWork(): Result {
        return try {
            // Get current hydration status using the reactive use case
            val status = hydrationGoalUseCase.hydrationStatus.first()
            
            SmartNotificationManager.showWaterReminder(
                applicationContext,
                status.consumedMl,
                status.goalMl
            )
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WaterReminderWorker", "Failed to show water reminder", e)
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
        
        /**
         * Reschedules water reminders when hydration goals change.
         * This ensures notifications are sent with updated goal information.
         */
        fun rescheduleOnGoalChange(context: Context) {
            cancelWaterReminders(context)
            scheduleWaterReminders(context)
        }
    }
}