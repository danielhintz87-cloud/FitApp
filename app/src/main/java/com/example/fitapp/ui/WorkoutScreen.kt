package com.example.fitapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.AppRepository
import com.example.fitapp.data.Goal
import com.example.fitapp.logic.PlanGenerator
import com.example.fitapp.ui.components.*
import com.example.fitapp.ui.design.Spacing
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DailyWorkoutScreen() {
    val plan by AppRepository.plan.collectAsState()
    if (plan == null) {
        EmptyState(
            title = "Kein Plan vorhanden",
            message = "Erstelle deinen Grundplan im Setup-Tab."
        )
        return
    }

    val today = plan!!.week.firstOrNull()
    if (today == null) {
        EmptyState(title = "Plan leer", message = "Generiere den Plan erneut.")
        return
    }

    SectionCard(title = "Heute", subtitle = "${today.durationMin} min") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            today.exercises.forEach { ex ->
                Text("• ${ex.name}" + (ex.sets?.let { s -> ex.reps?.let { r -> "  ($s×$r)" } } ?: ""), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(Spacing.md))
        InlineActions(
            primaryLabel = "Training starten & loggen",
            onPrimary = {
                AppRepository.logExercise(title = today.title, durationMin = today.durationMin, caloriesOut = today.durationMin * 6 /* grob */)
            },
            secondaryLabel = "Alternative vorschlagen",
            onSecondary = {
                val alt = PlanGenerator.alternativeForToday(plan!!.goal, deviceHint = plan!!.devices.firstOrNull()?.name ?: "Körpergewicht", timeMin = plan!!.timeBudgetMin)
                // „Alternative anzeigen“: wir loggen sie nicht automatisch – nur Vorschlag
                AppRepository.logExercise(title = alt.title, durationMin = alt.durationMin, caloriesOut = alt.durationMin * 6)
            }
        )
    }

    SectionCard(title = "Wochenplan (Markdown)") {
        Text(plan!!.markdown)
    }
}
