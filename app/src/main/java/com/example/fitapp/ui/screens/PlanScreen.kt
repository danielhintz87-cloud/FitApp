package com.example.fitapp.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.PlanRequest
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.UserPreferences
import com.example.fitapp.data.repo.NutritionRepository
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun PlanScreen(contentPadding: PaddingValues, navController: NavController? = null) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    var goal by remember { mutableStateOf("Muskelaufbau") }
    var sessions by remember { mutableStateOf("3") }
    var minutes by remember { mutableStateOf("60") }
    var equipment by remember { mutableStateOf("Hanteln, Klimmzugstange") }
    var result by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var saveStatus by remember { mutableStateOf("") }
    
    // Load saved equipment from UserPreferences on initialization
    LaunchedEffect(Unit) {
        val savedEquipment = UserPreferences.getSelectedEquipment(ctx)
        if (savedEquipment.isNotEmpty()) {
            equipment = savedEquipment.joinToString(", ")
        }
    }
    
    // Check for equipment changes when returning from equipment selection
    navController?.currentBackStackEntry?.savedStateHandle?.get<String>("equipment")?.let { newEquipment ->
        if (newEquipment.isNotBlank() && newEquipment != equipment) {
            equipment = newEquipment
        }
    }
    
    // Dropdown states
    var goalExpanded by remember { mutableStateOf(false) }
    var equipmentExpanded by remember { mutableStateOf(false) }
    
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
        
        OutlinedTextField(
            value = sessions,
            onValueChange = { sessions = it },
            label = { Text("Sessions pro Woche") },
            modifier = Modifier.fillMaxWidth()
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
                navController?.let { nav ->
                    nav.currentBackStackEntry?.savedStateHandle?.set("equipment", equipment)
                    nav.navigate("equipment")
                }
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
                            sessionsPerWeek = sessions.toIntOrNull() ?: 3,
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
                            equipment = req.equipment
                        )
                        saveStatus = "✓ Plan gespeichert (ID: $planId)"
                        
                        planContent
                    } catch (e: Exception) {
                        saveStatus = ""
                        "Fehler: ${e.message}"
                    } finally {
                        busy = false
                    }
                }
            }
        ) {
            Text(if (busy) "Generiere..." else "Plan erstellen")
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
