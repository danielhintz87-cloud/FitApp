package com.example.fitapp.shared

import kotlinx.serialization.Serializable

/**
 * Shared data models for communication between phone and watch
 */

@Serializable
data class WearWorkoutState(
    val isActive: Boolean = false,
    val exerciseName: String = "",
    val currentSet: Int = 0,
    val totalSets: Int = 0,
    val currentReps: Int = 0,
    val targetReps: Int = 0,
    val restTimeRemaining: Int = 0,
    val isResting: Boolean = false,
    val elapsedTime: Long = 0L,
    val heartRate: Int = 0,
    val caloriesBurned: Int = 0,
    val workoutId: String = "",
)

@Serializable
data class WearProgressData(
    val totalWorkouts: Int = 0,
    val weeklyWorkouts: Int = 0,
    val currentStreak: Int = 0,
    val personalRecords: List<WearPersonalRecord> = emptyList(),
    val lastWorkoutDate: String = "",
    val weeklyCaloriesBurned: Int = 0,
)

@Serializable
data class WearPersonalRecord(
    val exerciseName: String,
    val value: Float,
    val unit: String,
    val achievedDate: String,
)

@Serializable
data class WearNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: WearNotificationType,
    val timestamp: Long,
    val actionRequired: Boolean = false,
)

@Serializable
enum class WearNotificationType {
    WORKOUT_REMINDER,
    ACHIEVEMENT_UNLOCKED,
    STREAK_MILESTONE,
    REST_TIMER_COMPLETE,
    HYDRATION_REMINDER,
    MOTIVATIONAL_MESSAGE,
}

@Serializable
data class WearWorkoutAction(
    val action: WearActionType,
    val workoutId: String = "",
    val exerciseId: String = "",
    val setIndex: Int = -1,
    val reps: Int = 0,
    val weight: Float = 0f,
)

@Serializable
enum class WearActionType {
    START_WORKOUT,
    PAUSE_WORKOUT,
    RESUME_WORKOUT,
    COMPLETE_WORKOUT,
    COMPLETE_SET,
    SKIP_REST,
    UPDATE_HEART_RATE,
    SYNC_REQUEST,
}

// Data paths for Wearable Data Layer API
object WearDataPaths {
    const val WORKOUT_STATE = "/fitapp/workout/state"
    const val PROGRESS_DATA = "/fitapp/progress"
    const val NOTIFICATIONS = "/fitapp/notifications"
    const val ACTIONS = "/fitapp/actions"
    const val SYNC_REQUEST = "/fitapp/sync"
}
