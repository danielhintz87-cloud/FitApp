package com.example.fitapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.*
import com.example.fitapp.data.ai.Ai
import com.example.fitapp.logic.PlanGenerator
import com.example.fitapp.ui.components.InlineActions
import com.example.fitapp.ui.components.SectionCard
import com.example.fitapp.ui.design.Spacing
import kotlinx.coroutines.launch

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

    val scope = rememberCoroutineScope()

    SectionCard(title = "Ziel & Geräte") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Ziel", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Goal.values().forEach { g ->
                    FilterChip(
                        selected = goal == g,
                        onClick = { goal = g },
                        label = { Text(g.name) }
                    )
                }
            }

            Text("Geräte", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                devices.forEach { d ->
                    val isSel = selectedNames.contains(d.name)
                    FilterChip(
                        selected = isSel,
                        onClick = { AppRepository.toggleDevice(d.name) },
                        label = { Text(d.name) }
                    )
                }
            }

            TextButton(onClick = { showAddDevice = true }) { Text("+ Gerät hinzufügen") }
        }
    }

    SectionCard(title = "Zeit & Kalorien") {
        OutlinedTextField(
            value = timePerUnit,
            onValueChange = { timePerUnit = it },
            label = { Text("Minuten pro Einheit") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Spacing.md))
        OutlinedTextField(
            value = sessions,
            onValueChange = { sessions = it },
            label = { Text("Einheiten pro Woche") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Spacing.md))
        OutlinedTextField(
            value = budget,
            onValueChange = { budget = it },
            label = { Text("Tägliches Kalorienbudget") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }

    InlineActions(
        primaryLabel = "Grundplan generieren",
        onPrimary = {
            val selectedDevices = AppRepository.getSelectedDevices().ifEmpty { listOf(Device("Körpergewicht")) }
            val minutes = timePerUnit.toIntOrNull() ?: 30
            val sess = sessions.toIntOrNull() ?: 3
            val scopeLocal = scope
            scopeLocal.launch {
                val plan = runCatching {
                    Ai.repo.generateBasePlan(goal, selectedDevices, minutes, sess)
                }.getOrElse {
                    PlanGenerator.generateBasePlan(goal, selectedDevices, minutes, sess)
                }
                AppRepository.setPlan(plan)
                AppRepository.setCalorieSettings(CalorieSettings(goal, budget.toIntOrNull() ?: 2000))
            }
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
        modifier = Modifier.padding(bottom = 96.dp, top = Spacing.md)
    )

    val plan by AppRepository.plan.collectAsState()
    SectionCard(
        title = "Dein Plan",
        subtitle = if (plan == null) "Noch kein Plan generiert" else "${plan!!.sessionsPerWeek} Einheiten · ${plan!!.timeBudgetMin} min"
    ) {
        val p = plan
        if (p == null) {
            Text("Lege Ziel, Zeit & Geräte fest und tippe auf „Grundplan generieren“. ",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                p.week.forEach { day -> Text("• ${day.title} – ${day.durationMin} min") }
            }
            Spacer(Modifier.height(Spacing.sm))
            Text("Geräte: ${p.devices.joinToString { it.name }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    label = { Text("Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val n = newDeviceName.trim()
                    if (n.isNotEmpty()) {
                        AppRepository.addDevice(n)
                        AppRepository.toggleDevice(n)
                    }
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
