package com.example.fitapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.fitapp.data.*
import com.example.fitapp.logic.PlanGenerator
import com.example.fitapp.ui.components.*
import com.example.fitapp.ui.design.Spacing

// WICHTIG: unseren FilterChip klar vom M3-FilterChip unterscheiden
import com.example.fitapp.ui.components.FilterChip as AppFilterChip

@Composable
fun TrainingSetupScreen() {
    var goal by remember { mutableStateOf(Goal.Abnehmen) }
    val devices by AppRepository.devices.collectAsState()
    val selectedNames by AppRepository.selectedDeviceNames.collectAsState()

    var timePerUnit by remember { mutableStateOf("30") }
    var sessions by remember { mutableStateOf("3") }
    var budget by remember { mutableStateOf("2000") }

    var showAddDevice by remember { mutableStateOf(false) }
    var newDeviceName by remember { mutableStateOf("") }

    // --- Sektion: Ziel & Geräte ---
    SectionCard(title = "Ziel & Geräte") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Ziel", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Goal.values().forEach { g ->
                    AppFilterChip(
                        text = g.name,
                        selected = goal == g,
                        onClick = { goal = g }
                    )
                }
            }

            Text("Geräte", style = MaterialTheme.typography.titleSmall)
            FlowChips(
                items = devices.map { it.name },
                selected = selectedNames,
                onToggle = { name -> AppRepository.toggleDevice(name) }
            )

            TextButton(onClick = { showAddDevice = true }) {
                Text("+ Gerät hinzufügen")
            }
        }
    }

    // --- Sektion: Zeit & Kalorien ---
    SectionCard(title = "Zeit & Kalorien") {
        NumberField(label = "Minuten pro Einheit", value = timePerUnit, onValueChange = { timePerUnit = it })
        Spacer(Modifier.height(Spacing.md))
        NumberField(label = "Einheiten pro Woche", value = sessions, onValueChange = { sessions = it })
        Spacer(Modifier.height(Spacing.md))
        NumberField(label = "Tägliches Kalorienbudget", value = budget, onValueChange = { budget = it })
    }

    // --- Aktionen: Plan generieren / Alternative ---
    InlineActions(
        primaryLabel = "Grundplan generieren",
        onPrimary = {
            val selectedDevices = AppRepository.getSelectedDevices().ifEmpty { listOf(Device("Körpergewicht")) }
            val plan = PlanGenerator.generateBasePlan(
                goal = goal,
                devices = selectedDevices,
                timeBudgetMin = timePerUnit.toIntOrNull() ?: 30,
                sessionsPerWeek = sessions.toIntOrNull() ?: 3
            )
            AppRepository.setPlan(plan)
            AppRepository.setCalorieSettings(CalorieSettings(goal, budget.toIntOrNull() ?: 2000))
        },
        secondaryLabel = "Alternative für heute",
        onSecondary = {
            val p = AppRepository.plan.value ?: return@InlineActions
            val alt = PlanGenerator.alternativeForToday(
                goal = p.goal,
                deviceHint = p.devices.firstOrNull()?.name ?: "Körpergewicht",
                timeMin = p.timeBudgetMin
            )
            AppRepository.logExercise(alt.title, alt.durationMin, alt.durationMin * 6)
        },
        modifier = Modifier.padding(horizontal = Spacing.lg)
    )

    // --- Sektion: Dein Plan (kompakt) ---
    val plan by AppRepository.plan.collectAsState()
    SectionCard(
        title = "Dein Plan",
        subtitle = if (plan == null) "Noch kein Plan generiert" else "${plan!!.sessionsPerWeek} Einheiten · ${plan!!.timeBudgetMin} min"
    ) {
        val p = plan
        if (p == null) {
            Text(
                "Lege Ziel, Zeit & Geräte fest und tippe auf „Grundplan generieren“.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                p.week.forEach { day ->
                    Text("• ${day.title} – ${day.durationMin} min")
                }
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Geräte: ${p.devices.joinToString { it.name }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // --- Dialog: neues Gerät hinzufügen ---
    if (showAddDevice) {
        AlertDialog(
            onDismissRequest = { showAddDevice = false },
            title = { Text("Neues Gerät") },
            text = {
                OutlinedTextField(
                    value = newDeviceName,
                    onValueChange = { newDeviceName = it },
                    singleLine = true,
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    AppRepository.addDevice(newDeviceName)
                    newDeviceName = ""
                    showAddDevice = false
                }) { Text("Hinzufügen") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDevice = false }) { Text("Abbrechen") }
            }
        )
    }
}

/** Chips im Zeilen-Wrap (einfacher Flow) */
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
                row.forEach { name ->
                    AppFilterChip(
                        text = name,
                        selected = selected.contains(name),
                        onClick = { onToggle(name) }
                    )
                }
            }
        }
    }
}
