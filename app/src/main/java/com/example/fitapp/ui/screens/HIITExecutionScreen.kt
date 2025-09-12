package com.example.fitapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.domain.entities.HIITExercise
import com.example.fitapp.domain.entities.HIITWorkout
import kotlinx.coroutines.delay

enum class HIITPhase {
    READY,
    WORK,
    REST,
    COMPLETED,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HIITExecutionScreen(
    workout: HIITWorkout,
    onBackPressed: () -> Unit,
    onWorkoutCompleted: () -> Unit,
) {
    var currentRound by remember { mutableIntStateOf(1) }
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var currentPhase by remember { mutableStateOf(HIITPhase.READY) }
    var timeRemaining by remember { mutableIntStateOf(5) } // 5-second countdown to start
    var isPaused by remember { mutableStateOf(false) }
    var isWorkoutActive by remember { mutableStateOf(false) }

    val currentExercise =
        if (currentExerciseIndex < workout.exercises.size) {
            workout.exercises[currentExerciseIndex]
        } else {
            null
        }

    // Timer logic
    LaunchedEffect(isWorkoutActive, isPaused, timeRemaining, currentPhase) {
        if (isWorkoutActive && !isPaused && timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        } else if (timeRemaining == 0 && isWorkoutActive) {
            // Handle phase transitions
            when (currentPhase) {
                HIITPhase.READY -> {
                    currentPhase = HIITPhase.WORK
                    timeRemaining = workout.workInterval
                }
                HIITPhase.WORK -> {
                    currentPhase = HIITPhase.REST
                    timeRemaining = workout.restInterval
                }
                HIITPhase.REST -> {
                    // Move to next exercise or round
                    if (currentExerciseIndex < workout.exercises.size - 1) {
                        currentExerciseIndex++
                        currentPhase = HIITPhase.WORK
                        timeRemaining = workout.workInterval
                    } else if (currentRound < workout.rounds) {
                        // Next round
                        currentRound++
                        currentExerciseIndex = 0
                        currentPhase = HIITPhase.WORK
                        timeRemaining = workout.workInterval
                    } else {
                        // Workout completed
                        currentPhase = HIITPhase.COMPLETED
                        isWorkoutActive = false
                    }
                }
                HIITPhase.COMPLETED -> {
                    // Already completed
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(workout.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Runde $currentRound/${workout.rounds}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                if (isWorkoutActive) {
                    IconButton(onClick = { isPaused = !isPaused }) {
                        Icon(
                            if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (isPaused) "Fortsetzen" else "Pausieren",
                        )
                    }
                }
            },
        )

        when (currentPhase) {
            HIITPhase.READY -> {
                ReadyScreen(
                    timeRemaining = timeRemaining,
                    onStart = {
                        isWorkoutActive = true
                        currentPhase = HIITPhase.WORK
                        timeRemaining = workout.workInterval
                    },
                )
            }
            HIITPhase.WORK, HIITPhase.REST -> {
                currentExercise?.let { exercise ->
                    WorkoutActiveScreen(
                        exercise = exercise,
                        phase = currentPhase,
                        timeRemaining = timeRemaining,
                        totalTime = if (currentPhase == HIITPhase.WORK) workout.workInterval else workout.restInterval,
                        currentRound = currentRound,
                        totalRounds = workout.rounds,
                        exerciseIndex = currentExerciseIndex,
                        totalExercises = workout.exercises.size,
                        isPaused = isPaused,
                    )
                }
            }
            HIITPhase.COMPLETED -> {
                CompletedScreen(
                    workout = workout,
                    onFinish = onWorkoutCompleted,
                )
            }
        }
    }
}

@Composable
private fun ReadyScreen(
    timeRemaining: Int,
    onStart: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Bereit?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (timeRemaining > 0) {
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { timeRemaining / 5f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                )
                Text(
                    text = "$timeRemaining",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Workout startet in...")
        } else {
            Button(
                onClick = onStart,
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
            ) {
                Text("START", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WorkoutActiveScreen(
    exercise: HIITExercise,
    phase: HIITPhase,
    timeRemaining: Int,
    totalTime: Int,
    currentRound: Int,
    totalRounds: Int,
    exerciseIndex: Int,
    totalExercises: Int,
    isPaused: Boolean,
) {
    val isWorkPhase = phase == HIITPhase.WORK
    val phaseColor =
        if (isWorkPhase) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = {
                val totalWork = totalRounds * totalExercises
                if (totalWork > 0) {
                    (totalWork - (totalRounds - currentRound) * totalExercises - (totalExercises - exerciseIndex)) / totalWork.toFloat()
                } else {
                    0f
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Phase indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = phaseColor.copy(alpha = 0.1f)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (isWorkPhase) "ARBEITEN" else "PAUSE",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = phaseColor,
                )

                if (isPaused) {
                    Text(
                        text = "PAUSIERT",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Timer
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                progress = { (totalTime - timeRemaining) / totalTime.toFloat() },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                color = phaseColor,
            )
            Text(
                text = "$timeRemaining",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = phaseColor,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Exercise info (only during work phase)
        if (isWorkPhase) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = exercise.bodyweightExercise.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = exercise.bodyweightExercise.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )

                    if (exercise.targetReps != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ziel: ${exercise.targetReps} Wiederholungen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    // Instructions
                    if (exercise.bodyweightExercise.instructions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Anweisungen:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        exercise.bodyweightExercise.instructions.forEach { instruction ->
                            Text(
                                text = "• $instruction",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        } else {
            // Next exercise preview during rest
            val nextExerciseIndex = (exerciseIndex + 1) % totalExercises
            val nextExercise = exercise.bodyweightExercise // This would need to be passed from parent

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Nächste Übung:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = "Vorbereitung...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Round and exercise info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Runde", style = MaterialTheme.typography.bodySmall)
                Text(
                    "$currentRound / $totalRounds",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Übung", style = MaterialTheme.typography.bodySmall)
                Text(
                    "${exerciseIndex + 1} / $totalExercises",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun CompletedScreen(
    workout: HIITWorkout,
    onFinish: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = "Abgeschlossen",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Workout abgeschlossen!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = workout.name,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Gesamtzeit:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${workout.totalDuration / 60}min",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Runden:", style = MaterialTheme.typography.bodyMedium)
                    Text("${workout.rounds}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Übungen:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${workout.exercises.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Fertig")
        }
    }
}
