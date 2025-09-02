package com.example.fitapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.services.WorkoutExecutionManager
import com.example.fitapp.ui.screens.ExerciseStep

/**
 * Enhanced Workout Execution UI Components
 * Following the design specifications from the problem statement
 */

@Composable
fun WorkoutProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Schritt $currentStep von $totalSteps",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${((currentStep.toFloat() / totalSteps) * 100).toInt()}% abgeschlossen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ExerciseCard(
    workoutStep: WorkoutExecutionManager.WorkoutStep,
    modifier: Modifier = Modifier,
    onShowVideo: () -> Unit = {},
    onShowFormTips: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workoutStep.exercise.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = workoutStep.exercise.value,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Video preview button
                if (workoutStep.videoReference != null) {
                    IconButton(onClick = onShowVideo) {
                        Icon(
                            Icons.Filled.PlayCircle,
                            contentDescription = "Video anzeigen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Instructions
            if (workoutStep.instructions.isNotEmpty()) {
                Text(
                    text = workoutStep.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Form Tips Section
            if (workoutStep.formTips.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Technik-Hinweise",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = onShowFormTips) {
                        Text("Alle anzeigen")
                    }
                }
                
                workoutStep.formTips.take(2).forEach { tip ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Progression Hint
            workoutStep.progressionHint?.let { hint ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            hint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetInputSection(
    currentSet: WorkoutExecutionManager.WorkoutSet,
    autoWeightSuggestion: Float?,
    onWeightChange: (Float) -> Unit,
    onRepsChange: (Int) -> Unit,
    onRPEChange: (Int) -> Unit,
    onSetComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var weight by remember(currentSet) { mutableFloatStateOf(currentSet.actualWeight ?: currentSet.targetWeight ?: autoWeightSuggestion ?: 20f) }
    var reps by remember(currentSet) { mutableIntStateOf(currentSet.actualReps ?: currentSet.targetReps ?: 10) }
    var rpe by remember(currentSet) { mutableIntStateOf(currentSet.rpe ?: 5) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Satz ${currentSet.setNumber}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Weight Selector
            WeightSelector(
                weight = weight,
                onWeightChange = { 
                    weight = it
                    onWeightChange(it)
                },
                suggestion = autoWeightSuggestion
            )
            
            // Reps Counter
            RepsCounter(
                reps = reps,
                onRepsChange = { 
                    reps = it
                    onRepsChange(it)
                },
                targetReps = currentSet.targetReps
            )
            
            // RPE Selector
            RPESelector(
                rpe = rpe,
                onRPEChange = { 
                    rpe = it
                    onRPEChange(it)
                }
            )
            
            // Set Complete Button
            Button(
                onClick = {
                    onWeightChange(weight)
                    onRepsChange(reps)
                    onRPEChange(rpe)
                    onSetComplete()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Satz abschließen",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun WeightSelector(
    weight: Float,
    onWeightChange: (Float) -> Unit,
    suggestion: Float?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Gewicht",
                style = MaterialTheme.typography.labelLarge
            )
            suggestion?.let {
                Text(
                    "Vorschlag: ${it.toInt()}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Decrease button
            OutlinedButton(
                onClick = { onWeightChange((weight - 2.5f).coerceAtLeast(0f)) },
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Weniger")
            }
            
            // Weight display
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    "${weight.toInt()}kg",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            
            // Increase button
            Button(
                onClick = { onWeightChange(weight + 2.5f) },
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Mehr")
            }
        }
    }
}

@Composable
private fun RepsCounter(
    reps: Int,
    onRepsChange: (Int) -> Unit,
    targetReps: Int?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Wiederholungen",
                style = MaterialTheme.typography.labelLarge
            )
            targetReps?.let {
                Text(
                    "Ziel: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Decrease button
            OutlinedButton(
                onClick = { onRepsChange((reps - 1).coerceAtLeast(0)) },
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Weniger")
            }
            
            // Reps display
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    "$reps",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            
            // Increase button
            Button(
                onClick = { onRepsChange(reps + 1) },
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Mehr")
            }
        }
    }
}

@Composable
private fun RPESelector(
    rpe: Int,
    onRPEChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Anstrengung (RPE 1-10)",
            style = MaterialTheme.typography.labelLarge
        )
        
        Spacer(Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            (1..10).forEach { value ->
                val isSelected = value == rpe
                val color = when {
                    value <= 3 -> Color(0xFF4CAF50) // Green - Easy
                    value <= 6 -> Color(0xFFFF9800) // Orange - Moderate
                    value <= 8 -> Color(0xFFFF5722) // Red-Orange - Hard
                    else -> Color(0xFFF44336) // Red - Very Hard
                }
                
                Button(
                    onClick = { onRPEChange(value) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) color else MaterialTheme.colorScheme.outline,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "$value",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        
        Spacer(Modifier.height(4.dp))
        
        Text(
            when (rpe) {
                in 1..3 -> "Leicht - Könnte noch viele Wiederholungen machen"
                in 4..6 -> "Moderat - Noch 2-4 Wiederholungen möglich"
                in 7..8 -> "Schwer - Noch 1-2 Wiederholungen möglich"
                9 -> "Sehr schwer - Maximal 1 Wiederholung möglich"
                10 -> "Maximum - Keine weitere Wiederholung möglich"
                else -> ""
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RestTimerOverlay(
    timeRemaining: Int,
    nextExercise: String,
    motivationalMessage: String = "Du schaffst das! 💪",
    onSkipRest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "💤 Pause",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Circular Countdown
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                CircularProgressIndicator(
                    progress = { if (timeRemaining > 0) timeRemaining / 90f else 0f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline
                )
                Text(
                    "$timeRemaining",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                "Sekunden",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                motivationalMessage,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                "Nächste Übung:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                nextExercise,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            OutlinedButton(
                onClick = onSkipRest,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.SkipNext, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Pause überspringen")
            }
        }
    }
}

@Composable
fun WorkoutNavigationBar(
    canGoBack: Boolean,
    canGoNext: Boolean,
    isLastExercise: Boolean,
    onPrevious: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Previous button
        OutlinedButton(
            onClick = onPrevious,
            enabled = canGoBack,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Zurück")
        }
        
        // Pause button
        OutlinedButton(
            onClick = onPause,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Filled.Pause, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Pause")
        }
        
        // Next/Finish button
        Button(
            onClick = if (isLastExercise) onFinish else onNext,
            enabled = canGoNext,
            modifier = Modifier.weight(1f)
        ) {
            if (isLastExercise) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Beenden")
            } else {
                Text("Weiter")
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}