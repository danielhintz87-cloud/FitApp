package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import com.example.fitapp.domain.usecases.HydrationGoalUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

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
            val constraints =
                Constraints.Builder()
                    // No network requirement for water reminders
                    .build()

            val workRequest =
                PeriodicWorkRequestBuilder<WaterReminderWorker>(
                    2,
                    TimeUnit.HOURS, // Every 2 hours
                )
                    .setConstraints(constraints)
                    .setInitialDelay(30, TimeUnit.MINUTES) // Start after 30 minutes
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest,
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
