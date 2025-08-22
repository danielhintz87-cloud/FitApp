package com.example.fitapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.*
import com.example.fitapp.logic.PlanGenerator
import com.example.fitapp.ui.components.*
import com.example.fitapp.ui.design.Spacing

@Composable
fun TrainingSetupScreen() {
    var goal by remember { mutableStateOf(Goal.Abnehmen) }
    val available = listOf("Kurzhantel", "Kettlebell", "Bänder", "Klimmzugstange", "Rudergerät", "Laufband", "Matte")
    val selected = remember { mutableStateListOf<String>() }
    var timePerUnit by remember { mutableStateOf("30") }
    var sessions by remember { mutableStateOf("3") }
    var budget by remember { mutableStateOf("2000") }

    SectionCard(title = "Ziel & Geräte") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Ziel")
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Goal.values().forEach {
                    FilterChip(text = it.name, selected = goal == it, onClick = { goal = it })
                }
            }
            Text("Geräte")
            FlowChips(
                items = available,
                selected = selected.toSet(),
                onToggle = { item ->
                    if (selected.contains(item)) selected.remove(item) else selected.add(item)
                }
            )
        }
    }

    SectionCard(title = "Zeit & Kalorien") {
        NumberField(label = "Minuten pro Einheit", value = timePerUnit, onValueChange = { timePerUnit = it })
        Spacer(Modifier.height(Spacing.md))
        NumberField(label = "Einheiten pro Woche", value = sessions, onValueChange = { sessions = it })
        Spacer(Modifier.height(Spacing.md))
        NumberField(label = "Tägliches Kalorienbudget", value = budget, onValueChange = { budget = it })
    }

    InlineActions(
        primaryLabel = "Grundplan generieren",
        onPrimary = {
            val plan = PlanGenerator.generateBasePlan(
                goal = goal,
                devices = selected.map { Device(it) },
                timeBudgetMin = timePerUnit.toIntOrNull() ?: 30,
                sessionsPerWeek = sessions.toIntOrNull() ?: 3
            )
            AppRepository.setPlan(plan)
            AppRepository.setCalorieSettings(CalorieSettings(goal, budget.toIntOrNull() ?: 2000))
        },
        secondaryLabel = null,
        onSecondary = null,
        modifier = Modifier.padding(horizontal = Spacing.lg)
    )
}

/** Kleine Helper-Composable für „Flow“-Chips (wrap) */
@Composable
private fun FlowChips(
    items: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    val rows = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                row.forEach { txt ->
                    FilterChip(
                        text = txt,
                        selected = selected.contains(txt),
                        onClick = { onToggle(txt) }
                    )
                }
            }
        }
    }
}
