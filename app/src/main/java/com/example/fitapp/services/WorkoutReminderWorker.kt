package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class WorkoutReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val workoutName = inputData.getString("workout_name") ?: "Training"
            val scheduledTime = inputData.getString("scheduled_time") ?: "jetzt"

            SmartNotificationManager.showWorkoutReminder(
                applicationContext,
                workoutName,
                scheduledTime,
            )

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "workout_reminder_work"

        fun scheduleWorkoutReminder(
            context: Context,
            workoutName: String,
            scheduledTime: LocalTime,
            daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5), // Mon-Fri by default
        ) {
            val constraints =
                Constraints.Builder()
                    // No network requirement for workout reminders
                    .build()

            val inputData =
                Data.Builder()
                    .putString("workout_name", workoutName)
                    .putString("scheduled_time", scheduledTime.toString())
                    .build()

            val workRequest =
                PeriodicWorkRequestBuilder<WorkoutReminderWorker>(
                    1,
                    TimeUnit.DAYS,
                )
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .setInitialDelay(calculateInitialDelay(scheduledTime), TimeUnit.MINUTES)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "$WORK_NAME-$workoutName",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest,
            )
        }

        fun cancelWorkoutReminder(
            context: Context,
            workoutName: String,
        ) {
            WorkManager.getInstance(context).cancelUniqueWork("$WORK_NAME-$workoutName")
        }

        private fun calculateInitialDelay(targetTime: LocalTime): Long {
            val now = LocalTime.now()
            val minutesUntilTarget =
                if (now.isBefore(targetTime)) {
                    java.time.Duration.between(now, targetTime).toMinutes()
                } else {
                    // Next day at target time
                    java.time.Duration.between(now, targetTime.plusHours(24)).toMinutes()
                }

            return minutesUntilTarget
        }
    }
}
