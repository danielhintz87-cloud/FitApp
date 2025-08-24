package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.AppRepository
import com.example.fitapp.ui.components.SectionCard
import com.example.fitapp.ui.components.EmptyState
import com.example.fitapp.ui.design.Spacing
import java.time.format.DateTimeFormatter

@Composable
fun ProgressScreen() {
    val exerciseLogs by AppRepository.exerciseLogs.collectAsState()
    val foodLogs by AppRepository.foodLogs.collectAsState()
    val dateFormat = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
            .padding(bottom = 96.dp)
    ) {
        Text("Fortschritt", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(Spacing.md))
        if (exerciseLogs.isEmpty() && foodLogs.isEmpty()) {
            EmptyState(
                title = "Noch nichts hier",
                message = "Bisher wurden keine Workouts oder Mahlzeiten protokolliert."
            )
        } else {
            if (exerciseLogs.isNotEmpty()) {
                SectionCard(title = "Trainings-Historie") {
                    exerciseLogs.forEach { log ->
                        Text("• ${dateFormat.format(log.date)}: ${log.title} – ${log.durationMin} min, ${log.caloriesOut} kcal")
                    }
                }
                Spacer(Modifier.height(Spacing.md))
            }
            if (foodLogs.isNotEmpty()) {
                SectionCard(title = "Ernährungs-Historie") {
                    foodLogs.forEach { log ->
                        Text("• ${dateFormat.format(log.date)}: ${log.title} – ${log.caloriesIn} kcal")
                    }
                }
            }
        }
    }
}
