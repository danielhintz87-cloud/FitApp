package com.example.fitapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.example.fitapp.data.AppRepository
import com.example.fitapp.ui.components.MetricChip
import com.example.fitapp.ui.components.SectionCard
import com.example.fitapp.ui.design.Spacing
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun ProgressScreen() {
    val workouts by AppRepository.exerciseLogs.collectAsState()
    val foods by AppRepository.foodLogs.collectAsState()

    val week = currentWeekNumber()
    val thisWeekWorkouts = workouts.filter { weekOf(it.date) == week }
    val minutes = thisWeekWorkouts.sumOf { it.durationMin }
    val kcalOut = thisWeekWorkouts.sumOf { it.caloriesOut }
    val kcalIn = foods.filter { weekOf(it.date) == week }.sumOf { it.caloriesIn }

    SectionCard(title = "Diese Woche") {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            MetricChip("Workouts", thisWeekWorkouts.size.toString())
            MetricChip("Minuten", minutes.toString())
            MetricChip("kcal aufgenommen", kcalIn.toString(), filled = false)
            MetricChip("kcal verbraucht", kcalOut.toString(), filled = false)
        }
    }
}

private fun currentWeekNumber(): Int = weekOf(LocalDate.now())
private fun weekOf(date: LocalDate): Int =
    date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
