package com.example.fitapp.logic

import com.example.fitapp.data.*
import java.lang.StringBuilder

object PlanGenerator {
    fun generateBasePlan(
        goal: Goal,
        devices: List<Device>,
        timeBudgetMin: Int,
        sessionsPerWeek: Int
    ): Plan {
        val baseExercises = when (goal) {
            Goal.Abnehmen -> listOf(
                WorkoutExercise("Kniebeuge (Körpergewicht)", 3, 12),
                WorkoutExercise("Liegestütz (modifiziert ok)", 3, 10),
                WorkoutExercise("Rudern mit Band / Rudergerät", 3, 12)
            )
            Goal.Muskelaufbau -> listOf(
                WorkoutExercise("Goblet Squat (Kurzhantel)", 4, 10),
                WorkoutExercise("Bankdrücken KH", 4, 8),
                WorkoutExercise("Rudern KH/KB", 4, 10)
            )
            Goal.Ausdauer -> listOf(
                WorkoutExercise("Intervall: 5x (2min zügig / 1min locker) – Cardio", note = "Rudergerät/Laufband/Heimtrainer")
            )
            Goal.FitBleiben -> listOf(
                WorkoutExercise("Ganzkörper-Zirkel 3 Runden", note = "Kniebeuge, Liegestütz, Rudern, Plank")
            )
        }

        val days = (1..sessionsPerWeek).map { i ->
            WorkoutDay(
                title = "Tag $i",
                durationMin = timeBudgetMin,
                exercises = baseExercises
            )
        }

        val md = StringBuilder().apply {
            appendLine("# Wochenplan – ${goal.name}")
            appendLine("Geräte: ${devices.joinToString { it.name }}")
            appendLine()
            days.forEach { day ->
                appendLine("## ${day.title} – ${day.durationMin} min")
                day.exercises.forEach { ex ->
                    val detail = when {
                        ex.sets != null && ex.reps != null -> "(${ex.sets}×${ex.reps})"
                        else -> ex.note ?: ""
                    }
                    appendLine("- ${ex.name} $detail".trim())
                }
                appendLine()
            }
        }.toString()

        return Plan(goal, devices, timeBudgetMin, sessionsPerWeek, days, md)
    }

    fun alternativeForToday(goal: Goal, deviceHint: String, timeMin: Int): WorkoutDay =
        WorkoutDay(
            title = "Alternative (heute)",
            durationMin = timeMin,
            exercises = when (goal) {
                Goal.Abnehmen -> listOf(
                    WorkoutExercise("EMOM 20: 10 Kettlebell Swings, 10 Air Squats", note = "Gerät: $deviceHint")
                )
                Goal.Muskelaufbau -> listOf(
                    WorkoutExercise("Supersatz: Rudern + Liegestütz", 4, 12, note = "Gerät: $deviceHint")
                )
                Goal.Ausdauer -> listOf(
                    WorkoutExercise("Fahrtspiel $timeMin min – Wechsel locker/zügig", note = "Gerät: $deviceHint")
                )
                Goal.FitBleiben -> listOf(
                    WorkoutExercise("Zirkel $timeMin min – Ganzkörper", note = "Gerät: $deviceHint")
                )
            }
        )
}
