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
                Goal.Abnehmen    -> "Full Body Fatburn #$idx"
                Goal.Muskelaufbau -> "Hypertrophie-Push/Pull #$idx"
                Goal.Ausdauer    -> "Cardio Intervall #$idx"
                Goal.FitBleiben  -> "Ganzkörper Vital #$idx"
            }
            // Primäre Übung abhängig von verfügbaren Geräten
            val deviceName = if (devices.isNotEmpty()) devices[(idx - 1) % devices.size].name else "Körpergewicht"
            val primaryExercise = when {
                deviceName.contains("Laufband", ignoreCase = true) ->
                    WorkoutExercise("Laufband – ${minutes} min Lauf", note = "Intervall")
                deviceName.contains("Rudergerät", ignoreCase = true) ->
                    WorkoutExercise("Rudern – ${minutes * 100} m", note = "Intervall")
                else ->
                    WorkoutExercise("$deviceName Squats", sets = 3, reps = 12)
            }
            val exercises = listOf(
                primaryExercise,
                WorkoutExercise("Liegestütze", sets = 3, reps = 10),
                WorkoutExercise("Plank", note = "3× 45s")
            )
            WorkoutDay(title = title, durationMin = minutes, exercises = exercises)
        }
        // Plan-Objekt mit Markdown-Zusammenfassung zurückgeben
        val deviceListText = if (devices.isEmpty()) "Körpergewicht" else devices.joinToString { it.name }
        return Plan(
            goal = goal,
            devices = devices,
            timeBudgetMin = minutes,
            sessionsPerWeek = sessions,
            week = week,
            markdown = "# Plan (lokal)\n- Ziel: ${goal.name}, $sessions×/Woche, $minutes min\n- Geräte: $deviceListText"
        )
    }

    fun alternativeForToday(goal: Goal, deviceHint: String, timeMin: Int): WorkoutDay {
        val title = when (goal) {
            Goal.Abnehmen    -> "HIIT Kurzworkout"
            Goal.Muskelaufbau -> "Supersatz Express"
            Goal.Ausdauer    -> "Tempo-Intervalle"
            Goal.FitBleiben  -> "Mobilität & Core"
        }
        // Erste Übung auf Basis des Gerätehinweises auswählen
        val firstExerciseName = when {
            deviceHint.contains("Laufband", ignoreCase = true) ->
                "Laufband – $timeMin min Lauf"
            deviceHint.contains("Rudergerät", ignoreCase = true) ->
                "Rudern – ${timeMin * 100} m"
            else ->
                "$deviceHint Row"
        }
        val exercises = listOf(
            WorkoutExercise(firstExerciseName, sets = 3, reps = 12),
            WorkoutExercise("Burpees", sets = 3, reps = 12),
            WorkoutExercise("Mountain Climbers", note = "3× 30s")
        )
        return WorkoutDay(title = title, durationMin = timeMin, exercises = exercises)
    }
}
