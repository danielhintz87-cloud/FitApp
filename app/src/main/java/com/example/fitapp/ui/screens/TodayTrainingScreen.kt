package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.PlanRequest
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.data.prefs.UserPreferences
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTrainingScreen(
    navController: androidx.navigation.NavController? = null,
    onBackPressed: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    
    var currentPlan by remember { mutableStateOf<com.example.fitapp.data.db.PlanEntity?>(null) }
    var customGoal by remember { mutableStateOf("") }
    var customMinutes by remember { mutableStateOf("") }
    var customEquipment by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }
    
    val goalOptions = listOf(
        "Kraft heute",
        "Cardio heute", 
        "Leichtes Training",
        "Intensives Training",
        "Oberkörper Focus",
        "Unterkörper Focus",
        "Core Training",
        "Stretching/Mobility"
    )
    
    LaunchedEffect(Unit) {
        currentPlan = repo.getLatestPlan()
        currentPlan?.let { plan ->
            customGoal = "Angepasst: ${plan.goal}"
            customMinutes = plan.minutesPerSession.toString()
            // Load equipment from persistent storage instead of plan
            val savedEquipment = UserPreferences.getSelectedEquipment(ctx)
            customEquipment = if (savedEquipment.isNotEmpty()) {
                savedEquipment.joinToString(", ")
            } else {
                plan.equipment
            }
        }
    }
    
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Heutiges Training anpassen") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            currentPlan?.let { plan ->
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Aktueller Plan", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Titel: ${plan.title}", style = MaterialTheme.typography.bodyMedium)
                        Text("Ziel: ${plan.goal}", style = MaterialTheme.typography.bodySmall)
                        Text("Normal: ${plan.minutesPerSession} Min", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            Text("Training für heute anpassen", style = MaterialTheme.typography.titleLarge)
            Text(
                "Diese Anpassung beeinflusst nur das heutige Training, nicht den Gesamtplan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(16.dp))
            
            // Goal Dropdown
            ExposedDropdownMenuBox(
                expanded = goalExpanded,
                onExpandedChange = { goalExpanded = !goalExpanded }
            ) {
                OutlinedTextField(
                    value = customGoal,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Heutiges Trainingsziel") },
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
                                customGoal = option
                                goalExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            
            OutlinedTextField(
                value = customMinutes,
                onValueChange = { customMinutes = it },
                label = { Text("Trainingszeit (Minuten)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            
            OutlinedTextField(
                value = customEquipment,
                onValueChange = { customEquipment = it },
                label = { Text("Verfügbare Geräte heute") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            
            Button(
                enabled = !busy && customGoal.isNotBlank() && customMinutes.isNotBlank(),
                onClick = {
                    scope.launch {
                        busy = true
                        result = try {
                            val req = PlanRequest(
                                goal = customGoal,
                                weeks = 1, // Single day
                                sessionsPerWeek = 1,
                                minutesPerSession = customMinutes.toIntOrNull() ?: 30,
                                equipment = customEquipment.split(",").map { it.trim() }
                            )
                            
                            val planContent = AppAi.planWithOptimalProvider(ctx, req).getOrThrow()
                            
                            // Save as today's custom training (separate from main plan)
                            val today = LocalDate.now()
                            val dateIso = today.toString()
                            val workout = com.example.fitapp.data.db.TodayWorkoutEntity(
                                dateIso = dateIso,
                                content = planContent,
                                status = "pending",
                                planId = null // Custom training, not associated with main plan
                            )
                            repo.saveTodayWorkout(workout)
                            
                            planContent
                        } catch (e: Exception) {
                            "Fehler beim Erstellen des Trainings: ${e.message}"
                        } finally {
                            busy = false
                        }
                    }
                }
            ) {
                Text(if (busy) "Erstelle Training..." else "Heutiges Training erstellen")
            }
            
            // New button to start step-by-step daily workout
            Spacer(Modifier.height(8.dp))
            Button(
                enabled = !busy && customGoal.isNotBlank() && customMinutes.isNotBlank(),
                onClick = {
                    // Navigate to DailyWorkoutScreen with current parameters
                    val goal = customGoal.replace("Angepasst: ", "")
                    val minutes = customMinutes.toIntOrNull() ?: 60
                    navController?.navigate("daily_workout/$goal/$minutes")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Step-by-Step Training starten")
            }
            
            if (result.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Dein Training für heute", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(result, style = MaterialTheme.typography.bodyMedium)
                        
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val today = LocalDate.now()
                                            val dateIso = today.toString()
                                            
                                            // Save today's custom training
                                            val workout = com.example.fitapp.data.db.TodayWorkoutEntity(
                                                dateIso = dateIso,
                                                content = result,
                                                status = "completed",
                                                completedAt = System.currentTimeMillis() / 1000,
                                                planId = currentPlan?.id
                                            )
                                            repo.saveTodayWorkout(workout)
                                            
                                            // Trigger workout streak tracking
                                            val streakManager = com.example.fitapp.services.PersonalStreakManager(
                                                ctx, 
                                                com.example.fitapp.data.repo.PersonalMotivationRepository(AppDatabase.get(ctx))
                                            )
                                            streakManager.trackWorkoutCompletion(today)
                                            
                                            onBackPressed()
                                        } catch (e: Exception) {
                                            android.util.Log.e("TodayTrainingScreen", "Error saving training completion", e)
                                            onBackPressed() // Still go back even if there's an error
                                        }
                                    }
                                }
                            ) {
                                Text("Training abgeschlossen")
                            }
                            OutlinedButton(onClick = { result = "" }) {
                                Text("Neues Training erstellen")
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(96.dp))
        }
    }
}