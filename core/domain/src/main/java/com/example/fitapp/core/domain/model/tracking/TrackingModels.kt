package com.example.fitapp.core.domain.model.tracking

import java.time.LocalDate

/**
 * Domain model for workout/training session
 */
data class Workout(
    val id: Long = 0,
    val name: String,
    val date: LocalDate,
    val durationMinutes: Int,
    val exercises: List<Exercise> = emptyList(),
    val status: WorkoutStatus = WorkoutStatus.PLANNED,
    val completedAt: Long? = null
)

/**
 * Domain model for individual exercise
 */
data class Exercise(
    val id: Long = 0,
    val name: String,
    val sets: List<ExerciseSet> = emptyList(),
    val restTimeSeconds: Int = 60,
    val notes: String = ""
)

/**
 * Domain model for exercise set
 */
data class ExerciseSet(
    val id: Long = 0,
    val reps: Int,
    val weightKg: Float? = null,
    val durationSeconds: Int? = null,
    val completed: Boolean = false
)

/**
 * Workout status enumeration
 */
enum class WorkoutStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED
}

/**
 * Domain model for HIIT workout
 */
data class HIITWorkout(
    val id: Long = 0,
    val name: String,
    val exercises: List<HIITExercise>,
    val workSeconds: Int = 30,
    val restSeconds: Int = 15,
    val rounds: Int = 1,
    val setBreakSeconds: Int = 60
)

/**
 * Domain model for HIIT exercise
 */
data class HIITExercise(
    val name: String,
    val description: String = ""
)

/**
 * Domain model for training preferences
 */
data class TrainingPreferences(
    val defaultRestTimeSeconds: Int = 60,
    val reminderEnabled: Boolean = true,
    val preferredWorkoutDurationMinutes: Int = 45
)