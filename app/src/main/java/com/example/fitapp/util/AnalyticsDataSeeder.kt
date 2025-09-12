package com.example.fitapp.util

import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.BMIHistoryEntity
import com.example.fitapp.data.db.PersonalAchievementEntity
import com.example.fitapp.data.db.PersonalRecordEntity
import com.example.fitapp.data.db.PersonalStreakEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Utility class to seed sample data for testing Enhanced Analytics Dashboard
 */
class AnalyticsDataSeeder(private val db: AppDatabase) {
    suspend fun seedSampleData() {
        seedWeightHistory()
        seedAchievements()
        seedStreaks()
        seedPersonalRecords()
    }

    private suspend fun seedWeightHistory() {
        val today = LocalDate.now()
        val weights = listOf(85.0f, 84.5f, 84.2f, 83.8f, 83.5f, 83.1f, 82.8f)

        weights.forEachIndexed { index, weight ->
            val date = today.minusDays(index.toLong())
            val heightCm = 175f
            val bmi = weight / ((heightCm / 100) * (heightCm / 100))
            val category =
                when {
                    bmi < 18.5 -> "UNDERWEIGHT"
                    bmi < 25 -> "NORMAL"
                    bmi < 30 -> "OVERWEIGHT"
                    else -> "OBESE"
                }

            try {
                db.bmiHistoryDao().insert(
                    BMIHistoryEntity(
                        date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        height = heightCm,
                        weight = weight,
                        bmi = bmi,
                        category = category,
                        notes = "Sample data for analytics",
                    ),
                )
            } catch (e: Exception) {
                // Ignore if already exists
            }
        }
    }

    private suspend fun seedAchievements() {
        val achievements =
            listOf(
                PersonalAchievementEntity(
                    title = "Erste Woche",
                    description = "7 Tage Training abgeschlossen",
                    category = "training",
                    iconName = "fitness_center",
                    targetValue = 7.0,
                    currentValue = 7.0,
                    isCompleted = true,
                ),
                PersonalAchievementEntity(
                    title = "Kalorien-Meister",
                    description = "30 Tage Kalorienziel erreicht",
                    category = "nutrition",
                    iconName = "restaurant",
                    targetValue = 30.0,
                    currentValue = 22.0,
                    isCompleted = false,
                ),
                PersonalAchievementEntity(
                    title = "Fitness-Enthusiast",
                    description = "100 Workouts abgeschlossen",
                    category = "training",
                    iconName = "emoji_events",
                    targetValue = 100.0,
                    currentValue = 67.0,
                    isCompleted = false,
                ),
            )

        achievements.forEach { achievement ->
            try {
                db.personalAchievementDao().insert(achievement)
            } catch (e: Exception) {
                // Ignore if already exists
            }
        }
    }

    private suspend fun seedStreaks() {
        val streaks =
            listOf(
                PersonalStreakEntity(
                    name = "Training Streak",
                    description = "Tägliches Training",
                    category = "training",
                    currentStreak = 14,
                    longestStreak = 28,
                    lastActivityTimestamp = System.currentTimeMillis() / 1000,
                ),
                PersonalStreakEntity(
                    name = "Nutrition Streak",
                    description = "Tägliche Kalorienverfolgung",
                    category = "nutrition",
                    currentStreak = 8,
                    longestStreak = 15,
                    lastActivityTimestamp = System.currentTimeMillis() / 1000,
                ),
                PersonalStreakEntity(
                    name = "Water Streak",
                    description = "Tägliches Wasserziel",
                    category = "hydration",
                    currentStreak = 5,
                    longestStreak = 12,
                    lastActivityTimestamp = System.currentTimeMillis() / 1000,
                ),
            )

        streaks.forEach { streak ->
            try {
                db.personalStreakDao().insert(streak)
            } catch (e: Exception) {
                // Ignore if already exists
            }
        }
    }

    private suspend fun seedPersonalRecords() {
        val records =
            listOf(
                PersonalRecordEntity(
                    exerciseName = "Bankdrücken",
                    recordType = "1RM",
                    value = 85.0,
                    unit = "kg",
                    achievedAt = System.currentTimeMillis() / 1000,
                ),
                PersonalRecordEntity(
                    exerciseName = "Kniebeuge",
                    recordType = "1RM",
                    value = 120.0,
                    unit = "kg",
                    achievedAt = System.currentTimeMillis() / 1000,
                ),
                PersonalRecordEntity(
                    exerciseName = "Kreuzheben",
                    recordType = "1RM",
                    value = 140.0,
                    unit = "kg",
                    achievedAt = System.currentTimeMillis() / 1000,
                ),
            )

        records.forEach { record ->
            try {
                db.personalRecordDao().insert(record)
            } catch (e: Exception) {
                // Ignore if already exists
            }
        }
    }
}
