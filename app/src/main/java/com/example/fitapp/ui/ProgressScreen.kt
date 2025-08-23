package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.AppRepository
import com.example.fitapp.ui.design.Spacing
import java.time.LocalDate

@Composable
fun ProgressScreen() {
    val exercises by AppRepository.exerciseLogs.collectAsState()
    val foods by AppRepository.foodLogs.collectAsState()

    val last7 = (0..6).map { LocalDate.now().minusDays(it.toLong()) }
    val wCount = exercises.count { it.date in last7 }
    val wMinutes = exercises.filter { it.date in last7 }.sumOf { it.durationMin }
    val kcalIn = foods.filter { it.date in last7 }.sumOf { it.caloriesIn }
    val kcalOut = exercises.filter { it.date in last7 }.sumOf { it.caloriesOut }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .padding(bottom = 96.dp, top = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text("Fortschritt", style = MaterialTheme.typography.titleLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
            StatCard("Workouts", "$wCount")
            StatCard("Minuten", "$wMinutes")
            StatCard("kcal in", "$kcalIn")
            StatCard("kcal out", "$kcalOut")
        }

        OutlinedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Spacing.md)) {
                Text("Letzte 7 Tage", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.sm))
                Text("Workouts: $wCount · Minuten: $wMinutes · kcal Bilanz: ${kcalOut - kcalIn}")
                Spacer(Modifier.height(Spacing.xs))
                Text("Tipp: Konsequent bleiben – 3–4 Einheiten/Woche und kcal in Budget.")
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    OutlinedCard(Modifier.weight(1f)) {
        Column(Modifier.padding(Spacing.md)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.xs))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}
