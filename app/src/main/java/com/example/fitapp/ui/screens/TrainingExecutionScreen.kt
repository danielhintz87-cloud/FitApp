package com.example.fitapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.PlanEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ExerciseStep(
    val name: String,
    val type: String, // "reps", "time", "distance"
    val value: String, // "10", "30 sec", "2 km"
    val description: String = "",
    val restTime: Int = 0 // in seconds
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingExecutionScreen(
    planId: Long,
    onBackPressed: () -> Unit,
    onTrainingCompleted: () -> Unit
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val scope = rememberCoroutineScope()
    
    var plan by remember { mutableStateOf<PlanEntity?>(null) }
    var exercises by remember { mutableStateOf<List<ExerciseStep>>(emptyList()) }
    var currentExerciseIndex by remember { mutableStateOf(0) }
    var isInTraining by remember { mutableStateOf(false) }
    var showTrainingOverview by remember { mutableStateOf(true) }
    var completedExercises by remember { mutableStateOf(setOf<Int>()) }
    var isResting by remember { mutableStateOf(false) }
    var restTimeRemaining by remember { mutableStateOf(0) }
    var guidedMode by remember { mutableStateOf(false) }
    
    // Timer for rest periods
    LaunchedEffect(isResting, restTimeRemaining) {
        if (isResting && restTimeRemaining > 0) {
            delay(1000)
            restTimeRemaining--
        } else if (isResting && restTimeRemaining <= 0) {
            isResting = false
        }
    }
    
    // Load plan and parse exercises
    LaunchedEffect(planId) {
        plan = db.planDao().getPlan(planId)
        plan?.let { p ->
            exercises = parseTrainingContent(p.content)
        }
    }
    
    Column(Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { 
                Column {
                    Text("Training", style = MaterialTheme.typography.titleMedium)
                    plan?.let { Text(it.title, style = MaterialTheme.typography.bodyMedium) }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { guidedMode = !guidedMode }) {
                    Icon(
                        if (guidedMode) Icons.Filled.School else Icons.Filled.DirectionsRun,
                        contentDescription = "Geführter Modus",
                        tint = if (guidedMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
        
        if (showTrainingOverview) {
            // Training Overview
            TrainingOverview(
                exercises = exercises,
                completedExercises = completedExercises,
                onStartTraining = { 
                    showTrainingOverview = false
                    isInTraining = true
                }
            )
        } else if (isInTraining) {
            // Active Training Mode
            ActiveTrainingMode(
                exercises = exercises,
                currentIndex = currentExerciseIndex,
                completedExercises = completedExercises,
                isResting = isResting,
                restTimeRemaining = restTimeRemaining,
                guidedMode = guidedMode,
                onCompleteExercise = { index ->
                    completedExercises = completedExercises + index
                    if (index < exercises.size - 1) {
                        val exercise = exercises[index]
                        if (exercise.restTime > 0) {
                            isResting = true
                            restTimeRemaining = exercise.restTime
                        }
                        currentExerciseIndex = index + 1
                    } else {
                        // Training completed
                        isInTraining = false
                        onTrainingCompleted()
                    }
                },
                onSkipExercise = { index ->
                    if (index < exercises.size - 1) {
                        currentExerciseIndex = index + 1
                    } else {
                        isInTraining = false
                        onTrainingCompleted()
                    }
                },
                onPreviousExercise = { index ->
                    if (index > 0) {
                        currentExerciseIndex = index - 1
                        completedExercises = completedExercises - index
                        isResting = false
                        restTimeRemaining = 0
                    }
                }
            )
        }
    }
}

@Composable
private fun TrainingOverview(
    exercises: List<ExerciseStep>,
    completedExercises: Set<Int>,
    onStartTraining: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator
        val progress = if (exercises.isNotEmpty()) completedExercises.size.toFloat() / exercises.size else 0f
        
        Card {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trainingsfortschritt", style = MaterialTheme.typography.titleMedium)
                    Text("${completedExercises.size}/${exercises.size}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Gesamtdauer: ca. ${calculateTotalDuration(exercises)} Min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Exercise List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exercises.indices.toList()) { index ->
                ExerciseOverviewCard(
                    exercise = exercises[index],
                    index = index,
                    isCompleted = index in completedExercises
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Start Training Button
        Button(
            onClick = onStartTraining,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Training starten")
        }
    }
}

@Composable
private fun ExerciseOverviewCard(
    exercise: ExerciseStep,
    index: Int,
    isCompleted: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Circle,
                contentDescription = null,
                tint = if (isCompleted) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${index + 1}. ${exercise.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    exercise.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (exercise.description.isNotBlank()) {
                    Text(
                        exercise.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (exercise.restTime > 0) {
                    Text(
                        "Pause: ${exercise.restTime}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTrainingMode(
    exercises: List<ExerciseStep>,
    currentIndex: Int,
    completedExercises: Set<Int>,
    isResting: Boolean,
    restTimeRemaining: Int,
    guidedMode: Boolean,
    onCompleteExercise: (Int) -> Unit,
    onSkipExercise: (Int) -> Unit,
    onPreviousExercise: (Int) -> Unit
) {
    val currentExercise = exercises.getOrNull(currentIndex)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress
        LinearProgressIndicator(
            progress = if (exercises.isNotEmpty()) completedExercises.size.toFloat() / exercises.size else 0f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(16.dp))
        
        if (isResting) {
            // Rest Mode
            RestModeDisplay(
                restTimeRemaining = restTimeRemaining,
                nextExercise = exercises.getOrNull(currentIndex)?.name ?: ""
            )
        } else {
            currentExercise?.let { exercise ->
                // Current Exercise Display
                CurrentExerciseDisplay(
                    exercise = exercise,
                    exerciseNumber = currentIndex + 1,
                    totalExercises = exercises.size,
                    guidedMode = guidedMode
                )
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onPreviousExercise(currentIndex) },
                enabled = currentIndex > 0 && !isResting,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Zurück")
            }
            
            if (!isResting) {
                OutlinedButton(
                    onClick = { onSkipExercise(currentIndex) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Überspringen")
                }
                
                Button(
                    onClick = { onCompleteExercise(currentIndex) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Fertig")
                }
            }
        }
    }
}

@Composable
private fun RestModeDisplay(
    restTimeRemaining: Int,
    nextExercise: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "💤 Pause",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "$restTimeRemaining",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Sekunden", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Text(
                "Nächste Übung:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                nextExercise,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun CurrentExerciseDisplay(
    exercise: ExerciseStep,
    exerciseNumber: Int,
    totalExercises: Int,
    guidedMode: Boolean
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp)) {
            Text(
                "Übung $exerciseNumber von $totalExercises",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))
            Text(
                exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                exercise.value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (exercise.description.isNotBlank() && guidedMode) {
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "💡 Anleitung",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            exercise.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            if (exercise.restTime > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Danach ${exercise.restTime} Sekunden Pause",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private fun parseTrainingContent(content: String): List<ExerciseStep> {
    val exercises = mutableListOf<ExerciseStep>()
    val lines = content.lines()
    
    for (line in lines) {
        val trimmed = line.trim()
        when {
            // Match patterns like "3x10 Push-ups" or "10x Squats"
            trimmed.matches(Regex("\\d+x\\d+\\s+.*", RegexOption.IGNORE_CASE)) -> {
                val parts = trimmed.split("\\s+".toRegex(), 2)
                if (parts.size >= 2) {
                    exercises.add(ExerciseStep(
                        name = parts[1],
                        type = "reps",
                        value = parts[0],
                        description = "Führe ${parts[0]} aus",
                        restTime = 30
                    ))
                }
            }
            // Match patterns like "30 Sekunden Plank" or "2 Minuten Laufen"
            trimmed.matches(Regex("\\d+\\s+(Sekunden?|Minuten?)\\s+.*", RegexOption.IGNORE_CASE)) -> {
                val parts = trimmed.split("\\s+".toRegex(), 3)
                if (parts.size >= 3) {
                    exercises.add(ExerciseStep(
                        name = parts.drop(2).joinToString(" "),
                        type = "time",
                        value = "${parts[0]} ${parts[1]}",
                        description = "Halte für ${parts[0]} ${parts[1]}",
                        restTime = 15
                    ))
                }
            }
            // Match patterns like "Laufband: 5 km/h für 10 Min"
            trimmed.contains("km/h", ignoreCase = true) || trimmed.contains("stufe", ignoreCase = true) -> {
                exercises.add(ExerciseStep(
                    name = trimmed.substringBefore(":").trim(),
                    type = "cardio",
                    value = trimmed.substringAfter(":").trim(),
                    description = "Cardio-Training wie angegeben",
                    restTime = 60
                ))
            }
            // General exercise lines
            trimmed.isNotBlank() && !trimmed.startsWith("#") && trimmed.length > 5 -> {
                exercises.add(ExerciseStep(
                    name = trimmed,
                    type = "general",
                    value = "Nach Anweisung",
                    description = "Führe die Übung wie beschrieben aus",
                    restTime = 30
                ))
            }
        }
    }
    
    if (exercises.isEmpty()) {
        exercises.add(ExerciseStep(
            name = "Freies Training",
            type = "general",
            value = "Nach Plan",
            description = "Folge dem Trainingsplan",
            restTime = 0
        ))
    }
    
    return exercises
}

private fun calculateTotalDuration(exercises: List<ExerciseStep>): Int {
    var totalMinutes = 0
    exercises.forEach { exercise ->
        // Estimate exercise duration
        totalMinutes += when (exercise.type) {
            "reps" -> 2 // 2 minutes per rep exercise
            "time" -> {
                val timeStr = exercise.value.lowercase()
                when {
                    timeStr.contains("minute") -> timeStr.filter { it.isDigit() }.toIntOrNull() ?: 3
                    timeStr.contains("sekunde") -> (timeStr.filter { it.isDigit() }.toIntOrNull() ?: 30) / 60
                    else -> 3
                }
            }
            "cardio" -> 10 // 10 minutes for cardio
            else -> 3 // 3 minutes for general exercises
        }
        // Add rest time
        totalMinutes += exercise.restTime / 60
    }
    return totalMinutes
}