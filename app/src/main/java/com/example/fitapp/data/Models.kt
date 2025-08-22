package com.example.fitapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

enum class Goal { Abnehmen, Muskelaufbau, Ausdauer, FitBleiben }

data class Device(val name: String, val icon: String? = null)

data class CalorieSettings(
    val goal: Goal = Goal.Abnehmen,
    val dailyBudget: Int = 2000
)

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

data class ExerciseLog(
    val date: LocalDate,
    val title: String,
    val durationMin: Int,
    val caloriesOut: Int = 0
)

data class FoodLog(
    val date: LocalDate,
    val title: String,
    val caloriesIn: Int
)

data class Recipe(
    val title: String,
    val minutes: Int,
    val calories: Int,
    val tags: List<String>,
    val ingredients: List<Pair<String, String>>, // name -> qty (z.B. "Hähnchen" to "300g")
    val steps: List<String>
)

data class ShoppingItem(
    val id: Long,
    val name: String,
    val quantity: String,
    val checked: Boolean = false
)
