package com.example.fitapp.feature.tracking.navigation

/**
 * Tracking feature navigation routes
 */
sealed class TrackingRoute(val route: String) {
    object TrainingHub : TrackingRoute("training_hub")
    object TodayTraining : TrackingRoute("today_training")
    object WorkoutPlanner : TrackingRoute("workout_planner")
    object HIITBuilder : TrackingRoute("hiit_builder")
    object HIITExecution : TrackingRoute("hiit_execution")
    object WorkoutExecution : TrackingRoute("workout_execution/{workoutId}") {
        fun createRoute(workoutId: Long) = "workout_execution/$workoutId"
    }
    object TrainingHistory : TrackingRoute("training_history")
    object AITrainer : TrackingRoute("ai_trainer")
    
    companion object {
        const val TRACKING_GRAPH_ROUTE = "tracking_graph"
        
        /**
         * All tracking routes for navigation validation
         */
        val allRoutes = listOf(
            TrainingHub.route,
            TodayTraining.route,
            WorkoutPlanner.route,
            HIITBuilder.route,
            HIITExecution.route,
            WorkoutExecution.route,
            TrainingHistory.route,
            AITrainer.route
        )
    }
}