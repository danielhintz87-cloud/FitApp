package com.example.fitapp.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fitapp.MainActivity
import com.example.fitapp.R
import com.example.fitapp.data.db.PersonalAchievementEntity
import com.example.fitapp.data.db.PersonalStreakEntity
import com.example.fitapp.data.db.SocialChallengeEntity

object SmartNotificationManager {
    
    // Notification channels
    private const val CHANNEL_ACHIEVEMENTS = "achievements"
    private const val CHANNEL_STREAKS = "streaks"
    private const val CHANNEL_MILESTONES = "milestones"
    private const val CHANNEL_DAILY_MOTIVATION = "daily_motivation"
    private const val CHANNEL_WORKOUT_REMINDERS = "workout_reminders"
    private const val CHANNEL_NUTRITION_REMINDERS = "nutrition_reminders"
    private const val CHANNEL_WATER_REMINDERS = "water_reminders"
    private const val CHANNEL_MACRO_WARNINGS = "macro_warnings"
    private const val CHANNEL_SOCIAL_CHALLENGES = "social_challenges"
    private const val CHANNEL_CHALLENGE_UPDATES = "challenge_updates"
    
    // Notification IDs
    private const val NOTIFICATION_ID_ACHIEVEMENT = 1000
    private const val NOTIFICATION_ID_STREAK_WARNING = 2000
    private const val NOTIFICATION_ID_STREAK_MILESTONE = 3000
    private const val NOTIFICATION_ID_DAILY_MOTIVATION = 4000
    private const val NOTIFICATION_ID_WORKOUT_REMINDER = 5000
    private const val NOTIFICATION_ID_MEAL_REMINDER = 6000
    private const val NOTIFICATION_ID_WATER_REMINDER = 7000
    private const val NOTIFICATION_ID_MACRO_WARNING = 8000
    private const val NOTIFICATION_ID_CHALLENGE_JOINED = 9000
    private const val NOTIFICATION_ID_CHALLENGE_COMPLETED = 10000
    private const val NOTIFICATION_ID_CHALLENGE_UPDATE = 11000
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Achievement channel
            val achievementChannel = NotificationChannel(
                CHANNEL_ACHIEVEMENTS,
                "Erfolge",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Benachrichtigungen für freigeschaltete Erfolge"
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
            }
            
            // Streak warning channel
            val streakChannel = NotificationChannel(
                CHANNEL_STREAKS,
                "Streak-Warnungen",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Warnungen für gefährdete Streaks"
                setShowBadge(true)
                enableLights(true)
            }
            
            // Milestone channel
            val milestoneChannel = NotificationChannel(
                CHANNEL_MILESTONES,
                "Meilensteine",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Benachrichtigungen für erreichte Meilensteine"
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
            }
            
            // Daily motivation channel
            val motivationChannel = NotificationChannel(
                CHANNEL_DAILY_MOTIVATION,
                "Tägliche Motivation",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tägliche Motivationsnachrichten"
                setShowBadge(false)
            }
            
            // Workout reminders channel
            val workoutChannel = NotificationChannel(
                CHANNEL_WORKOUT_REMINDERS,
                "Workout-Erinnerungen",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Erinnerungen für geplante Workouts"
                setShowBadge(true)
                enableLights(true)
            }
            
            // Nutrition reminders channel
            val nutritionChannel = NotificationChannel(
                CHANNEL_NUTRITION_REMINDERS,
                "Ernährungs-Erinnerungen",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Erinnerungen für Mahlzeiten und Ernährung"
                setShowBadge(true)
            }
            
            // Water reminders channel
            val waterChannel = NotificationChannel(
                CHANNEL_WATER_REMINDERS,
                "Wasser-Erinnerungen",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Erinnerungen für Wasserzufuhr"
                setShowBadge(false)
            }
            
            // Macro warnings channel
            val macroChannel = NotificationChannel(
                CHANNEL_MACRO_WARNINGS,
                "Makro-Warnungen",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Warnungen bei Makro-Zielen"
                setShowBadge(true)
                enableLights(true)
            }
            
            // Social challenge channels
            val socialChallengeChannel = NotificationChannel(
                CHANNEL_SOCIAL_CHALLENGES,
                "Social Challenges",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Benachrichtigungen für Challenge-Teilnahme und Abschluss"
                setShowBadge(true)
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
            }
            
            val challengeUpdateChannel = NotificationChannel(
                CHANNEL_CHALLENGE_UPDATES,
                "Challenge Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates zu Challenge-Fortschritt und Ranglisten"
                setShowBadge(false)
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
            }
            
            notificationManager.createNotificationChannels(listOf(
                achievementChannel,
                streakChannel,
                milestoneChannel,
                motivationChannel,
                workoutChannel,
                nutritionChannel,
                waterChannel,
                macroChannel,
                socialChallengeChannel,
                challengeUpdateChannel
            ))
        }
    }
    
    /**
     * Show achievement unlock notification
     */
    fun showAchievementUnlocked(context: Context, achievement: PersonalAchievementEntity) {
        if (!hasNotificationPermission(context)) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "progress")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            (NOTIFICATION_ID_ACHIEVEMENT + achievement.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system icon
            .setContentTitle("🎉 Erfolg freigeschaltet!")
            .setContentText(achievement.title)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${achievement.title}\n${achievement.description}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Ansehen",
                pendingIntent
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_ACHIEVEMENT + achievement.id.toInt(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    /**
     * Show streak warning notification
     */
    fun showStreakWarning(context: Context, streak: PersonalStreakEntity) {
        if (!hasNotificationPermission(context)) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "today")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            (NOTIFICATION_ID_STREAK_WARNING + streak.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_STREAKS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🔥 Streak in Gefahr!")
            .setContentText("Deine ${streak.name}-Streak (${streak.currentStreak} Tage) ist gefährdet")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Deine ${streak.name}-Streak von ${streak.currentStreak} Tagen ist in Gefahr! Aktiviere die App heute, um sie fortzusetzen."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Jetzt aktivieren",
                pendingIntent
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_STREAK_WARNING + streak.id.toInt(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    /**
     * Show streak milestone notification
     */
    fun showStreakMilestone(context: Context, streak: PersonalStreakEntity, milestone: Int) {
        if (!hasNotificationPermission(context)) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "progress")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            (NOTIFICATION_ID_STREAK_MILESTONE + streak.id + milestone).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val milestoneText = when (milestone) {
            7 -> "Eine Woche geschafft! 🎯"
            14 -> "Zwei Wochen stark! 💪"
            30 -> "Ein Monat Durchhaltevermögen! 🏆"
            50 -> "50 Tage - Du bist unaufhaltbar! ⭐"
            100 -> "100 Tage - Legendary Status! 👑"
            else -> "$milestone Tage erreicht! 🔥"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_MILESTONES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔥 Streak-Meilenstein!")
            .setContentText("${streak.name}: $milestoneText")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${streak.name}\n$milestoneText\n\n${streak.description}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Feiern!",
                pendingIntent
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_STREAK_MILESTONE + streak.id.toInt(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    /**
     * Show daily motivation notification
     */
    fun showDailyMotivation(context: Context, personalStats: PersonalStats) {
        if (!hasNotificationPermission(context)) return
        
        val motivationMessages = listOf(
            "Zeit für dein Training! Du schaffst das! 💪",
            "Heute ist ein perfekter Tag für Fitness! 🌟",
            "Deine Ziele warten auf dich! 🎯",
            "Kleine Schritte führen zu großen Veränderungen! 👣",
            "Du bist stärker als deine Ausreden! 🔥",
            "Halte dein Training durch - du wirst stärker! 🔥",
            "Bleib am Ball - jeder Tag bringt dich näher ans Ziel! 🎯",
            "Fitness ist eine Reise, nicht ein Ziel! 🛤️",
            "Jeder Tag ist eine neue Chance! ☀️"
        )
        
        val message = motivationMessages.random()
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "today")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_DAILY_MOTIVATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val statsText = buildString {
            if (personalStats.activeStreaks > 0) {
                append("🔥 ${personalStats.activeStreaks} aktive Streaks\n")
            }
            if (personalStats.completedAchievements > 0) {
                append("🏆 ${personalStats.completedAchievements} Erfolge\n")
            }
            if (personalStats.longestStreak > 0) {
                append("📈 Längste Streak: ${personalStats.longestStreak} Tage")
            }
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_MOTIVATION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("FitApp Motivation")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$message\n\n$statsText"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Los geht's!",
                pendingIntent
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_DAILY_MOTIVATION,
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    /**
     * Show workout reminder notification
     */
    fun showWorkoutReminder(context: Context, workoutName: String, scheduledTime: String) {
        if (!hasNotificationPermission(context)) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "today")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_WORKOUT_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_WORKOUT_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Workout-Zeit!")
            .setContentText("Zeit für dein $workoutName um $scheduledTime")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Dein $workoutName ist für $scheduledTime geplant. Bereit, deine Ziele zu erreichen? 💪"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Jetzt trainieren",
                pendingIntent
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_WORKOUT_REMINDER,
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    /**
     * Show meal reminder notification
     */
    fun showMealReminder(context: Context, mealType: String, mealTime: String) {
        if (!hasNotificationPermission(context)) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "nutrition")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_MEAL_REMINDER + mealType.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val mealEmoji = when (mealType.lowercase()) {
            "frühstück" -> "🌅"
            "mittagessen" -> "🍽️"
            "abendessen" -> "🌆"
            "snack" -> "🍎"
            else -> "🍽️"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_NUTRITION_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$mealEmoji Zeit für $mealType!")
            .setContentText("Vergiss nicht, dein $mealType zu loggen")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Zeit für dein $mealType um $mealTime. Vergiss nicht, deine Mahlzeit zu tracken! 📱"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Jetzt loggen",
                pendingIntent
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_MEAL_REMINDER + mealType.hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    /**
     * Show water reminder notification
     */
    fun showWaterReminder(context: Context, currentIntake: Int, targetIntake: Int) {
        if (!hasNotificationPermission(context)) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "nutrition")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_WATER_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val remaining = targetIntake - currentIntake
        val progressText = if (remaining > 0) {
            "Noch ${remaining}ml bis zu deinem Ziel!"
        } else {
            "Großartig! Du hast dein Wasserziel erreicht! 🎉"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_WATER_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💧 Wasser trinken!")
            .setContentText("Zeit für ein Glas Wasser")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$progressText\n\nAktuell: ${currentIntake}ml von ${targetIntake}ml"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Getrunken",
                pendingIntent
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_WATER_REMINDER,
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    /**
     * Show macro warning notification
     */
    fun showMacroWarning(context: Context, macroType: String, current: Int, target: Int, isOverTarget: Boolean) {
        if (!hasNotificationPermission(context)) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "nutrition")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_MACRO_WARNING + macroType.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val warningText = if (isOverTarget) {
            "⚠️ $macroType-Ziel überschritten!"
        } else {
            "📊 $macroType-Ziel noch nicht erreicht"
        }
        
        val detailText = if (isOverTarget) {
            "Du hast bereits ${current}g $macroType von ${target}g erreicht. Achte auf deine nächsten Mahlzeiten!"
        } else {
            "Du hast ${current}g von ${target}g $macroType erreicht. Noch ${target - current}g bis zum Ziel!"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_MACRO_WARNINGS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(warningText)
            .setContentText("$macroType: ${current}g / ${target}g")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(detailText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Überprüfen",
                pendingIntent
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_MACRO_WARNING + macroType.hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    /**
     * Show notification when user joins a challenge
     */
    fun showChallengeJoined(context: Context, challenge: SocialChallengeEntity) {
        if (!hasNotificationPermission(context)) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "challenges") // For navigation to challenges screen
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SOCIAL_CHALLENGES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Challenge beigetreten! 🎯")
            .setContentText("Du bist jetzt Teil der \"${challenge.title}\" Challenge!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Du bist jetzt Teil der \"${challenge.title}\" Challenge!\n\nZiel: ${challenge.targetValue} ${challenge.unit} in ${challenge.duration} Tagen\n\nViel Erfolg! 💪"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 300, 300))
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_CHALLENGE_JOINED + challenge.id.toInt(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    /**
     * Show notification when user completes a challenge
     */
    fun showChallengeCompleted(context: Context, challenge: SocialChallengeEntity) {
        if (!hasNotificationPermission(context)) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "achievements") // For navigation to achievements screen
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SOCIAL_CHALLENGES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Challenge geschafft! 🏆")
            .setContentText("Glückwunsch! Du hast \"${challenge.title}\" erfolgreich abgeschlossen!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("🎉 CHALLENGE ABGESCHLOSSEN! 🎉\n\n\"${challenge.title}\"\n\nDu hast ${challenge.targetValue} ${challenge.unit} erreicht!\n\n${challenge.reward?.let { "Belohnung: $it" } ?: ""}\n\nAuf zur nächsten Challenge! 💪"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_CHALLENGE_COMPLETED + challenge.id.toInt(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    /**
     * Show notification for challenge progress updates
     */
    fun showChallengeProgressUpdate(context: Context, challengeTitle: String, progress: Double, target: Double, rank: Int) {
        if (!hasNotificationPermission(context)) return
        
        val progressPercentage = (progress / target * 100).toInt()
        val remainingProgress = target - progress
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "challenges")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_CHALLENGE_UPDATES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Challenge Update 📊")
            .setContentText("$challengeTitle: $progressPercentage% erreicht (Rang #$rank)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$challengeTitle\n\n📊 Fortschritt: $progress / $target\n🎯 $progressPercentage% erreicht\n🏆 Aktueller Rang: #$rank\n\nNoch ${remainingProgress.toInt()} bis zum Ziel!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_CHALLENGE_UPDATE + challengeTitle.hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }
    
    data class PersonalStats(
        val activeStreaks: Int,
        val completedAchievements: Int,
        val longestStreak: Int,
        val totalWorkouts: Int
    )
}

// Missing enums referenced in build errors
enum class NotificationCategory {
    WORKOUT,
    NUTRITION, 
    MEDIA,
    GENERAL,
    NOT_REQUIRED
}

enum class NetworkRequirement {
    CONNECTED,
    UNMETERED,
    ANY
}