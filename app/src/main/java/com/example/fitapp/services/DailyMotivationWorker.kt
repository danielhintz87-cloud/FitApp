package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.PersonalMotivationRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class DailyMotivationWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.get(applicationContext)
            val repository = PersonalMotivationRepository(database)
            val achievementManager = PersonalAchievementManager(applicationContext, repository)
            val streakManager = PersonalStreakManager(applicationContext, repository)

            // Initialize default data if needed
            achievementManager.initializeDefaultAchievements()
            streakManager.initializeDefaultStreaks()

            // Update daily streaks
            streakManager.updateDailyStreaks()

            // Check for streak warnings
            streakManager.checkStreakWarnings()

            // Check and unlock achievements
            achievementManager.checkAndUnlockAchievements()

            // Show daily motivation (only on weekdays, not too early/late)
            val currentHour = java.time.LocalTime.now().hour
            if (currentHour in 8..20 && java.time.LocalDate.now().dayOfWeek.value <= 5) {
                showDailyMotivation(repository)
            }

            Result.success()
        } catch (exception: Exception) {
            Result.retry()
        }
    }

    private suspend fun showDailyMotivation(repository: PersonalMotivationRepository) {
        val activeStreaks = repository.activeStreaksFlow().first()
        val completedAchievements = repository.achievementsByCompletionFlow(true).first()
        val longestStreak = activeStreaks.maxOfOrNull { it.currentStreak } ?: 0

        val stats =
            SmartNotificationManager.PersonalStats(
                activeStreaks = activeStreaks.size,
                completedAchievements = completedAchievements.size,
                longestStreak = longestStreak,
                totalWorkouts = 0, // Could be calculated if needed
            )

        SmartNotificationManager.showDailyMotivation(applicationContext, stats)
    }

    companion object {
        private const val WORK_NAME = "daily_motivation_work"

        fun scheduleWork(context: Context) {
            val constraints =
                Constraints.Builder()
                    // No network requirement needed for daily motivation
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workRequest =
                PeriodicWorkRequestBuilder<DailyMotivationWorker>(
                    1,
                    TimeUnit.DAYS,
                )
                    .setConstraints(constraints)
                    .setInitialDelay(calculateInitialDelay(), TimeUnit.MINUTES)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest,
            )
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        private fun calculateInitialDelay(): Long {
            val now = java.time.LocalTime.now()
            val target = java.time.LocalTime.of(9, 0) // 9 AM

            val minutesUntilTarget =
                if (now.isBefore(target)) {
                    java.time.Duration.between(now, target).toMinutes()
                } else {
                    // Next day at 9 AM
                    java.time.Duration.between(now, target.plusHours(24)).toMinutes()
                }

            return minutesUntilTarget
        }
    }
}
