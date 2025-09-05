package com.example.fitapp.wear.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.example.fitapp.wear.presentation.viewmodel.WearWorkoutViewModel

@Composable
fun WorkoutWearScreen(
    onNavigateBack: () -> Unit,
    viewModel: WearWorkoutViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Initialize ViewModel with context
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }
    
    val workoutState by viewModel.workoutState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        if (workoutState.isActive) {
            ActiveWorkoutContent(
                workoutState = workoutState,
                onPauseWorkout = { viewModel.pauseWorkout() },
                onCompleteSet = { viewModel.completeCurrentSet() },
                onSkipRest = { viewModel.skipRest() },
                onCompleteWorkout = { viewModel.completeWorkout() }
            )
        } else {
            NoActiveWorkoutContent(
                onNavigateBack = onNavigateBack,
                onStartWorkout = { viewModel.requestWorkoutStart() }
            )
        }
    }
}

@Composable
fun ActiveWorkoutContent(
    workoutState: com.example.fitapp.shared.WearWorkoutState,
    onPauseWorkout: () -> Unit,
    onCompleteSet: () -> Unit,
    onSkipRest: () -> Unit,
    onCompleteWorkout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimeText()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ScalingLazyColumn(
            state = rememberScalingLazyListState(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Exercise name and progress
            item {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = workoutState.exerciseName,
                            style = MaterialTheme.typography.title3,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Set ${workoutState.currentSet} of ${workoutState.totalSets}",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.primary
                        )
                        
                        if (workoutState.targetReps > 0) {
                            Text(
                                text = "${workoutState.currentReps}/${workoutState.targetReps} reps",
                                style = MaterialTheme.typography.caption1
                            )
                        }
                    }
                }
            }
            
            // Rest timer or workout stats
            if (workoutState.isResting) {
                item {
                    RestTimerCard(
                        restTimeRemaining = workoutState.restTimeRemaining,
                        onSkipRest = onSkipRest
                    )
                }
            } else {
                item {
                    WorkoutStatsCard(
                        heartRate = workoutState.heartRate,
                        caloriesBurned = workoutState.caloriesBurned,
                        elapsedTime = workoutState.elapsedTime
                    )
                }
            }
            
            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!workoutState.isResting) {
                        Button(
                            onClick = onCompleteSet,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.primaryButtonColors()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Complete Set"
                            )
                        }
                    }
                    
                    Button(
                        onClick = onPauseWorkout,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.secondaryButtonColors()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause"
                        )
                    }
                }
            }
            
            // Complete workout button
            item {
                Button(
                    onClick = onCompleteWorkout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.primaryButtonColors(
                        backgroundColor = Color.Green.copy(alpha = 0.8f)
                    )
                ) {
                    Text("Complete Workout")
                }
            }
        }
    }
}

@Composable
fun RestTimerCard(
    restTimeRemaining: Int,
    onSkipRest: () -> Unit
) {
    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rest Time",
                style = MaterialTheme.typography.caption1,
                color = MaterialTheme.colors.primary
            )
            
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${restTimeRemaining}s",
                    style = MaterialTheme.typography.title2,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            CompactButton(
                onClick = onSkipRest
            ) {
                Text("Skip")
            }
        }
    }
}

@Composable
fun WorkoutStatsCard(
    heartRate: Int,
    caloriesBurned: Int,
    elapsedTime: Long
) {
    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WorkoutStatItem(
                icon = Icons.Default.Favorite,
                value = if (heartRate > 0) "$heartRate" else "--",
                label = "BPM",
                color = Color.Red
            )
            
            WorkoutStatItem(
                icon = Icons.Default.LocalFireDepartment,
                value = "$caloriesBurned",
                label = "Cal",
                color = Color.Red
            )
            
            WorkoutStatItem(
                icon = Icons.Default.Schedule,
                value = formatTime(elapsedTime),
                label = "Time",
                color = MaterialTheme.colors.primary
            )
        }
    }
}

@Composable
fun WorkoutStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption2
        )
    }
}

@Composable
fun NoActiveWorkoutContent(
    onNavigateBack: () -> Unit,
    onStartWorkout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = "No Workout",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "No Active Workout",
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start a workout on your phone to track it here",
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Request Workout")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        CompactButton(
            onClick = onNavigateBack
        ) {
            Text("Back")
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}