package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Worker for sending proactive digital coach notifications
 */
class DigitalCoachWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    private val digitalCoach = DigitalCoachManager(applicationContext)

    companion object {
        private const val TAG = "DigitalCoachWorker"
        private const val WORK_NAME = "digital_coach_notifications"

        /**
         * Schedule periodic digital coach notifications
         */
        fun schedule(context: Context) {
            val constraints =
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workRequest =
                PeriodicWorkRequestBuilder<DigitalCoachWorker>(
                    6,
                    TimeUnit.HOURS, // Check every 6 hours for coaching opportunities
                )
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS,
                    )
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest,
                )

            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Digital coach notifications scheduled",
            )
        }

        /**
         * Schedule one-time coaching notification with delay
         */
        fun scheduleOneTime(
            context: Context,
            delayMinutes: Long,
            contextType: CoachingContext,
        ) {
            val constraints =
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workRequest =
                OneTimeWorkRequestBuilder<DigitalCoachWorker>()
                    .setConstraints(constraints)
                    .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                    .setInputData(
                        Data.Builder()
                            .putString("coaching_context", contextType.name)
                            .build(),
                    )
                    .build()

            WorkManager.getInstance(context).enqueue(workRequest)

            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "One-time digital coach notification scheduled for $contextType in $delayMinutes minutes",
            )
        }

        /**
         * Cancel all digital coach notifications
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYSTEM,
                    TAG,
                    "Digital coach worker started",
                )

                // Check if it's an appropriate time to send notifications (not too late/early)
                val currentHour = LocalTime.now().hour
                if (currentHour < 7 || currentHour > 22) {
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYSTEM,
                        TAG,
                        "Skipping coaching notification - inappropriate time ($currentHour:00)",
                    )
                    return@withContext Result.success()
                }

                // Get the coaching context from input data, or determine automatically
                val contextType =
                    inputData.getString("coaching_context")?.let {
                        try {
                            CoachingContext.valueOf(it)
                        } catch (e: Exception) {
                            null
                        }
                    } ?: determineOptimalCoachingContext()

                // Generate and send coaching notification
                val coachingMessage =
                    digitalCoach.generateContextualCoachingMessage(
                        context = contextType,
                    )

                SmartNotificationManager.showDigitalCoachNotification(
                    applicationContext,
                    coachingMessage,
                )

                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYSTEM,
                    TAG,
                    "Digital coach notification sent successfully: ${coachingMessage.type}",
                )

                Result.success()
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYSTEM,
                    TAG,
                    "Failed to send digital coach notification",
                    exception = e,
                )
                Result.retry()
            }
        }

    /**
     * Determine the most appropriate coaching context based on current conditions
     */
    private suspend fun determineOptimalCoachingContext(): CoachingContext {
        val triggers = digitalCoach.getRecommendedCoachingTriggers()

        return when {
            triggers.contains(CoachingTrigger.WORKOUT_REMINDER) -> CoachingContext.WORKOUT_REMINDER
            triggers.contains(CoachingTrigger.GOAL_PROGRESS) -> CoachingContext.GOAL_PROGRESS
            triggers.contains(CoachingTrigger.STREAK_MOTIVATION) -> CoachingContext.STREAK_MOTIVATION
            triggers.contains(CoachingTrigger.NUTRITION_TIP) -> CoachingContext.NUTRITION_TIP
            triggers.contains(CoachingTrigger.RECOVERY_REMINDER) -> CoachingContext.RECOVERY_REMINDER
            else -> {
                // Default to daily check-in if no specific triggers
                val currentHour = LocalTime.now().hour
                when (currentHour) {
                    in 7..10 -> CoachingContext.WORKOUT_REMINDER
                    in 11..14 -> CoachingContext.GOAL_PROGRESS
                    in 15..18 -> CoachingContext.NUTRITION_TIP
                    in 19..21 -> CoachingContext.DAILY_CHECK_IN
                    else -> CoachingContext.DAILY_CHECK_IN
                }
            }
        }
    }
}

/**
 * Utility object to trigger coaching notifications based on user actions
 */
object DigitalCoachTriggers {
    /**
     * Trigger coaching notification after workout completion
     */
    fun onWorkoutCompleted(context: Context) {
        DigitalCoachWorker.scheduleOneTime(
            context = context,
            delayMinutes = 5, // 5 minutes after workout
            contextType = CoachingContext.POST_WORKOUT,
        )
    }

    /**
     * Trigger coaching notification for workout reminder
     */
    fun onWorkoutReminder(
        context: Context,
        delayMinutes: Long = 60,
    ) {
        DigitalCoachWorker.scheduleOneTime(
            context = context,
            delayMinutes = delayMinutes,
            contextType = CoachingContext.WORKOUT_REMINDER,
        )
    }

    /**
     * Trigger coaching notification for goal progress
     */
    fun onGoalProgress(context: Context) {
        DigitalCoachWorker.scheduleOneTime(
            context = context,
            delayMinutes = 0, // Immediate
            contextType = CoachingContext.GOAL_PROGRESS,
        )
    }

    /**
     * Trigger coaching notification for streak motivation
     */
    fun onStreakMilestone(context: Context) {
        DigitalCoachWorker.scheduleOneTime(
            context = context,
            delayMinutes = 0, // Immediate
            contextType = CoachingContext.STREAK_MOTIVATION,
        )
    }

    /**
     * Trigger coaching notification for nutrition tip
     */
    fun onNutritionLogged(context: Context) {
        DigitalCoachWorker.scheduleOneTime(
            context = context,
            delayMinutes = 30, // 30 minutes after logging nutrition
            contextType = CoachingContext.NUTRITION_TIP,
        )
    }
}
