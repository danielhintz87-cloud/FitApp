package com.example.fitapp.domain

import com.example.fitapp.data.db.PersonalAchievementEntity

/**
 * Weight loss and BMI specific achievements
 */
object WeightLossAchievements {
    // Weight Loss Milestones
    val FIRST_KG_LOST =
        PersonalAchievementEntity(
            title = "Erster Kilogram",
            description = "Verliere dein erstes Kilogram",
            category = "weight_loss",
            iconName = "emoji_events",
            targetValue = 1.0,
            unit = "kg",
        )

    val FIVE_KG_MILESTONE =
        PersonalAchievementEntity(
            title = "5kg Meilenstein",
            description = "Erreiche 5kg Gewichtsverlust",
            category = "weight_loss",
            iconName = "military_tech",
            targetValue = 5.0,
            unit = "kg",
        )

    val TEN_KG_MILESTONE =
        PersonalAchievementEntity(
            title = "10kg Champion",
            description = "Erreiche 10kg Gewichtsverlust",
            category = "weight_loss",
            iconName = "workspace_premium",
            targetValue = 10.0,
            unit = "kg",
        )

    val TWENTY_KG_MILESTONE =
        PersonalAchievementEntity(
            title = "20kg Held",
            description = "Erreiche 20kg Gewichtsverlust",
            category = "weight_loss",
            iconName = "auto_awesome",
            targetValue = 20.0,
            unit = "kg",
        )

    // BMI Category Achievements
    val BMI_NORMAL_REACHED =
        PersonalAchievementEntity(
            title = "Normalgewicht erreicht",
            description = "BMI ist jetzt im Normalbereich (18.5-24.9)",
            category = "bmi",
            iconName = "health_and_safety",
            targetValue = 24.9,
            unit = "BMI",
        )

    val BMI_IMPROVED =
        PersonalAchievementEntity(
            title = "BMI verbessert",
            description = "BMI um mindestens 2 Punkte reduziert",
            category = "bmi",
            iconName = "trending_up",
            targetValue = 2.0,
            unit = "BMI Punkte",
        )

    // Behavioral and Habit Achievements
    val SUGAR_FREE_WEEK =
        PersonalAchievementEntity(
            title = "Zuckerfrei",
            description = "7 aufeinanderfolgende Tage ohne Zucker",
            category = "nutrition",
            iconName = "no_food",
            targetValue = 7.0,
            unit = "Tage",
        )

    val PORTION_CONTROL_MASTER =
        PersonalAchievementEntity(
            title = "Portionskontrolle",
            description = "21 Tage bewusste Portionsgrößen eingehalten",
            category = "habit",
            iconName = "restaurant",
            targetValue = 21.0,
            unit = "Tage",
        )

    val HYDRATION_HERO =
        PersonalAchievementEntity(
            title = "Hydration Held",
            description = "14 aufeinanderfolgende Tage Wasserziel erreicht",
            category = "nutrition",
            iconName = "water_drop",
            targetValue = 14.0,
            unit = "Tage",
        )

    val VEGETABLE_CHAMPION =
        PersonalAchievementEntity(
            title = "Gemüse Champion",
            description = "30 Tage täglich mindestens 5 Portionen Gemüse",
            category = "nutrition",
            iconName = "eco",
            targetValue = 30.0,
            unit = "Tage",
        )

    val MINDFUL_EATING_STREAK =
        PersonalAchievementEntity(
            title = "Achtsames Essen",
            description = "14 Tage regelmäßige Verhaltens-Check-ins",
            category = "mindfulness",
            iconName = "psychology",
            targetValue = 14.0,
            unit = "Check-ins",
        )

    // Consistency Achievements
    val CALORIE_STREAK_WEEK =
        PersonalAchievementEntity(
            title = "Kalorienziel-Woche",
            description = "7 aufeinanderfolgende Tage Kalorienziel erreicht",
            category = "consistency",
            iconName = "local_fire_department",
            targetValue = 7.0,
            unit = "Tage",
        )

    val CALORIE_STREAK_MONTH =
        PersonalAchievementEntity(
            title = "Kalorienziel-Monat",
            description = "30 aufeinanderfolgende Tage Kalorienziel erreicht",
            category = "consistency",
            iconName = "local_fire_department",
            targetValue = 30.0,
            unit = "Tage",
        )

    val WEIGHT_LOGGING_CONSISTENT =
        PersonalAchievementEntity(
            title = "Gewichts-Tracker",
            description = "21 Tage regelmäßiges Gewicht loggen",
            category = "consistency",
            iconName = "scale",
            targetValue = 21.0,
            unit = "Einträge",
        )

    // Progress Photo Achievements
    val FIRST_PROGRESS_PHOTO =
        PersonalAchievementEntity(
            title = "Fortschritt dokumentiert",
            description = "Erstes Fortschrittsfoto aufgenommen",
            category = "progress",
            iconName = "photo_camera",
            targetValue = 1.0,
            unit = "Foto",
        )

    val MONTHLY_PROGRESS_PHOTOS =
        PersonalAchievementEntity(
            title = "Monatliche Dokumentation",
            description = "4 Wochen lang wöchentliche Fortschrittsfotos",
            category = "progress",
            iconName = "photo_library",
            targetValue = 4.0,
            unit = "Fotos",
        )

    // Exercise Integration Achievements
    val EXERCISE_CONSISTENCY =
        PersonalAchievementEntity(
            title = "Training & Abnehmen",
            description = "14 Tage Training und Kalorienziel kombiniert",
            category = "integration",
            iconName = "fitness_center",
            targetValue = 14.0,
            unit = "Tage",
        )

    /**
     * Get all weight loss achievements
     */
    fun getAllWeightLossAchievements(): List<PersonalAchievementEntity> =
        listOf(
            FIRST_KG_LOST,
            FIVE_KG_MILESTONE,
            TEN_KG_MILESTONE,
            TWENTY_KG_MILESTONE,
            BMI_NORMAL_REACHED,
            BMI_IMPROVED,
            SUGAR_FREE_WEEK,
            PORTION_CONTROL_MASTER,
            HYDRATION_HERO,
            VEGETABLE_CHAMPION,
            MINDFUL_EATING_STREAK,
            CALORIE_STREAK_WEEK,
            CALORIE_STREAK_MONTH,
            WEIGHT_LOGGING_CONSISTENT,
            FIRST_PROGRESS_PHOTO,
            MONTHLY_PROGRESS_PHOTOS,
            EXERCISE_CONSISTENCY,
        )

    /**
     * Get achievements by category
     */
    fun getAchievementsByCategory(category: String): List<PersonalAchievementEntity> {
        return getAllWeightLossAchievements().filter { it.category == category }
    }
}

/**
 * Weight loss challenges for gamification
 */
data class WeightLossChallenge(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int, // days
    val targetMetric: ChallengeMetric,
    val targetValue: Float,
    val reward: String,
    val difficulty: ChallengeDifficulty,
)

enum class ChallengeMetric {
    CALORIE_DEFICIT,
    SUGAR_INTAKE,
    STEPS,
    WATER_INTAKE,
    VEGETABLE_SERVINGS,
    PROTEIN_TARGET,
    MINDFUL_EATING,
    WEIGHT_LOSS,
    BMI_REDUCTION,
}

enum class ChallengeDifficulty(val germanName: String) {
    EASY("Leicht"),
    MEDIUM("Mittel"),
    HARD("Schwer"),
    EXPERT("Experte"),
}

/**
 * Predefined weight loss challenges
 */
object WeightLossChallenges {
    val SEVEN_DAY_WATER =
        WeightLossChallenge(
            id = "7day_water",
            title = "7-Tage Wasser Challenge",
            description = "Trinke 7 Tage lang täglich dein Wasserziel",
            duration = 7,
            targetMetric = ChallengeMetric.WATER_INTAKE,
            targetValue = 100f, // 100% of daily goal
            reward = "Hydration Hero Badge",
            difficulty = ChallengeDifficulty.EASY,
        )

    val FOURTEEN_DAY_CALORIE =
        WeightLossChallenge(
            id = "14day_calorie",
            title = "14-Tage Kalorienziel",
            description = "Halte 14 Tage lang dein tägliches Kalorienziel ein",
            duration = 14,
            targetMetric = ChallengeMetric.CALORIE_DEFICIT,
            targetValue = 100f,
            reward = "Konsistenz Champion",
            difficulty = ChallengeDifficulty.MEDIUM,
        )

    val SUGAR_FREE_MONTH =
        WeightLossChallenge(
            id = "sugar_free_month",
            title = "Zucker-freier Monat",
            description = "30 Tage ohne zugesetzten Zucker",
            duration = 30,
            targetMetric = ChallengeMetric.SUGAR_INTAKE,
            targetValue = 0f,
            reward = "Sugar-Free Warrior",
            difficulty = ChallengeDifficulty.HARD,
        )

    val MINDFUL_EATING_WEEK =
        WeightLossChallenge(
            id = "mindful_week",
            title = "Achtsames Essen",
            description = "7 Tage täglich mindful eating Check-ins",
            duration = 7,
            targetMetric = ChallengeMetric.MINDFUL_EATING,
            targetValue = 1f, // 1 check-in per day
            reward = "Mindfulness Master",
            difficulty = ChallengeDifficulty.MEDIUM,
        )

    val FIVE_KG_IN_MONTH =
        WeightLossChallenge(
            id = "5kg_month",
            title = "5kg in 4 Wochen",
            description = "Verliere 5kg in 28 Tagen auf gesunde Weise",
            duration = 28,
            targetMetric = ChallengeMetric.WEIGHT_LOSS,
            targetValue = 5f,
            reward = "Rapid Progress Award",
            difficulty = ChallengeDifficulty.EXPERT,
        )

    fun getAllChallenges(): List<WeightLossChallenge> =
        listOf(
            SEVEN_DAY_WATER,
            FOURTEEN_DAY_CALORIE,
            SUGAR_FREE_MONTH,
            MINDFUL_EATING_WEEK,
            FIVE_KG_IN_MONTH,
        )
}
