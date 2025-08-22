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
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.*
import com.example.fitapp.logic.PlanGenerator
import com.example.fitapp.ui.components.*
import com.example.fitapp.ui.design.Spacing
import java.time.LocalDate

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

    SectionCard(title = "Ziel & Geräte") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Ziel", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Goal.values().forEach {
                    FilterChip(text = it.name, selected = goal == it, onClick = { goal = it })
                }
            }

            Text("Geräte", style = MaterialTheme.typography.titleSmall)
            FlowChips(
                items = devices.map { it.name },
                selected = selectedNames,
                onToggle = { name -> AppRepository.toggleDevice(name) }
            )

            TextButton(onClick = { showAddDevice = true }) { Text("+ Gerät hinzufügen") }
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
            val selected = AppRepository.getSelectedDevices().ifEmpty { listOf(Device("Körpergewicht")) }
            val plan = PlanGenerator.generateBasePlan(
                goal = goal,
                devices = selected,
                timeBudgetMin = timePerUnit.toIntOrNull() ?: 30,
                sessionsPerWeek = sessions.toIntOrNull() ?: 3
            )
            AppRepository.setBasePlan(plan)
            AppRepository.setCalorieSettings(CalorieSettings(goal, budget.toIntOrNull() ?: 2000))
        },
        secondaryLabel = "Alternative für heute",
        onSecondary = {
            val ps = AppRepository.planState.value ?: return@InlineActions
            val alt = PlanGenerator.alternativeForToday(
                goal = ps.base.goal,
                deviceHint = ps.base.devices.firstOrNull()?.name ?: "Körpergewicht",
                timeMin = ps.base.timeBudgetMin
            )
            AppRepository.setOverrideFor(LocalDate.now(), alt, OverrideReason.Alternative)
        },
        modifier = Modifier.padding(horizontal = Spacing.lg)
    )

    val plan by AppRepository.plan.collectAsState()
    SectionCard(
        title = "Dein Plan",
        subtitle = if (plan == null) "Noch kein Plan generiert"
        else "${plan!!.sessionsPerWeek} Einheiten · ${plan!!.timeBudgetMin} min"
    ) {
        val p = plan
        if (p == null) {
            Text("Lege Ziel, Zeit & Geräte fest und tippe auf „Grundplan generieren“.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                p.week.forEach { day -> Text("• ${day.title} – ${day.durationMin} min") }
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Geräte: ${p.devices.joinToString { it.name }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showAddDevice) {
        AlertDialog(
            onDismissRequest = { showAddDevice = false },
            title = { Text("Neues Gerät") },
            text = {
                OutlinedTextField(
                    value = newDeviceName,
                    onValueChange = { newDeviceName = it },
                    singleLine = true,
                    label = { Text("Name (z. B. „Springseil“)")}
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    AppRepository.addDevice(newDeviceName)
                    newDeviceName = ""
                    showAddDevice = false
                }) { Text("Hinzufügen") }
            },
            dismissButton = { TextButton(onClick = { showAddDevice = false }) { Text("Abbrechen") } }
        )
    }
}

/** Mini-Flow-Layout (wrap) für Chips */
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
                    FilterChip(text = txt, selected = selected.contains(txt), onClick = { onToggle(txt) })
                }
            }
        }
    }
}
