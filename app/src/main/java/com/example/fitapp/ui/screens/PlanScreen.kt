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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.PlanRequest
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.UserPreferences
import com.example.fitapp.data.prefs.UserPreferencesLegacy
import com.example.fitapp.data.repo.NutritionRepository
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.fitapp.util.rememberDebouncedState
import com.example.fitapp.util.rememberSafeCoroutineScope
import com.example.fitapp.util.rememberStable
import com.example.fitapp.util.rememberAutoClearing

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun PlanScreen(contentPadding: PaddingValues, navController: NavController? = null) {
    val ctx = LocalContext.current
    val scope = rememberSafeCoroutineScope()
    
    // Optimize expensive repository creation
    val repo = rememberStable(ctx) { NutritionRepository(AppDatabase.get(ctx)) }
    
    // Optimized state management
    var goal by remember { mutableStateOf("Muskelaufbau") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    val (minutes, updateMinutes) = rememberDebouncedState("60", 500L)
    var equipment by remember { mutableStateOf("Hanteln, Klimmzugstange") }
    var result by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var saveStatus by remember { mutableStateOf("") }
    
    // Stable callback for day selection to prevent recomposition
    val onDayToggle = remember {
        { dayKey: String ->
            selectedDays = if (selectedDays.contains(dayKey)) {
                selectedDays - dayKey
            } else {
                selectedDays + dayKey
            }
        }
    }
    
    // Load saved equipment initially
    LaunchedEffect(Unit) {
        val savedEquipment = UserPreferencesLegacy.getSelectedEquipment(ctx)
        if (savedEquipment.isNotEmpty()) {
            equipment = savedEquipment.joinToString(", ")
        }
    }

    // Refresh equipment when screen is resumed
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val savedEquipment = UserPreferencesLegacy.getSelectedEquipment(ctx)
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
    
    // Weekdays for picker - using more descriptive labels
    val weekdays = listOf(
        "MONDAY" to "Montag",
        "TUESDAY" to "Dienstag", 
        "WEDNESDAY" to "Mittwoch",
        "THURSDAY" to "Donnerstag",
        "FRIDAY" to "Freitag",
        "SATURDAY" to "Samstag",
        "SUNDAY" to "Sonntag"
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
        Text(
            "Erstellen Sie Ihren personalisierten Trainingsplan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(24.dp))
        
        // Weight Loss Integration Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Personalized Weight Loss Program",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Combine your training plan with a personalized weight loss program for optimal results.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            navController?.navigate("bmi_calculator")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Calculate BMI")
                    }
                    Button(
                        onClick = {
                            navController?.navigate("weight_loss_program")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Weight Program")
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
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
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
        Spacer(Modifier.height(24.dp))
        
        // Weekday selection
        Text("Trainingstage", style = MaterialTheme.typography.titleMedium)
        Text(
            "Wählen Sie die Tage aus, an denen Sie trainieren möchten",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(Modifier.height(12.dp))
        
        // Use FlowRow-like layout for better responsiveness
        Column(modifier = Modifier.fillMaxWidth()) {
            // First row: Monday to Thursday
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                weekdays.take(4).forEach { (dayKey, dayLabel) ->
                    FilterChip(
                        onClick = { onDayToggle(dayKey) },
                        label = { 
                            Text(
                                dayLabel,
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        selected = selectedDays.contains(dayKey),
                        leadingIcon = if (selectedDays.contains(dayKey)) {
                            { 
                                Icon(
                                    Icons.Default.Check, 
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                ) 
                            }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Second row: Friday to Sunday
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                weekdays.takeLast(3).forEach { (dayKey, dayLabel) ->
                    FilterChip(
                        onClick = { onDayToggle(dayKey) },
                        label = { 
                            Text(
                                dayLabel,
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        selected = selectedDays.contains(dayKey),
                        leadingIcon = if (selectedDays.contains(dayKey)) {
                            { 
                                Icon(
                                    Icons.Default.Check, 
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                ) 
                            }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Add spacer to balance the row
                Spacer(Modifier.weight(1f))
            }
        }
        
        Text(
            text = if (selectedDays.isEmpty()) {
                "Keine Trainingstage ausgewählt"
            } else {
                "${selectedDays.size} Trainingstage pro Woche gewählt"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (selectedDays.isEmpty()) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = minutes,
            onValueChange = updateMinutes,
            label = { Text("Minuten pro Session") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
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
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
            enabled = !busy && selectedDays.isNotEmpty() && minutes.toIntOrNull() != null && minutes.toInt() > 0,
            onClick = {
                scope.launch {
                    busy = true
                    saveStatus = ""
                    result = try {
                        // Comprehensive input validation
                        when {
                            selectedDays.isEmpty() -> {
                                "❌ Bitte wählen Sie mindestens einen Trainingstag aus."
                            }
                            minutes.toIntOrNull() == null -> {
                                "❌ Bitte geben Sie eine gültige Zahl für die Trainingszeit ein."
                            }
                            minutes.toInt() <= 0 -> {
                                "❌ Trainingszeit muss mindestens 1 Minute sein."
                            }
                            minutes.toInt() > 300 -> {
                                "❌ Trainingszeit sollte höchstens 300 Minuten sein."
                            }
                            goal.isBlank() -> {
                                "❌ Bitte wählen Sie ein Trainingsziel aus."
                            }
                            else -> {
                                    val providerStatus = AppAi.getProviderStatus(ctx)
                                    if (!providerStatus.contains("✓")) {
                                        result = "❌ Keine verfügbaren AI-Provider. Bitte prüfen Sie Ihre API-Schlüssel in den Einstellungen."
                                        return@launch
                                    }
                                
                                // Get the most current equipment selection from UserPreferences
                                val savedEquipment = try {
                                    UserPreferencesLegacy.getSelectedEquipment(ctx)
                                } catch (e: Exception) {
                                    android.util.Log.w("PlanScreen", "Failed to load equipment preferences", e)
                                    emptyList()
                                }
                                
                                val finalEquipment = if (savedEquipment.isNotEmpty()) {
                                    savedEquipment
                                } else {
                                    equipment.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                }
                                
                                val req = PlanRequest(
                                    goal = goal.trim(),
                                    weeks = 12,
                                    sessionsPerWeek = selectedDays.size,
                                    minutesPerSession = minutes.toInt(),
                                    equipment = finalEquipment
                                )
                                
                                // Generate plan with comprehensive error handling
                                val planResult = AppAi.planWithOptimalProvider(ctx, req)
                                
                                if (planResult.isFailure) {
                                    val error = planResult.exceptionOrNull()
                                    when {
                                        error?.message?.contains("API") == true -> 
                                            "❌ API-Fehler: ${error.message}\n\nBitte prüfen Sie Ihre Internetverbindung und API-Schlüssel."
                                        error?.message?.contains("timeout") == true -> 
                                            "❌ Zeitüberschreitung. Bitte versuchen Sie es erneut."
                                        else -> 
                                            "❌ Fehler bei der Planerstellung: ${error?.message ?: "Unbekannter Fehler"}"
                                    }
                                } else {
                                    val planContent = planResult.getOrThrow()
                                    
                                    // Validate generated content
                                    if (planContent.isBlank()) {
                                        "❌ Leerer Plan erhalten. Bitte versuchen Sie es erneut."
                                    } else if (planContent.length < 100) {
                                        "❌ Plan scheint unvollständig zu sein. Bitte versuchen Sie es erneut."
                                    } else {
                                        // Save the plan to database with error handling
                                        try {
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
                                            saveStatus = "✅ Plan erfolgreich gespeichert (ID: $planId)"
                                            planContent
                                        } catch (e: Exception) {
                                            android.util.Log.e("PlanScreen", "Failed to save plan", e)
                                            saveStatus = "⚠️ Plan erstellt, aber Speichern fehlgeschlagen: ${e.message}"
                                            planContent
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PlanScreen", "Unexpected error in plan generation", e)
                        saveStatus = ""
                        "❌ Unerwarteter Fehler: ${e.message}\n\nProvider Status:\n${AppAi.getProviderStatus(ctx)}"
                    } finally {
                        busy = false
                    }
                }
            }
        ) {
            if (busy) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text("Generiere Plan...")
                }
            } else {
                Text(
                    when {
                        selectedDays.isEmpty() -> "Bitte Trainingstage wählen"
                        minutes.toIntOrNull() == null || minutes.toInt() <= 0 -> "Ungültige Trainingszeit"
                        else -> "Plan erstellen"
                    }
                )
            }
        }

        OutlinedButton(
            onClick = {
                goal = "Muskelaufbau"
                selectedDays = setOf() // Clear selected days
                updateMinutes("60") // Use the debounced updater
                equipment = ""
                result = ""
                saveStatus = ""
                UserPreferencesLegacy.saveSelectedEquipment(ctx, emptyList())
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
