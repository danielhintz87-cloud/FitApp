package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.domain.entities.*
import com.example.fitapp.services.WorkoutManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HIITBuilderScreen(
    onBackPressed: () -> Unit,
    onWorkoutCreated: (HIITWorkout) -> Unit
) {
    val context = LocalContext.current
    val workoutManager = remember { WorkoutManager(context) }
    
    var availableExercises by remember { mutableStateOf<List<BodyweightExercise>>(emptyList()) }
    var selectedExercises by remember { mutableStateOf<List<BodyweightExercise>>(emptyList()) }
    var workInterval by remember { mutableIntStateOf(30) }
    var restInterval by remember { mutableIntStateOf(30) }
    var rounds by remember { mutableIntStateOf(4) }
    var difficulty by remember { mutableStateOf(HIITDifficulty.BEGINNER) }
    var workoutName by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    
    // Load available exercises
    LaunchedEffect(Unit) {
        availableExercises = workoutManager.getDefaultBodyweightExercises()
    }
    
    // Update intervals based on difficulty
    LaunchedEffect(difficulty) {
        when (difficulty) {
            HIITDifficulty.BEGINNER -> {
                workInterval = 20
                restInterval = 40
            }
            HIITDifficulty.INTERMEDIATE -> {
                workInterval = 30
                restInterval = 30
            }
            HIITDifficulty.ADVANCED -> {
                workInterval = 45
                restInterval = 15
            }
            HIITDifficulty.EXPERT -> {
                workInterval = 60
                restInterval = 10
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("HIIT Builder") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(
                    onClick = { showCreateDialog = true },
                    enabled = selectedExercises.isNotEmpty()
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Workout erstellen")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HIIT Configuration Section
            item {
                HIITConfigurationCard(
                    difficulty = difficulty,
                    onDifficultyChange = { difficulty = it },
                    workInterval = workInterval,
                    onWorkIntervalChange = { workInterval = it },
                    restInterval = restInterval,
                    onRestIntervalChange = { restInterval = it },
                    rounds = rounds,
                    onRoundsChange = { rounds = it }
                )
            }
            
            // Selected Exercises Section
            if (selectedExercises.isNotEmpty()) {
                item {
                    SelectedExercisesCard(
                        selectedExercises = selectedExercises,
                        onRemoveExercise = { exercise ->
                            selectedExercises = selectedExercises - exercise
                        },
                        workInterval = workInterval,
                        restInterval = restInterval,
                        rounds = rounds
                    )
                }
            }
            
            // Available Exercises Section
            item {
                Text(
                    text = "Übungen auswählen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(availableExercises.groupBy { it.category }.toList()) { (category, exercises) ->
                ExerciseCategoryCard(
                    category = category,
                    exercises = exercises,
                    selectedExercises = selectedExercises,
                    onExerciseSelected = { exercise ->
                        if (exercise !in selectedExercises) {
                            selectedExercises = selectedExercises + exercise
                        }
                    }
                )
            }
        }
    }
    
    // Create Workout Dialog
    if (showCreateDialog) {
        CreateWorkoutDialog(
            workoutName = workoutName,
            onWorkoutNameChange = { workoutName = it },
            onDismiss = { showCreateDialog = false },
            onConfirm = {
                val builder = HIITBuilder(
                    selectedExercises = selectedExercises,
                    workInterval = workInterval,
                    restInterval = restInterval,
                    rounds = rounds,
                    difficulty = difficulty
                )
                val workout = builder.generateWorkout(workoutName.ifEmpty { "Custom HIIT Workout" })
                onWorkoutCreated(workout)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun HIITConfigurationCard(
    difficulty: HIITDifficulty,
    onDifficultyChange: (HIITDifficulty) -> Unit,
    workInterval: Int,
    onWorkIntervalChange: (Int) -> Unit,
    restInterval: Int,
    onRestIntervalChange: (Int) -> Unit,
    rounds: Int,
    onRoundsChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "HIIT Konfiguration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Difficulty Selection
            Text(
                text = "Schwierigkeitsgrad",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HIITDifficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = difficulty == diff,
                        onClick = { onDifficultyChange(diff) },
                        label = { Text(diff.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Intervals Configuration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Arbeitszeit", style = MaterialTheme.typography.bodyMedium)
                    Text("${workInterval}s", style = MaterialTheme.typography.titleLarge)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pausenzeit", style = MaterialTheme.typography.bodyMedium)
                    Text("${restInterval}s", style = MaterialTheme.typography.titleLarge)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Runden", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (rounds > 1) onRoundsChange(rounds - 1) }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Weniger")
                        }
                        Text("$rounds", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { if (rounds < 10) onRoundsChange(rounds + 1) }) {
                            Icon(Icons.Filled.Add, contentDescription = "Mehr")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedExercisesCard(
    selectedExercises: List<BodyweightExercise>,
    onRemoveExercise: (BodyweightExercise) -> Unit,
    workInterval: Int,
    restInterval: Int,
    rounds: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ausgewählte Übungen (${selectedExercises.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val totalTime = (selectedExercises.size * workInterval + selectedExercises.size * restInterval) * rounds / 60
                Text(
                    text = "~${totalTime}min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            selectedExercises.forEachIndexed { index, exercise ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${index + 1}. ${exercise.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = exercise.category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = { onRemoveExercise(exercise) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Entfernen")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCategoryCard(
    category: BodyweightCategory,
    exercises: List<BodyweightExercise>,
    selectedExercises: List<BodyweightExercise>,
    onExerciseSelected: (BodyweightExercise) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = getCategoryDisplayName(category),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            exercises.forEach { exercise ->
                val isSelected = exercise in selectedExercises
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = { if (!isSelected) onExerciseSelected(exercise) }
                        )
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = exercise.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    if (isSelected) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Ausgewählt",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Hinzufügen",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateWorkoutDialog(
    workoutName: String,
    onWorkoutNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workout erstellen") },
        text = {
            Column {
                Text("Gib deinem HIIT Workout einen Namen:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = workoutName,
                    onValueChange = onWorkoutNameChange,
                    label = { Text("Workout Name") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Erstellen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

private fun getCategoryDisplayName(category: BodyweightCategory): String {
    return when (category) {
        BodyweightCategory.PUSH -> "Drück-Übungen"
        BodyweightCategory.PULL -> "Zug-Übungen"
        BodyweightCategory.SQUAT -> "Bein-Übungen"
        BodyweightCategory.CORE -> "Core-Übungen"
        BodyweightCategory.CARDIO -> "Cardio-Übungen"
        BodyweightCategory.FULL_BODY -> "Ganzkörper-Übungen"
    }
}