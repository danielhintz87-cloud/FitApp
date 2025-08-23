package com.example.fitapp.logic

import com.example.fitapp.data.*

object PlanGenerator {

    fun generateBasePlan(
        goal: Goal,
        devices: List<Device>,
        minutes: Int,
        sessions: Int
    ): Plan {
        val week = (1..sessions).map { idx ->
            val title = when (goal) {
                Goal.Abnehmen -> "Full Body Fatburn #$idx"
                Goal.Muskelaufbau -> "Hypertrophie-Push/Pull #$idx"
                Goal.Ausdauer -> "Cardio Intervall #$idx"
                Goal.FitBleiben -> "Ganzkörper Vital #$idx"
            }
            val ex = listOf(
                WorkoutExercise("${devices.firstOrNull()?.name ?: "Körpergewicht"} Squats", 3, 12),
                WorkoutExercise("Liegestütze", 3, 10),
                WorkoutExercise("Plank", note = "3× 45s")
            )
            WorkoutDay(title = title, durationMin = minutes, exercises = ex)
        }
        return Plan(
            goal = goal,
            devices = devices,
            timeBudgetMin = minutes,
            sessionsPerWeek = sessions,
            week = week,
            markdown = "# Plan (lokal)\n– ${goal.name} · $sessions×/Woche · ${minutes} min"
        )
    }

    fun alternativeForToday(goal: Goal, deviceHint: String, timeMin: Int): WorkoutDay {
        val title = when (goal) {
            Goal.Abnehmen -> "HIIT Kurzworkout"
            Goal.Muskelaufbau -> "Supersatz Express"
            Goal.Ausdauer -> "Tempo-Intervalle"
            Goal.FitBleiben -> "Mobilität & Core"
        }
        val ex = listOf(
            WorkoutExercise("$deviceHint Row", 3, 12),
            WorkoutExercise("Burpees", 3, 12),
            WorkoutExercise("Mountain Climbers", note = "3× 30s")
        )
        return WorkoutDay(title, timeMin, ex)
    }
}
