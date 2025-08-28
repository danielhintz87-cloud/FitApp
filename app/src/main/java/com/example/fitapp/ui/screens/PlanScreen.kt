package com.example.fitapp.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.PlanRequest
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.UserPreferences
import com.example.fitapp.data.repo.NutritionRepository
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun PlanScreen(contentPadding: PaddingValues, navController: NavController? = null) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    var goal by remember { mutableStateOf("Muskelaufbau") }
    var selectedDays by remember { mutableStateOf(setOf("MONDAY", "WEDNESDAY", "FRIDAY")) }
    var minutes by remember { mutableStateOf("60") }
    var equipment by remember { mutableStateOf("Hanteln, Klimmzugstange") }
    var result by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var saveStatus by remember { mutableStateOf("") }
    
    // Load saved equipment initially
    LaunchedEffect(Unit) {
        val savedEquipment = UserPreferences.getSelectedEquipment(ctx)
        if (savedEquipment.isNotEmpty()) {
            equipment = savedEquipment.joinToString(", ")
        }
    }

    // Refresh equipment when screen is resumed
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val savedEquipment = UserPreferences.getSelectedEquipment(ctx)
                if (savedEquipment.isNotEmpty()) {
                    equipment = savedEquipment.joinToString(", ")
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    // Dropdown states
    var goalExpanded by remember { mutableStateOf(false) }
    var equipmentExpanded by remember { mutableStateOf(false) }
    
    // Weekdays for picker
    val weekdays = listOf(
        "MONDAY" to "Mo",
        "TUESDAY" to "Di", 
        "WEDNESDAY" to "Mi",
        "THURSDAY" to "Do",
        "FRIDAY" to "Fr",
        "SATURDAY" to "Sa",
        "SUNDAY" to "So"
    )
    
    // Predefined options
    val goalOptions = listOf(
        "Muskelaufbau", 
        "Abnehmen", 
        "Kraft steigern", 
        "Ausdauer verbessern", 
        "Körper definieren", 
        "Allgemeine Fitness",
        "Funktionelle Fitness",
        "Beweglichkeit verbessern"
    )
    
    val equipmentOptions = listOf(
        "Nur Körpergewicht",
        "Hanteln, Klimmzugstange", 
        "Vollausstattung Fitnessstudio",
        "Heimstudio (Hanteln, Bänke)",
        "Kettlebells, Widerstandsbänder",
        "Cardio-Geräte (Laufband, Fahrrad)",
        "Functional Training (TRX, Medizinbälle)",
        "Crossfit Equipment"
    )

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("12-Wochen-Trainingsplan", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        // Goal Dropdown
        ExposedDropdownMenuBox(
            expanded = goalExpanded,
            onExpandedChange = { goalExpanded = !goalExpanded }
        ) {
            OutlinedTextField(
                value = goal,
                onValueChange = { },
                readOnly = true,
                label = { Text("Trainingsziel") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = goalExpanded,
                onDismissRequest = { goalExpanded = false }
            ) {
                goalOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            goal = option
                            goalExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        
        // Weekday selection
        Text("Trainingstage", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            weekdays.forEach { (dayKey, dayLabel) ->
                AssistChip(
                    onClick = {
                        selectedDays = if (selectedDays.contains(dayKey)) {
                            selectedDays - dayKey
                        } else {
                            selectedDays + dayKey
                        }
                    },
                    label = { Text(dayLabel) },
                    leadingIcon = if (selectedDays.contains(dayKey)) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null,
                    modifier = Modifier.weight(1f / 7f)
                )
            }
        }
        
        Text(
            text = "${selectedDays.size} Trainingstage pro Woche gewählt",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = minutes,
            onValueChange = { minutes = it },
            label = { Text("Minuten pro Session") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        // Equipment Dropdown
        ExposedDropdownMenuBox(
            expanded = equipmentExpanded,
            onExpandedChange = { equipmentExpanded = !equipmentExpanded }
        ) {
            OutlinedTextField(
                value = equipment,
                onValueChange = { },
                readOnly = true,
                label = { Text("Verfügbare Geräte") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipmentExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = equipmentExpanded,
                onDismissRequest = { equipmentExpanded = false }
            ) {
                equipmentOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            equipment = option
                            equipmentExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        
        // Equipment selection button
        OutlinedButton(
            onClick = {
                navController?.navigate("equipment")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Detaillierte Geräteauswahl")
        }
        Spacer(Modifier.height(16.dp))
        
        Button(
            enabled = !busy,
            onClick = {
                scope.launch {
                    busy = true
                    result = try {
                        // Debug: Check provider status
                        val providerStatus = AppAi.getProviderStatus(ctx)
                        println("Provider Status: $providerStatus")
                        
                        // Get the most current equipment selection from UserPreferences
                        val savedEquipment = UserPreferences.getSelectedEquipment(ctx)
                        val finalEquipment = if (savedEquipment.isNotEmpty()) {
                            savedEquipment
                        } else {
                            equipment.split(",").map { it.trim() }
                        }
                        
                        val req = PlanRequest(
                            goal = goal,
                            weeks = 12,
                            sessionsPerWeek = selectedDays.size,
                            minutesPerSession = minutes.toIntOrNull() ?: 60,
                            equipment = finalEquipment
                        )
                        val planContent = AppAi.planWithOptimalProvider(ctx, req).getOrThrow()
                        
                        // Save the plan to database
                        val planId = repo.savePlan(
                            title = "12-Wochen-Trainingsplan: $goal",
                            content = planContent,
                            goal = goal,
                            weeks = 12,
                            sessionsPerWeek = req.sessionsPerWeek,
                            minutesPerSession = req.minutesPerSession,
                            equipment = req.equipment,
                            trainingDays = selectedDays.toList()
                        )
                        saveStatus = "✓ Plan gespeichert (ID: $planId)"
                        
                        planContent
                    } catch (e: Exception) {
                        saveStatus = ""
                        "Fehler bei der Planerstellung:\n\n${e.message}\n\nProvider Status:\n${AppAi.getProviderStatus(ctx)}"
                    } finally {
                        busy = false
                    }
                }
            }
        ) {
            Text(if (busy) "Generiere..." else "Plan erstellen")
        }

        OutlinedButton(
            onClick = {
                goal = "Muskelaufbau"
                selectedDays = setOf("MONDAY", "WEDNESDAY", "FRIDAY")
                minutes = "60"
                equipment = ""
                result = ""
                saveStatus = ""
                UserPreferences.saveSelectedEquipment(ctx, emptyList())
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Zurücksetzen")
        }

        // Real Reset button that removes the plan from the app
        OutlinedButton(
            onClick = {
                scope.launch {
                    try {
                        repo.deleteAllPlans()
                        saveStatus = "✓ Alle Pläne gelöscht"
                        result = ""
                    } catch (e: Exception) {
                        saveStatus = "Fehler beim Löschen: ${e.message}"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Plan komplett löschen")
        }

        if (saveStatus.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(saveStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        
        if (result.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Card {
                Text(result, Modifier.padding(16.dp))
            }
        }
    }
}
