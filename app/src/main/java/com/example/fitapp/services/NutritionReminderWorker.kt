package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class NutritionReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val mealType = inputData.getString("meal_type") ?: "Mahlzeit"
            val mealTime = inputData.getString("meal_time") ?: "jetzt"

            SmartNotificationManager.showMealReminder(
                applicationContext,
                mealType,
                mealTime,
            )

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "nutrition_reminder_work"

        fun scheduleMealReminders(context: Context) {
            val mealTimes =
                mapOf(
                    "Frühstück" to LocalTime.of(8, 0),
                    "Mittagessen" to LocalTime.of(12, 30),
                    "Abendessen" to LocalTime.of(18, 30),
                )

            mealTimes.forEach { (mealType, time) ->
                scheduleMealReminder(context, mealType, time)
            }
        }

        private fun scheduleMealReminder(
            context: Context,
            mealType: String,
            scheduledTime: LocalTime,
        ) {
            val constraints =
                Constraints.Builder()
                    // No network requirement for meal reminders
                    .build()

            val inputData =
                Data.Builder()
                    .putString("meal_type", mealType)
                    .putString("meal_time", scheduledTime.toString())
                    .build()

            val workRequest =
                PeriodicWorkRequestBuilder<NutritionReminderWorker>(
                    1,
                    TimeUnit.DAYS,
                )
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .setInitialDelay(calculateInitialDelay(scheduledTime), TimeUnit.MINUTES)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "$WORK_NAME-$mealType",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest,
            )
        }

        fun cancelMealReminders(context: Context) {
            val mealTypes = listOf("Frühstück", "Mittagessen", "Abendessen")
            mealTypes.forEach { mealType ->
                WorkManager.getInstance(context).cancelUniqueWork("$WORK_NAME-$mealType")
            }
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
