package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.ai.AppAi
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

/**
 * Digital Coach Manager for proactive AI coaching
 * Provides contextual coaching messages, tips, and motivation based on user activity
 */
class DigitalCoachManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.get(context)
) {
    private val motivationRepo = PersonalMotivationRepository(database)
    private val nutritionRepo = NutritionRepository(database)
    
    companion object {
        private const val TAG = "DigitalCoachManager"
    }
    
    /**
     * Generate contextual coaching message based on current user state
     */
    suspend fun generateContextualCoachingMessage(
        userId: String = "default",
        context: CoachingContext = CoachingContext.DAILY_CHECK_IN
    ): CoachingMessage = withContext(Dispatchers.IO) {
        try {
            val userStats = getUserStats()
            val recentActivity = getRecentActivity()
            
            val message = when (context) {
                CoachingContext.DAILY_CHECK_IN -> generateDailyCheckInMessage(userStats, recentActivity)
                CoachingContext.POST_WORKOUT -> generatePostWorkoutMessage(userStats, recentActivity)
                CoachingContext.WORKOUT_REMINDER -> generateWorkoutReminderMessage(userStats, recentActivity)
                CoachingContext.GOAL_PROGRESS -> generateGoalProgressMessage(userStats, recentActivity)
                CoachingContext.STREAK_MOTIVATION -> generateStreakMotivationMessage(userStats, recentActivity)
                CoachingContext.NUTRITION_TIP -> generateNutritionTipMessage(userStats, recentActivity)
                CoachingContext.RECOVERY_REMINDER -> generateRecoveryReminderMessage(userStats, recentActivity)
            }
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.AI,
                TAG,
                "Generated coaching message for context: $context"
            )
            
            message
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.AI,
                TAG,
                "Failed to generate coaching message",
                exception = e
            )
            CoachingMessage.default()
        }
    }
    
    /**
     * Determine optimal coaching triggers based on user patterns
     */
    suspend fun getRecommendedCoachingTriggers(): List<CoachingTrigger> = withContext(Dispatchers.IO) {
        try {
            val triggers = mutableListOf<CoachingTrigger>()
            val userStats = getUserStats()
            val recentActivity = getRecentActivity()
            
            // Workout-related triggers
            if (shouldTriggerWorkoutReminder(recentActivity)) {
                triggers.add(CoachingTrigger.WORKOUT_REMINDER)
            }
            
            // Goal progress triggers
            if (shouldTriggerGoalProgress(userStats)) {
                triggers.add(CoachingTrigger.GOAL_PROGRESS)
            }
            
            // Streak motivation triggers
            if (shouldTriggerStreakMotivation(userStats)) {
                triggers.add(CoachingTrigger.STREAK_MOTIVATION)
            }
            
            // Nutrition triggers
            if (shouldTriggerNutritionTip(recentActivity)) {
                triggers.add(CoachingTrigger.NUTRITION_TIP)
            }
            
            // Recovery triggers
            if (shouldTriggerRecoveryReminder(recentActivity)) {
                triggers.add(CoachingTrigger.RECOVERY_REMINDER)
            }
            
            triggers
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.AI,
                TAG,
                "Failed to get coaching triggers",
                exception = e
            )
            emptyList()
        }
    }
    
    /**
     * Process coaching feedback to improve future recommendations
     */
    suspend fun processCoachingFeedback(
        messageId: String,
        feedback: CoachingFeedback
    ): Unit = withContext(Dispatchers.IO) {
        try {
            // Store feedback for future ML improvements
            StructuredLogger.info(
                StructuredLogger.LogCategory.AI,
                TAG,
                "Received coaching feedback: $feedback for message: $messageId"
            )
            
            // For now, log the feedback. In future, this could be used to improve AI models
            when (feedback) {
                CoachingFeedback.HELPFUL -> {
                    // User found the message helpful - similar messages should be prioritized
                }
                CoachingFeedback.NOT_HELPFUL -> {
                    // User found the message not helpful - avoid similar messages
                }
                CoachingFeedback.MORE_OF_THIS -> {
                    // User wants more of this type of content
                }
                CoachingFeedback.LESS_OF_THIS -> {
                    // User wants less of this type of content
                }
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.AI,
                TAG,
                "Failed to process coaching feedback",
                exception = e
            )
        }
    }
    
    // Private helper methods
    
    private suspend fun getUserStats(): UserStats {
        val activeStreaks = motivationRepo.activeStreaksFlow().first()
        val achievements = motivationRepo.achievementsByCompletionFlow(true).first()
        val today = LocalDate.now()
        val todayEpoch = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val entries = nutritionRepo.dayEntriesFlow(todayEpoch).first()
        val goal = nutritionRepo.goalFlow(today).first()
        
        return UserStats(
            activeStreaks = activeStreaks.size,
            longestStreak = activeStreaks.maxOfOrNull { it.currentStreak } ?: 0,
            completedAchievements = achievements.size,
            todayCalories = entries.sumOf { it.kcal },
            calorieGoal = goal?.targetKcal ?: 2000,
            hasCompletedWorkoutToday = checkWorkoutCompletedToday(),
            hasLoggedNutritionToday = entries.isNotEmpty()
        )
    }
    
    private suspend fun getRecentActivity(): RecentActivity {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val dayBeforeYesterday = today.minusDays(2)
        
        return RecentActivity(
            workoutDaysInWeek = countWorkoutDaysInWeek(),
            nutritionTrackingDays = countNutritionTrackingDays(),
            daysSinceLastWorkout = daysSinceLastWorkout(),
            streakEndangered = checkIfStreakEndangered()
        )
    }
    
    private suspend fun generateDailyCheckInMessage(stats: UserStats, activity: RecentActivity): CoachingMessage {
        val messages = mutableListOf<String>()
        
        when {
            stats.activeStreaks > 0 && stats.longestStreak >= 7 -> {
                messages.add("Fantastisch! Deine ${stats.longestStreak}-Tage Streak zeigt echte Hingabe! 🔥")
                messages.add("Du hast bereits ${stats.completedAchievements} Erfolge freigeschaltet! Weiter so! 🏆")
            }
            stats.hasCompletedWorkoutToday -> {
                messages.add("Großartig! Du hast heute bereits trainiert! 💪")
                if (!stats.hasLoggedNutritionToday) {
                    messages.add("Vergiss nicht, deine Mahlzeiten zu tracken, um das Maximum aus deinem Training herauszuholen! 🍎")
                }
            }
            activity.daysSinceLastWorkout >= 3 -> {
                messages.add("Hey! Du warst ${activity.daysSinceLastWorkout} Tage nicht aktiv. Zeit für ein Comeback! 🎯")
                messages.add("Schon 10 Minuten Bewegung können den Unterschied machen! 💚")
            }
            else -> {
                val motivational = listOf(
                    "Heute ist ein perfekter Tag für deine Fitness! 🌟",
                    "Kleine Schritte führen zu großen Erfolgen! 👣",
                    "Du bist stärker als deine Ausreden! 🔥",
                    "Jeder Tag ist eine neue Chance, besser zu werden! ☀️"
                )
                messages.add(motivational.random())
            }
        }
        
        return CoachingMessage(
            id = generateMessageId(),
            title = "Dein täglicher Coach",
            content = messages.joinToString(" "),
            type = CoachingMessageType.DAILY_MOTIVATION,
            priority = CoachingPriority.MEDIUM,
            actionButtons = listOf("Los geht's!", "Später")
        )
    }
    
    private suspend fun generatePostWorkoutMessage(stats: UserStats, activity: RecentActivity): CoachingMessage {
        val congratsMessages = listOf(
            "Ausgezeichnet! Du hast heute dein Training abgeschlossen! 💪",
            "Fantastisch! Ein weiterer Schritt näher zu deinen Zielen! 🎯",
            "Toll gemacht! Du hast heute wieder bewiesen, was in dir steckt! ⭐"
        )
        
        val tips = listOf(
            "Vergiss nicht, ausreichend Protein zu dir zu nehmen für die Regeneration! 🥗",
            "Hydration ist jetzt wichtig - trinke genug Wasser! 💧",
            "Nimm dir Zeit für Stretching und Regeneration! 🧘‍♀️"
        )
        
        return CoachingMessage(
            id = generateMessageId(),
            title = "Training abgeschlossen!",
            content = "${congratsMessages.random()} ${tips.random()}",
            type = CoachingMessageType.POST_WORKOUT,
            priority = CoachingPriority.HIGH,
            actionButtons = listOf("Nutrition loggen", "Regeneration")
        )
    }
    
    private suspend fun generateWorkoutReminderMessage(stats: UserStats, activity: RecentActivity): CoachingMessage {
        val timeBasedMessages = when (java.time.LocalTime.now().hour) {
            in 6..10 -> "Guten Morgen! Zeit für dein Morning Workout! 🌅"
            in 11..14 -> "Mittagspause perfekt für ein schnelles Training! ⏰"
            in 15..18 -> "Nachmittagsenergie nutzen - ab zum Training! 🏃‍♀️"
            in 19..21 -> "Feierabend-Training für einen perfekten Tagesabschluss! 🌆"
            else -> "Zeit für dein Training! 💪"
        }
        
        return CoachingMessage(
            id = generateMessageId(),
            title = "Workout-Zeit!",
            content = timeBasedMessages,
            type = CoachingMessageType.WORKOUT_REMINDER,
            priority = CoachingPriority.HIGH,
            actionButtons = listOf("Jetzt trainieren", "In 30 Min erinnern")
        )
    }
    
    private suspend fun generateGoalProgressMessage(stats: UserStats, activity: RecentActivity): CoachingMessage {
        val progressPercent = if (stats.calorieGoal > 0) {
            (stats.todayCalories.toFloat() / stats.calorieGoal * 100).toInt()
        } else 0
        
        val message = when {
            progressPercent >= 90 -> "Perfekt! Du hast fast dein Kalorienziel erreicht! (${progressPercent}%) 🎯"
            progressPercent >= 70 -> "Super! Du bist auf einem guten Weg! (${progressPercent}%) 📈"
            progressPercent >= 50 -> "Du machst Fortschritte! Noch ${100 - progressPercent}% bis zum Ziel! 💪"
            else -> "Du hast noch Potenzial für heute! Nur ${100 - progressPercent}% bis zum Ziel! 🚀"
        }
        
        return CoachingMessage(
            id = generateMessageId(),
            title = "Dein Fortschritt heute",
            content = message,
            type = CoachingMessageType.GOAL_PROGRESS,
            priority = CoachingPriority.MEDIUM,
            actionButtons = listOf("Details anzeigen", "Mahlzeit hinzufügen")
        )
    }
    
    private suspend fun generateStreakMotivationMessage(stats: UserStats, activity: RecentActivity): CoachingMessage {
        return when {
            stats.longestStreak >= 30 -> CoachingMessage(
                id = generateMessageId(),
                title = "Streak Legend!",
                content = "Unglaublich! ${stats.longestStreak} Tage Streak! Du bist eine wahre Inspiration! 👑",
                type = CoachingMessageType.STREAK_MOTIVATION,
                priority = CoachingPriority.HIGH,
                actionButtons = listOf("Teilen", "Weiter so!")
            )
            stats.longestStreak >= 7 -> CoachingMessage(
                id = generateMessageId(),
                title = "Streak-Held!",
                content = "Fantastisch! ${stats.longestStreak} Tage am Stück! Du beweist echte Disziplin! 🔥",
                type = CoachingMessageType.STREAK_MOTIVATION,
                priority = CoachingPriority.HIGH,
                actionButtons = listOf("Stolz sein", "Weiter machen!")
            )
            else -> CoachingMessage(
                id = generateMessageId(),
                title = "Streak starten!",
                content = "Heute ist der perfekte Tag, um eine neue Streak zu beginnen! 🎯",
                type = CoachingMessageType.STREAK_MOTIVATION,
                priority = CoachingPriority.MEDIUM,
                actionButtons = listOf("Challenge annehmen", "Später")
            )
        }
    }
    
    private suspend fun generateNutritionTipMessage(stats: UserStats, activity: RecentActivity): CoachingMessage {
        val tips = listOf(
            "Probiere Pause zwischen den Mahlzeiten für bessere Regeneration! ⏰",
            "Mehr Gemüse in jeder Mahlzeit für optimale Nährstoffversorgung! 🥬",
            "Ausreichend Wasser trinken unterstützt deinen Stoffwechsel! 💧",
            "Protein in jeder Mahlzeit hilft beim Muskelaufbau! 🥚"
        )
        
        return CoachingMessage(
            id = generateMessageId(),
            title = "Ernährungs-Tipp",
            content = tips.random(),
            type = CoachingMessageType.NUTRITION_TIP,
            priority = CoachingPriority.LOW,
            actionButtons = listOf("Merken", "Mehr Tipps")
        )
    }
    
    private suspend fun generateRecoveryReminderMessage(stats: UserStats, activity: RecentActivity): CoachingMessage {
        val recoveryTips = listOf(
            "Gönn dir heute eine Pause - Regeneration ist genauso wichtig wie Training! 😴",
            "Stretching und Mobility-Work fördern deine Regeneration! 🧘‍♀️",
            "Ausreichend Schlaf ist der Schlüssel zu besserem Training! 🌙"
        )
        
        return CoachingMessage(
            id = generateMessageId(),
            title = "Regeneration",
            content = recoveryTips.random(),
            type = CoachingMessageType.RECOVERY_REMINDER,
            priority = CoachingPriority.MEDIUM,
            actionButtons = listOf("Pause machen", "Morgen trainieren")
        )
    }
    
    // Helper methods for trigger logic
    private suspend fun shouldTriggerWorkoutReminder(activity: RecentActivity): Boolean {
        return activity.daysSinceLastWorkout >= 1
    }
    
    private suspend fun shouldTriggerGoalProgress(stats: UserStats): Boolean {
        return stats.calorieGoal > 0 && stats.todayCalories > 0
    }
    
    private suspend fun shouldTriggerStreakMotivation(stats: UserStats): Boolean {
        return stats.activeStreaks > 0 || stats.longestStreak >= 7
    }
    
    private suspend fun shouldTriggerNutritionTip(activity: RecentActivity): Boolean {
        return activity.nutritionTrackingDays >= 3
    }
    
    private suspend fun shouldTriggerRecoveryReminder(activity: RecentActivity): Boolean {
        return activity.workoutDaysInWeek >= 5
    }
    
    // Placeholder implementations for user activity tracking
    private suspend fun checkWorkoutCompletedToday(): Boolean = false
    private suspend fun countWorkoutDaysInWeek(): Int = Random.nextInt(0, 7)
    private suspend fun countNutritionTrackingDays(): Int = Random.nextInt(0, 7)
    private suspend fun daysSinceLastWorkout(): Int = Random.nextInt(0, 5)
    private suspend fun checkIfStreakEndangered(): Boolean = Random.nextBoolean()
    
    private fun generateMessageId(): String = "msg_${System.currentTimeMillis()}_${Random.nextInt(1000)}"
}

// Data classes for Digital Coach

data class CoachingMessage(
    val id: String,
    val title: String,
    val content: String,
    val type: CoachingMessageType,
    val priority: CoachingPriority,
    val actionButtons: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun default() = CoachingMessage(
            id = "default",
            title = "Bleib dran!",
            content = "Du machst das großartig! Jeden Tag ein Schritt näher zu deinem Ziel! 💪",
            type = CoachingMessageType.DAILY_MOTIVATION,
            priority = CoachingPriority.LOW
        )
    }
}

data class UserStats(
    val activeStreaks: Int,
    val longestStreak: Int,
    val completedAchievements: Int,
    val todayCalories: Int,
    val calorieGoal: Int,
    val hasCompletedWorkoutToday: Boolean,
    val hasLoggedNutritionToday: Boolean
)

data class RecentActivity(
    val workoutDaysInWeek: Int,
    val nutritionTrackingDays: Int,
    val daysSinceLastWorkout: Int,
    val streakEndangered: Boolean
)

enum class CoachingContext {
    DAILY_CHECK_IN,
    POST_WORKOUT,
    WORKOUT_REMINDER,
    GOAL_PROGRESS,
    STREAK_MOTIVATION,
    NUTRITION_TIP,
    RECOVERY_REMINDER
}

enum class CoachingMessageType {
    DAILY_MOTIVATION,
    POST_WORKOUT,
    WORKOUT_REMINDER,
    GOAL_PROGRESS,
    STREAK_MOTIVATION,
    NUTRITION_TIP,
    RECOVERY_REMINDER
}

enum class CoachingPriority {
    LOW,
    MEDIUM,
    HIGH
}

enum class CoachingTrigger {
    WORKOUT_REMINDER,
    GOAL_PROGRESS,
    STREAK_MOTIVATION,
    NUTRITION_TIP,
    RECOVERY_REMINDER
}

enum class CoachingFeedback {
    HELPFUL,
    NOT_HELPFUL,
    MORE_OF_THIS,
    LESS_OF_THIS
}