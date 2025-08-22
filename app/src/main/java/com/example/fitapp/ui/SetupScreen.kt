package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.fitapp.ui.components.FilterChip as AppFilterChip
import com.example.fitapp.ui.components.InlineActions
import com.example.fitapp.ui.components.NumberField
import com.example.fitapp.ui.components.SectionCard
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

    var showAdd by remember { mutableStateOf(false) }
    var newDevice by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 96.dp)
    ) {
        SectionCard(title = "Ziel & Geräte") {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text("Ziel", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Goal.values().forEach {
                        AppFilterChip(text = it.name, selected = goal == it, onClick = { goal = it })
                    }
                }

                Text("Geräte", style = MaterialTheme.typography.titleSmall)
                FlowChips(
                    items = devices.map { it.name },
                    selected = selectedNames,
                    onToggle = { name -> AppRepository.toggleDevice(name) }
                )
                TextButton(onClick = { showAdd = true }) { Text("+ Gerät hinzufügen") }
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
            modifier = Modifier.padding(top = Spacing.lg)
        )
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Neues Gerät") },
            text = {
                OutlinedTextField(
                    value = newDevice,
                    onValueChange = { newDevice = it },
                    singleLine = true,
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    AppRepository.addDevice(newDevice)
                    newDevice = ""
                    showAdd = false
                }) { Text("Hinzufügen") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Abbrechen") } }
        )
    }
}

/** kleine Wrap-Helfer für Chips */
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
                    AppFilterChip(
                        text = txt,
                        selected = selected.contains(txt),
                        onClick = { onToggle(txt) }
                    )
                }
            }
        }
    }
}
