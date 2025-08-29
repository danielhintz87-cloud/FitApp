package com.example.fitapp.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AppAi
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.TodayWorkoutEntity
import com.example.fitapp.data.prefs.UserPreferences
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.services.PersonalAchievementManager
import com.example.fitapp.services.PersonalStreakManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyWorkoutScreen(
    goal: String,
    minutes: Int,
    onBackPressed: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val workoutDao = remember { AppDatabase.get(ctx).todayWorkoutDao() }
    val motivationRepo = remember { PersonalMotivationRepository(AppDatabase.get(ctx)) }
    val achievementManager = remember { PersonalAchievementManager(ctx, motivationRepo) }
    val streakManager = remember { PersonalStreakManager(ctx, motivationRepo) }
    
    var workoutSteps by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isCompleted by remember { mutableStateOf(false) }
    var keepScreenOn by remember { mutableStateOf(true) }
    var restTimer by remember { mutableStateOf(0) }
    var isResting by remember { mutableStateOf(false) }
    
    // Keep screen on functionality
    DisposableEffect(keepScreenOn) {
        val activity = ctx as? Activity
        if (keepScreenOn && activity != null) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    // Rest timer effect
    LaunchedEffect(isResting, restTimer) {
        if (isResting && restTimer > 0) {
            kotlinx.coroutines.delay(1000)
            restTimer--
        } else if (isResting && restTimer == 0) {
            isResting = false
        }
    }
    
    // Generate workout on first load
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val equipment = UserPreferences.getSelectedEquipment(ctx)
                val result = AppAi.generateDailyWorkoutSteps(ctx, goal, minutes, equipment)
                
                result.fold(
                    onSuccess = { content ->
                        // Parse content into steps (format: "Exercise | Description")
                        val steps = content.split("\n")
                            .filter { it.isNotBlank() && it.contains("|") }
                            .map { it.trim() }
                        
                        workoutSteps = steps
                        
                        // Save to database
                        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val workout = TodayWorkoutEntity(
                            dateIso = today,
                            content = content,
                            status = "pending"
                        )
                        workoutDao.upsert(workout)
                        
                        isLoading = false
                    },
                    onFailure = { e ->
                        error = e.message
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Heutiges Training")
                        if (!isLoading && workoutSteps.isNotEmpty()) {
                            Text(
                                "Schritt ${currentStepIndex + 1} von ${workoutSteps.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    // Screen on toggle
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = { keepScreenOn = it }
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Erstelle dein heutiges Training...")
                    }
                }
                
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Fehler beim Erstellen des Trainings:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            error ?: "Unbekannter Fehler",
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBackPressed) {
                            Text("Zurück")
                        }
                    }
                }
                
                isCompleted -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Training abgeschlossen!",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Gut gemacht! Du hast dein heutiges Training erfolgreich beendet.",
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBackPressed) {
                            Text("Fertig")
                        }
                    }
                }
                
                workoutSteps.isNotEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Progress bar
                        LinearProgressIndicator(
                            progress = (currentStepIndex + 1).toFloat() / workoutSteps.size,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        if (isResting) {
                            // Rest timer display
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Pause",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "$restTimer",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Sekunden")
                            }
                        } else {
                            // Current exercise display
                            val currentStep = workoutSteps[currentStepIndex]
                            val parts = currentStep.split("|").map { it.trim() }
                            val exerciseName = parts.getOrNull(0) ?: "Übung"
                            val exerciseDescription = parts.getOrNull(1) ?: "Keine Beschreibung"
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .weight(1f)
                            ) {
                                Text(
                                    exerciseName,
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(Modifier.height(16.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        exerciseDescription,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        // Control buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Back button
                            OutlinedButton(
                                onClick = {
                                    if (currentStepIndex > 0) {
                                        currentStepIndex--
                                        isResting = false
                                    }
                                },
                                enabled = currentStepIndex > 0 && !isResting,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Zurück")
                            }
                            
                            // Skip button
                            OutlinedButton(
                                onClick = {
                                    if (currentStepIndex < workoutSteps.size - 1) {
                                        currentStepIndex++
                                        isResting = false
                                    } else {
                                        // Complete workout
                                        scope.launch {
                                            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                            workoutDao.setStatus(
                                                today, 
                                                "completed", 
                                                System.currentTimeMillis() / 1000
                                            )
                                            
                                            // Track achievement and streak progress
                                            achievementManager.trackWorkoutCompletion()
                                            streakManager.trackWorkoutCompletion()
                                        }
                                        isCompleted = true
                                    }
                                },
                                enabled = !isResting,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Weiter")
                            }
                            
                            // Finish/Rest button
                            Button(
                                onClick = {
                                    val currentStep = workoutSteps[currentStepIndex]
                                    if (currentStep.contains("Pause", ignoreCase = true)) {
                                        // Extract rest time from description
                                        val description = currentStep.split("|").getOrNull(1) ?: ""
                                        val timeMatch = Regex("(\\d+)\\s*[sS]").find(description)
                                        val seconds = timeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 60
                                        
                                        restTimer = seconds
                                        isResting = true
                                    } else if (currentStepIndex < workoutSteps.size - 1) {
                                        currentStepIndex++
                                    } else {
                                        // Complete workout
                                        scope.launch {
                                            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                            workoutDao.setStatus(
                                                today, 
                                                "completed", 
                                                System.currentTimeMillis() / 1000
                                            )
                                            
                                            // Track achievement and streak progress
                                            achievementManager.trackWorkoutCompletion()
                                            streakManager.trackWorkoutCompletion()
                                        }
                                        isCompleted = true
                                    }
                                },
                                enabled = !isResting,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (currentStepIndex < workoutSteps.size - 1) "Erledigt" else "Abschließen"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}