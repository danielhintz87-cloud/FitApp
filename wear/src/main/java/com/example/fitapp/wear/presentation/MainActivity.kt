package com.example.fitapp.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.*
import com.example.fitapp.wear.presentation.theme.FitAppWearTheme
import com.example.fitapp.wear.presentation.viewmodel.WearWorkoutViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            FitAppWearTheme {
                FitAppWearNavigation()
            }
        }
    }
}

@Composable
fun FitAppWearNavigation() {
    val navController = rememberSwipeDismissableNavController()
    
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainWearScreen(
                onNavigateToWorkout = { navController.navigate("workout") },
                onNavigateToProgress = { navController.navigate("progress") }
            )
        }
        composable("workout") {
            WorkoutWearScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("progress") {
            ProgressWearScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainWearScreen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToProgress: () -> Unit,
    viewModel: WearWorkoutViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Initialize ViewModel with context
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }
    
    val workoutState by viewModel.workoutState.collectAsState()
    val progressData by viewModel.progressData.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimeText()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ScalingLazyColumn(
            state = rememberScalingLazyListState(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Quick status card
            item {
                Card(
                    onClick = onNavigateToWorkout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (workoutState.isActive) {
                            Text(
                                text = "Active Workout",
                                style = MaterialTheme.typography.title3,
                                color = MaterialTheme.colors.primary
                            )
                            Text(
                                text = workoutState.exerciseName,
                                style = MaterialTheme.typography.body2,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${workoutState.currentSet}/${workoutState.totalSets} sets",
                                style = MaterialTheme.typography.caption1
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = "Start Workout",
                                tint = MaterialTheme.colors.primary
                            )
                            Text(
                                text = "Start Workout",
                                style = MaterialTheme.typography.title3
                            )
                        }
                    }
                }
            }
            
            // Progress summary
            item {
                Card(
                    onClick = onNavigateToProgress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Progress",
                            tint = MaterialTheme.colors.secondary
                        )
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.title3
                        )
                        Text(
                            text = "${progressData.weeklyWorkouts} workouts this week",
                            style = MaterialTheme.typography.caption1,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${progressData.currentStreak} day streak",
                            style = MaterialTheme.typography.caption1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Quick actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.syncWithPhone() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync"
                        )
                    }
                    
                    Button(
                        onClick = { /* Handle heart rate monitoring */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart Rate"
                        )
                    }
                }
            }
        }
    }
}