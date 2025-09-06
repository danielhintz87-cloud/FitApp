package com.example.fitapp.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.PlanEntity
import com.example.fitapp.services.WorkoutExecutionManager
import com.example.fitapp.services.SmartRestTimer
import com.example.fitapp.services.RestTimerState
import com.example.fitapp.ui.components.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Enhanced Training Execution Screen
 * Implements the comprehensive workout execution system as specified
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTrainingExecutionScreen(
    planId: Long,
    onBackPressed: () -> Unit,
    onTrainingCompleted: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()
    
    // Core state
    var plan by remember { mutableStateOf<PlanEntity?>(null) }
    var exercises by remember { mutableStateOf<List<ExerciseStep>>(emptyList()) }
    
    // Workout execution state
    val smartRestTimer = remember { SmartRestTimer(context) }
    val workoutManager = remember { WorkoutExecutionManager(db, smartRestTimer) }
    
    val workoutFlow by workoutManager.workoutFlow.collectAsState()
    val currentStep by workoutManager.currentStep.collectAsState()
    val isInWorkout by workoutManager.isInWorkout.collectAsState()
    val restTimerState by smartRestTimer.timerState.collectAsState()
    
    // UI state
    var showOverview by remember { mutableStateOf(true) }
    var showFormTipsDialog by remember { mutableStateOf(false) }
    var showVideoDialog by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var workoutSummary by remember { mutableStateOf<WorkoutExecutionManager.WorkoutSummary?>(null) }
    
    // Keep screen on during workout
    DisposableEffect(isInWorkout) {
        val activity = context as? Activity
        if (isInWorkout) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Load plan and exercises
    LaunchedEffect(planId) {
        plan = db.planDao().getPlan(planId)
        plan?.let { p ->
            exercises = parseTrainingContent(p.content)
        }
    }

    // Main UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        plan?.title ?: "Training",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    if (isInWorkout) {
                        IconButton(onClick = { showPauseDialog = true }) {
                            Icon(Icons.Filled.Pause, contentDescription = "Pausieren")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Show workout summary
                workoutSummary != null -> {
                    WorkoutSummaryScreen(
                        summary = workoutSummary!!,
                        onDismiss = {
                            workoutSummary = null
                            onTrainingCompleted()
                        }
                    )
                }
                
                // Show training overview
                showOverview && !isInWorkout -> {
                    TrainingOverviewScreen(
                        exercises = exercises,
                        onStartTraining = {
                            scope.launch {
                                workoutManager.startWorkoutFlow(planId, exercises)
                                showOverview = false
                            }
                        }
                    )
                }
                
                // Show workout execution
                isInWorkout && currentStep != null -> {
                    WorkoutExecutionScreen(
                        workoutStep = currentStep!!,
                        workoutFlow = workoutFlow!!,
                        restTimerState = restTimerState,
                        onSetComplete = { weight, reps, rpe ->
                            scope.launch {
                                workoutManager.logSet(weight.toDouble(), reps, rpe)
                                
                                // Start rest timer if needed
                                if (currentStep!!.restTime > 0) {
                                    workoutManager.startRestTimer(currentStep!!.restTime)
                                }
                            }
                        },
                        onNextStep = {
                            scope.launch {
                                val nextStep = workoutManager.navigateToNextStep()
                                if (nextStep == null) {
                                    // Workout completed
                                    val summary = workoutManager.finishWorkout()
                                    workoutSummary = summary
                                }
                            }
                        },
                        onPreviousStep = {
                            // Implementation for going back would be here
                        },
                        onPauseWorkout = {
                            showPauseDialog = true
                        },
                        onFinishWorkout = {
                            showFinishDialog = true
                        },
                        onShowVideo = {
                            showVideoDialog = true
                        },
                        onShowFormTips = {
                            showFormTipsDialog = true
                        },
                        onSkipRest = {
                            workoutManager.skipRestPeriod()
                        }
                    )
                }
                
                // Loading state
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // Dialogs
    if (showFormTipsDialog && currentStep != null) {
        FormTipsDialog(
            formTips = currentStep!!.formTips,
            exerciseName = currentStep!!.exercise.name,
            onDismiss = { showFormTipsDialog = false }
        )
    }

    if (showVideoDialog && currentStep != null) {
        VideoDialog(
            exerciseName = currentStep!!.exercise.name,
            videoReference = currentStep!!.videoReference,
            onDismiss = { showVideoDialog = false }
        )
    }

    if (showPauseDialog) {
        PauseWorkoutDialog(
            onResume = { showPauseDialog = false },
            onEndWorkout = {
                scope.launch {
                    val summary = workoutManager.finishWorkout()
                    workoutSummary = summary
                    showPauseDialog = false
                }
            },
            onDismiss = { showPauseDialog = false }
        )
    }

    if (showFinishDialog) {
        FinishWorkoutDialog(
            onConfirm = {
                scope.launch {
                    val summary = workoutManager.finishWorkout()
                    workoutSummary = summary
                    showFinishDialog = false
                }
            },
            onDismiss = { showFinishDialog = false }
        )
    }
}

@Composable
private fun TrainingOverviewScreen(
    exercises: List<ExerciseStep>,
    onStartTraining: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Bereit für dein Training?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${exercises.size} Übungen warten auf dich",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
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
        }

        item {
            Text(
                "Übungen im Training:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(exercises.withIndex().toList()) { (index, exercise) ->
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            exercise.value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (exercise.restTime > 0) {
                        Text(
                            "${exercise.restTime}s Pause",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutExecutionScreen(
    workoutStep: WorkoutExecutionManager.WorkoutStep,
    workoutFlow: WorkoutExecutionManager.WorkoutExecutionFlow,
    restTimerState: RestTimerState,
    onSetComplete: (Float, Int, Int) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onPauseWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onShowVideo: () -> Unit,
    onShowFormTips: () -> Unit,
    onSkipRest: () -> Unit
) {
    val currentSet = workoutStep.sets[workoutStep.currentSet]
    val isLastSet = workoutStep.currentSet >= workoutStep.sets.size - 1
    val isLastExercise = workoutFlow.currentExerciseIndex >= workoutFlow.exercises.size - 1
    
    var weight by remember(currentSet) { mutableFloatStateOf(currentSet.actualWeight ?: currentSet.targetWeight ?: workoutStep.autoWeightSuggestion ?: 20f) }
    var reps by remember(currentSet) { mutableIntStateOf(currentSet.actualReps ?: currentSet.targetReps ?: 10) }
    var rpe by remember(currentSet) { mutableIntStateOf(currentSet.rpe ?: 5) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WorkoutProgressBar(
                currentStep = workoutFlow.currentExerciseIndex + 1,
                totalSteps = workoutFlow.exercises.size
            )
        }

        item {
            ExerciseCard(
                workoutStep = workoutStep,
                onShowVideo = onShowVideo,
                onShowFormTips = onShowFormTips
            )
        }

        // Show rest timer if active
        when (restTimerState) {
            is RestTimerState.RUNNING -> {
                item {
                    val nextExercise = if (isLastSet && !isLastExercise) {
                        workoutFlow.exercises[workoutFlow.currentExerciseIndex + 1].name
                    } else {
                        "Nächster Satz"
                    }
                    
                    RestTimerOverlay(
                        timeRemaining = restTimerState.remaining,
                        nextExercise = nextExercise,
                        onSkipRest = onSkipRest
                    )
                }
            }
            is RestTimerState.COMPLETED -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Pause beendet! 🔥",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onNextStep) {
                                Text("Weiter zum nächsten Satz")
                            }
                        }
                    }
                }
            }
            else -> {
                // Show set input section when not resting
                item {
                    SetInputSection(
                        currentSet = currentSet,
                        autoWeightSuggestion = workoutStep.autoWeightSuggestion,
                        onWeightChange = { weight = it },
                        onRepsChange = { reps = it },
                        onRPEChange = { rpe = it },
                        onSetComplete = {
                            onSetComplete(weight, reps, rpe)
                        }
                    )
                }
            }
        }

        item {
            WorkoutNavigationBar(
                canGoBack = workoutFlow.currentExerciseIndex > 0,
                canGoNext = true,
                isLastExercise = isLastExercise && isLastSet,
                onPrevious = onPreviousStep,
                onPause = onPauseWorkout,
                onNext = onNextStep,
                onFinish = onFinishWorkout
            )
        }
    }
}

@Composable
private fun WorkoutSummaryScreen(
    summary: WorkoutExecutionManager.WorkoutSummary,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Training abgeschlossen! 🎉",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryMetric(
                        label = "Dauer",
                        value = "${summary.duration / 60} Min"
                    )
                    SummaryMetric(
                        label = "Volumen",
                        value = "${summary.totalVolume.toInt()} kg"
                    )
                    SummaryMetric(
                        label = "Übungen",
                        value = "${summary.exercisesCompleted}"
                    )
                }
                
                if (summary.personalRecords > 0) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            "🏆 ${summary.personalRecords} neuer Rekord!",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fertig")
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

// Dialog Components
@Composable
private fun FormTipsDialog(
    formTips: List<String>,
    exerciseName: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "Technik-Tipps: $exerciseName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                
                formTips.forEach { tip ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            tip,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verstanden")
                }
            }
        }
    }
}

@Composable
private fun VideoDialog(
    exerciseName: String,
    videoReference: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Video: $exerciseName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                
                // Placeholder for video player
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.PlayCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Video Player")
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Schließen")
                }
            }
        }
    }
}

@Composable
private fun PauseWorkoutDialog(
    onResume: () -> Unit,
    onEndWorkout: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.Pause, contentDescription = null)
        },
        title = {
            Text("Training pausiert")
        },
        text = {
            Text("Möchtest du das Training fortsetzen oder beenden?")
        },
        confirmButton = {
            Button(onClick = onResume) {
                Text("Fortsetzen")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onEndWorkout) {
                Text("Beenden")
            }
        }
    )
}

@Composable
private fun FinishWorkoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.Check, contentDescription = null)
        },
        title = {
            Text("Training beenden?")
        },
        text = {
            Text("Möchtest du das Training jetzt beenden? Dein Fortschritt wird gespeichert.")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Beenden")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

// Helper function to parse training content - simplified version
private fun parseTrainingContent(content: String): List<ExerciseStep> {
    return content.split("|").mapNotNull { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
            val parts = trimmed.split(" - ", limit = 2)
            if (parts.size >= 2) {
                val name = parts[0].trim()
                val description = parts[1].trim()
                
                // Extract rest time if specified
                val restTimeMatch = Regex("Pause: (\\d+)").find(description)
                val restTime = restTimeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 60
                
                // Determine exercise type and value
                val (type, value) = when {
                    description.contains("mal", ignoreCase = true) || 
                    description.contains("reps", ignoreCase = true) ||
                    description.contains("x", ignoreCase = true) -> {
                        val repsMatch = Regex("(\\d+)\\s*(?:mal|reps|x)").find(description)
                        "reps" to (repsMatch?.groupValues?.get(1) ?: "10")
                    }
                    description.contains("min", ignoreCase = true) ||
                    description.contains("sek", ignoreCase = true) -> {
                        "time" to description
                    }
                    else -> "reps" to "10"
                }
                
                ExerciseStep(
                    name = name,
                    type = type,
                    value = value,
                    description = description,
                    restTime = restTime
                )
            } else null
        } else null
    }
}