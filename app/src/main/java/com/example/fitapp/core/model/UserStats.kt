package com.example.fitapp.core.model

data class UserStats(
    val activeStreaks: Int = 0,
    val longestStreak: Int = 0,
    val completedAchievements: Int = 0,
    val todayCalories: Int = 0,
    val calorieGoal: Int = 0,
    val hasCompletedWorkoutToday: Boolean = false,
    val hasLoggedNutritionToday: Boolean = false,
)
