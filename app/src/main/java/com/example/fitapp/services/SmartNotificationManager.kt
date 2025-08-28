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

object SmartNotificationManager {
    
    // Notification channels
    private const val CHANNEL_ACHIEVEMENTS = "achievements"
    private const val CHANNEL_STREAKS = "streaks"
    private const val CHANNEL_MILESTONES = "milestones"
    private const val CHANNEL_DAILY_MOTIVATION = "daily_motivation"
    
    // Notification IDs
    private const val NOTIFICATION_ID_ACHIEVEMENT = 1000
    private const val NOTIFICATION_ID_STREAK_WARNING = 2000
    private const val NOTIFICATION_ID_STREAK_MILESTONE = 3000
    private const val NOTIFICATION_ID_DAILY_MOTIVATION = 4000
    
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
            
            notificationManager.createNotificationChannels(listOf(
                achievementChannel,
                streakChannel,
                milestoneChannel,
                motivationChannel
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
            0,
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
            0,
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
            0,
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
            0,
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
    
    data class PersonalStats(
        val activeStreaks: Int,
        val completedAchievements: Int,
        val longestStreak: Int,
        val totalWorkouts: Int
    )
}