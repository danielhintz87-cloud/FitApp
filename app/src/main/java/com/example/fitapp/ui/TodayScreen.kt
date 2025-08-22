package com.example.fitapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.AppRepository
import com.example.fitapp.data.OverrideReason
import com.example.fitapp.data.unitFor
import com.example.fitapp.logic.PlanGenerator
import com.example.fitapp.ui.components.InlineActions
import com.example.fitapp.ui.components.MetricChip
import com.example.fitapp.ui.components.SectionCard
import java.time.LocalDate

@Composable
fun TodayScreen() {
    val ps by AppRepository.planState.collectAsState()
    val foods by AppRepository.foodLogs.collectAsState()
    val workouts by AppRepository.exerciseLogs.collectAsState()
    val today = LocalDate.now()

    val todayUnit = ps?.unitFor(today)
    var showDetails by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                        startY = 0f, endY = 800f
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Heute", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(160.dp))

            SectionCard(
                title = todayUnit?.title ?: "Keine Einheit geplant",
                subtitle = todayUnit?.durationMin?.let { "$it min" }
            ) {
                if (todayUnit == null) {
                    Text(
                        "Lege unter „Setup“ deinen Grundplan an, dann erscheint hier deine heutige Einheit.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        todayUnit.exercises.forEach { ex ->
                            val detail = when {
                                ex.sets != null && ex.reps != null -> " (${ex.sets}×${ex.reps})"
                                ex.note != null -> " – ${ex.note}"
                                else -> ""
                            }
                            Text("• ${ex.name}$detail")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    InlineActions(
                        primaryLabel = "Training starten",
                        onPrimary = {
                            AppRepository.logExercise(todayUnit.title, todayUnit.durationMin, todayUnit.durationMin * 6)
                        },
                        secondaryLabel = "Alternative",
                        onSecondary = {
                            val base = ps?.base ?: return@InlineActions
                            val alt = PlanGenerator.alternativeForToday(
                                goal = base.goal,
                                deviceHint = base.devices.firstOrNull()?.name ?: "Körpergewicht",
                                timeMin = base.timeBudgetMin
                            )
                            AppRepository.setOverrideFor(today, alt, OverrideReason.Alternative)
                        }
                    )
                    TextButton(onClick = { showDetails = true }) { Text("Details") }
                }
            }

            val inKcal = foods.filter { it.date == today }.sumOf { it.caloriesIn }
            val outKcal = workouts.filter { it.date == today }.sumOf { it.caloriesOut }
            SectionCard(title = "Kalorien heute") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricChip("Aufnahme", "$inKcal kcal")
                    MetricChip("Verbrauch", "$outKcal kcal", filled = false)
                }
            }
        }
    }

    if (showDetails && ps != null) {
        PlanMarkdownDialog(markdown = ps!!.base.markdown) { showDetails = false }
    }
}
