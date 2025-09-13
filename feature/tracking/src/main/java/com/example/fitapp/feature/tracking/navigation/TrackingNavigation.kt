package com.example.fitapp.feature.tracking.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/**
 * Tracking feature navigation graph
 */
fun NavGraphBuilder.trackingGraph(navController: NavController) {
    navigation(
        startDestination = TrackingRoute.TrainingHub.route,
        route = TrackingRoute.TRACKING_GRAPH_ROUTE
    ) {
        composable(TrackingRoute.TrainingHub.route) {
            TrainingHubPlaceholder()
        }
        
        composable(TrackingRoute.TodayTraining.route) {
            TodayTrainingPlaceholder()
        }
        
        composable(TrackingRoute.WorkoutPlanner.route) {
            WorkoutPlannerPlaceholder()
        }
        
        composable(TrackingRoute.HIITBuilder.route) {
            HIITBuilderPlaceholder()
        }
        
        composable(TrackingRoute.HIITExecution.route) {
            HIITExecutionPlaceholder()
        }
        
        composable(TrackingRoute.WorkoutExecution.route) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId")?.toLongOrNull() ?: 0L
            WorkoutExecutionPlaceholder(workoutId)
        }
        
        composable(TrackingRoute.TrainingHistory.route) {
            TrainingHistoryPlaceholder()
        }
        
        composable(TrackingRoute.AITrainer.route) {
            AITrainerPlaceholder()
        }
    }
}

// Placeholder composables - will be replaced with actual screens during migration
@Composable
private fun TrainingHubPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Training Hub - Coming Soon")
    }
}

@Composable
private fun TodayTrainingPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Today Training - Coming Soon")
    }
}

@Composable
private fun WorkoutPlannerPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Workout Planner - Coming Soon")
    }
}

@Composable
private fun HIITBuilderPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("HIIT Builder - Coming Soon")
    }
}

@Composable
private fun HIITExecutionPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("HIIT Execution - Coming Soon")
    }
}

@Composable
private fun WorkoutExecutionPlaceholder(workoutId: Long) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Workout Execution (ID: $workoutId) - Coming Soon")
    }
}

@Composable
private fun TrainingHistoryPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Training History - Coming Soon")
    }
}

@Composable
private fun AITrainerPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("AI Trainer - Coming Soon")
    }
}