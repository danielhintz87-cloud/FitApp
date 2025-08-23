package com.example.fitapp.data

import java.time.LocalDate

enum class Goal { Abnehmen, Muskelaufbau, Ausdauer, FitBleiben }

data class Device(val name: String)

data class WorkoutExercise(
    val name: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val note: String? = null
)

data class WorkoutDay(
    val title: String,
    val durationMin: Int,
    val exercises: List<WorkoutExercise>
)

data class Plan(
    val goal: Goal,
    val devices: List<Device>,
    val timeBudgetMin: Int,
    val sessionsPerWeek: Int,
    val week: List<WorkoutDay>,
    val markdown: String
)

data class CalorieSettings(
    val goal: Goal = Goal.FitBleiben,
    val dailyBudget: Int = 2000
)

data class ExerciseLog(
    val date: LocalDate,
    val title: String,
    val durationMin: Int,
    val caloriesOut: Int
)

data class FoodLog(
    val date: LocalDate,
    val title: String,
    val caloriesIn: Int
)

data class ShoppingItem(
    val id: Long,
    val name: String,
    val quantity: String,
    val checked: Boolean
)

data class CalorieEstimate(
    val label: String,
    val calories: Int,
    val confidence: Float,
    val note: String
)
