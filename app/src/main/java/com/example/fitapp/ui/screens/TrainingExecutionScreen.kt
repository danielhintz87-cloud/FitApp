package com.example.fitapp.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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

data class ExerciseStep(
    val name: String,
    val type: String, // "reps", "time", "distance"
    val value: String, // "10", "30 sec", "2 km"
    val description: String = "",
    val restTime: Int = 0, // in seconds
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingExecutionScreen(
    planId: Long,
    onBackPressed: () -> Unit,
    onTrainingCompleted: () -> Unit,
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }

    var plan by remember { mutableStateOf<PlanEntity?>(null) }
    var exercises by remember { mutableStateOf<List<ExerciseStep>>(emptyList()) }
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var isInTraining by remember { mutableStateOf(false) }
    var showTrainingOverview by remember { mutableStateOf(true) }
    var completedExercises by remember { mutableStateOf(setOf<Int>()) }
    var isResting by remember { mutableStateOf(false) }
    var restTimeRemaining by remember { mutableIntStateOf(0) }
    var guidedMode by remember { mutableStateOf(false) }
    var keepScreenOn by remember { mutableStateOf(false) }

    // Keep screen on during training
    DisposableEffect(keepScreenOn || isInTraining) {
        val activity = ctx as? Activity
        val shouldKeepOn = keepScreenOn || isInTraining
        if (shouldKeepOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                Row {
                    IconButton(onClick = { keepScreenOn = !keepScreenOn }) {
                        Icon(
                            if (keepScreenOn) Icons.Filled.ScreenLockPortrait else Icons.Filled.ScreenLockRotation,
                            contentDescription = "Display an/aus",
                            tint = if (keepScreenOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = { guidedMode = !guidedMode }) {
                        Icon(
                            if (guidedMode) Icons.Filled.School else Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = "Geführter Modus",
                            tint = if (guidedMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
        )

        if (showTrainingOverview) {
            // Training Overview
            TrainingOverview(
                exercises = exercises,
                completedExercises = completedExercises,
                onStartTraining = {
                    showTrainingOverview = false
                    isInTraining = true
                },
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
                },
            )
        }
    }
}

@Composable
private fun TrainingOverview(
    exercises: List<ExerciseStep>,
    completedExercises: Set<Int>,
    onStartTraining: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        // Progress indicator
        val progress = if (exercises.isNotEmpty()) completedExercises.size.toFloat() / exercises.size else 0f

        Card {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Trainingsfortschritt", style = MaterialTheme.typography.titleMedium)
                    Text("${completedExercises.size}/${exercises.size}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Gesamtdauer: ca. ${calculateTotalDuration(exercises)} Min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Exercise List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(exercises.indices.toList()) { index ->
                ExerciseOverviewCard(
                    exercise = exercises[index],
                    index = index,
                    isCompleted = index in completedExercises,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Start/Continue Training Button
        Button(
            onClick = onStartTraining,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Icon(
                if (completedExercises.isNotEmpty()) Icons.Filled.PlayArrow else Icons.Filled.PlayArrow,
                contentDescription = null,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (completedExercises.isNotEmpty()) "Training fortsetzen" else "Training starten",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun ExerciseOverviewCard(
    exercise: ExerciseStep,
    index: Int,
    isCompleted: Boolean,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isCompleted) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Circle,
                contentDescription = null,
                tint =
                    if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${index + 1}. ${exercise.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    exercise.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (exercise.description.isNotBlank()) {
                    Text(
                        exercise.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                if (exercise.restTime > 0) {
                    Text(
                        "Pause: ${exercise.restTime}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
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
    onPreviousExercise: (Int) -> Unit,
) {
    val currentExercise = exercises.getOrNull(currentIndex)

    // Cardio timer state
    var cardioTimeRemaining by remember(currentIndex) { mutableIntStateOf(0) }
    var cardioTimerRunning by remember(currentIndex) { mutableStateOf(false) }

    // Initialize cardio timer for cardio exercises
    LaunchedEffect(currentExercise) {
        if (currentExercise?.type == "cardio") {
            // Extract time from cardio exercise value (e.g., "5 km/h für 10 Min" -> 10 minutes)
            val timeMatch = Regex("(\\d+)\\s*[mM]in", RegexOption.IGNORE_CASE).find(currentExercise.value)
            val minutes = timeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 5
            cardioTimeRemaining = minutes * 60 // Convert to seconds
        }
    }

    // Cardio timer countdown
    LaunchedEffect(cardioTimerRunning, cardioTimeRemaining) {
        if (cardioTimerRunning && cardioTimeRemaining > 0) {
            delay(1000)
            cardioTimeRemaining--
        } else if (cardioTimerRunning && cardioTimeRemaining <= 0) {
            cardioTimerRunning = false
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        // Progress
        LinearProgressIndicator(
            progress = { if (exercises.isNotEmpty()) completedExercises.size.toFloat() / exercises.size else 0f },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        if (isResting) {
            // Rest Mode
            RestModeDisplay(
                restTimeRemaining = restTimeRemaining,
                nextExercise = exercises.getOrNull(currentIndex)?.name ?: "",
            )
        } else {
            currentExercise?.let { exercise ->
                // Current Exercise Display
                CurrentExerciseDisplay(
                    exercise = exercise,
                    exerciseNumber = currentIndex + 1,
                    totalExercises = exercises.size,
                    guidedMode = guidedMode,
                    cardioTimeRemaining = cardioTimeRemaining,
                    cardioTimerRunning = cardioTimerRunning,
                    onStartCardioTimer = { cardioTimerRunning = true },
                    onStopCardioTimer = { cardioTimerRunning = false },
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { onPreviousExercise(currentIndex) },
                enabled = currentIndex > 0 && !isResting,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Zurück")
            }

            if (!isResting) {
                OutlinedButton(
                    onClick = { onSkipExercise(currentIndex) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Überspringen")
                }

                Button(
                    onClick = { onCompleteExercise(currentIndex) },
                    modifier = Modifier.weight(1f),
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
    nextExercise: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "💤 Pause",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "$restTimeRemaining",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text("Sekunden", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Text(
                "Nächste Übung:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                nextExercise,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun CurrentExerciseDisplay(
    exercise: ExerciseStep,
    exerciseNumber: Int,
    totalExercises: Int,
    guidedMode: Boolean,
    cardioTimeRemaining: Int = 0,
    cardioTimerRunning: Boolean = false,
    onStartCardioTimer: () -> Unit = {},
    onStopCardioTimer: () -> Unit = {},
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp)) {
            Text(
                "Übung $exerciseNumber von $totalExercises",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                exercise.value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            // Cardio Timer Display
            if (exercise.type == "cardio") {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (cardioTimerRunning) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                },
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "⏱️ Cardio Timer",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${cardioTimeRemaining / 60}:${(cardioTimeRemaining % 60).toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = if (cardioTimerRunning) onStopCardioTimer else onStartCardioTimer,
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (cardioTimerRunning) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                    ),
                            ) {
                                Icon(
                                    if (cardioTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (cardioTimerRunning) "Pause" else "Start")
                            }
                        }
                    }
                }

                // Enhanced cardio instructions
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "🏃 Cardio-Anleitung",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(Modifier.height(4.dp))
                        val cardioInstructions = getCardioInstructions(exercise.name, exercise.value)
                        Text(
                            cardioInstructions,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (exercise.description.isNotBlank() && guidedMode && exercise.type != "cardio") {
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "💡 Anleitung",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            exercise.description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (exercise.restTime > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Danach ${exercise.restTime} Sekunden Pause",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
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
                    exercises.add(
                        ExerciseStep(
                            name = parts[1],
                            type = "reps",
                            value = parts[0],
                            description = "Führe ${parts[0]} aus",
                            restTime = 30,
                        ),
                    )
                }
            }
            // Match patterns like "30 Sekunden Plank" or "2 Minuten Laufen"
            trimmed.matches(Regex("\\d+\\s+(Sekunden?|Minuten?)\\s+.*", RegexOption.IGNORE_CASE)) -> {
                val parts = trimmed.split("\\s+".toRegex(), 3)
                if (parts.size >= 3) {
                    exercises.add(
                        ExerciseStep(
                            name = parts.drop(2).joinToString(" "),
                            type = "time",
                            value = "${parts[0]} ${parts[1]}",
                            description = "Halte für ${parts[0]} ${parts[1]}",
                            restTime = 15,
                        ),
                    )
                }
            }
            // Match patterns like "Laufband: 5 km/h für 10 Min"
            trimmed.contains("km/h", ignoreCase = true) || trimmed.contains("stufe", ignoreCase = true) -> {
                exercises.add(
                    ExerciseStep(
                        name = trimmed.substringBefore(":").trim(),
                        type = "cardio",
                        value = trimmed.substringAfter(":").trim(),
                        description = "Cardio-Training wie angegeben",
                        restTime = 60,
                    ),
                )
            }
            // General exercise lines
            trimmed.isNotBlank() && !trimmed.startsWith("#") && trimmed.length > 5 -> {
                exercises.add(
                    ExerciseStep(
                        name = trimmed,
                        type = "general",
                        value = "Nach Anweisung",
                        description = "Führe die Übung wie beschrieben aus",
                        restTime = 30,
                    ),
                )
            }
        }
    }

    if (exercises.isEmpty()) {
        exercises.add(
            ExerciseStep(
                name = "Freies Training",
                type = "general",
                value = "Nach Plan",
                description = "Folge dem Trainingsplan",
                restTime = 0,
            ),
        )
    }

    return exercises
}

private fun calculateTotalDuration(exercises: List<ExerciseStep>): Int {
    var totalMinutes = 0
    exercises.forEach { exercise ->
        // Estimate exercise duration
        totalMinutes +=
            when (exercise.type) {
                "reps" -> 2 // 2 minutes per rep exercise
                "time" -> {
                    val timeStr = exercise.value.lowercase(java.util.Locale.ROOT)
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

private fun getCardioInstructions(
    exerciseName: String,
    exerciseValue: String,
): String {
    val lowercaseName = exerciseName.lowercase(java.util.Locale.ROOT)
    val lowercaseValue = exerciseValue.lowercase(java.util.Locale.ROOT)

    return when {
        lowercaseName.contains("laufband") || lowercaseName.contains("treadmill") -> {
            val speedMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*km/h").find(lowercaseValue)
            val speed = speedMatch?.groupValues?.get(1) ?: "5"
            "🏃‍♂️ Laufband:\n• Geschwindigkeit: $speed km/h\n• Steigung: 0-2% (leichte Steigung)\n• Gleichmäßiges Tempo halten\n• Bei Atemnot Geschwindigkeit reduzieren"
        }
        lowercaseName.contains("rudern") || lowercaseName.contains("rowing") -> {
            "🚣‍♂️ Rudergerät:\n• Mittlere Intensität (60-70% Herzfrequenz)\n• Gleichmäßige Züge\n• Rücken gerade halten\n• Knie nicht vollständig durchstrecken"
        }
        lowercaseName.contains("fahrrad") || lowercaseName.contains("bike") || lowercaseName.contains("ergometer") -> {
            val levelMatch = Regex("stufe\\s*(\\d+)").find(lowercaseValue)
            val level = levelMatch?.groupValues?.get(1) ?: "5-8"
            "🚴‍♂️ Ergometer:\n• Widerstandsstufe: ${level}\n• Sitzhöhe korrekt einstellen\n• Gleichmäßig treten\n• Oberkörper aufrecht"
        }
        lowercaseName.contains("elliptical") || lowercaseName.contains("crosstrainer") -> {
            "🏃‍♀️ Crosstrainer:\n• Mittlere Intensität\n• Arme aktiv mitbewegen\n• Aufrechte Haltung\n• Gleichmäßiger Rhythmus"
        }
        lowercaseName.contains("stepper") -> {
            "👟 Stepper:\n• Kontrollierte Bewegungen\n• Ganzen Fuß aufsetzen\n• Knie nicht vollständig durchstrecken\n• Handlauf nur zur Balance nutzen"
        }
        else -> {
            "🏃‍♂️ Cardio-Training:\n• Gleichmäßige Intensität beibehalten\n• Atmung kontrollieren\n• Bei zu hoher Belastung Tempo reduzieren\n• Ausreichend trinken"
        }
    }
}
