package com.example.fitapp.core.domain.model.hydration

import java.time.LocalDate

/**
 * Domain model for hydration tracking
 */
data class HydrationGoal(
    val date: LocalDate,
    val targetMl: Int,
    val currentMl: Int = 0
) {
    val progressPercentage: Float
        get() = if (targetMl > 0) (currentMl.toFloat() / targetMl) * 100f else 0f
        
    val isCompleted: Boolean
        get() = currentMl >= targetMl
}

/**
 * Domain model for water intake entries
 */
data class WaterIntake(
    val id: Long = 0,
    val date: LocalDate,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Domain model for hydration preferences
 */
data class HydrationPreferences(
    val defaultDailyGoalMl: Int = 2000,
    val reminderEnabled: Boolean = true,
    val reminderIntervalMinutes: Int = 120
)