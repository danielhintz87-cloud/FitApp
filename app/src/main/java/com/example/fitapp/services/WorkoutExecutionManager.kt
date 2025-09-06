package com.example.fitapp.services

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.fitapp.data.db.*
import com.example.fitapp.ui.screens.ExerciseStep
import com.example.fitapp.util.StructuredLogger
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/**
 * Comprehensive Workout Execution Manager
 * Handles workout flow, set tracking, rest periods, and performance analytics
 */
class WorkoutExecutionManager(
    private val database: AppDatabase,
    private val smartRestTimer: SmartRestTimer
) {
    companion object {
        private const val TAG = "WorkoutExecutionManager"
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var currentSetStartTime: Long? = null
    private var lastSetEndTime: Long? = null
    
    // Flow states for reactive UI
    private val _workoutFlow = MutableStateFlow<WorkoutExecutionFlow?>(null)
    val workoutFlow: StateFlow<WorkoutExecutionFlow?> = _workoutFlow.asStateFlow()
    
    private val _currentStep = MutableStateFlow<WorkoutStep?>(null)
    val currentStep: StateFlow<WorkoutStep?> = _currentStep.asStateFlow()
    
    private val _isInWorkout = MutableStateFlow(false)
    val isInWorkout: StateFlow<Boolean> = _isInWorkout.asStateFlow()

    data class WorkoutStep(
        val exercise: ExerciseStep,
        val sets: List<WorkoutSet>,
        val currentSet: Int,
        val restTime: Int,
        val instructions: String,
        val videoReference: String? = null,
        val formTips: List<String> = emptyList(),
        val progressionHint: String? = null,
        val autoWeightSuggestion: Float? = null
    )

    data class WorkoutSet(
        val setNumber: Int,
        val targetWeight: Float? = null,
        val targetReps: Int? = null,
        val actualWeight: Float? = null,
        val actualReps: Int? = null,
        val rpe: Int? = null, // Rate of Perceived Exertion 1-10
        val restTime: Int? = null,
        val isCompleted: Boolean = false,
        val formQuality: Float? = null // 0.0-1.0 from sensor analysis
    )

    data class WorkoutExecutionFlow(
        val sessionId: String,
        val planId: Long,
        val exercises: List<ExerciseStep>,
        val currentExerciseIndex: Int = 0,
        val startTime: Long,
        val completedExercises: Set<Int> = emptySet(),
        val workoutSteps: List<WorkoutStep> = emptyList(),
        val totalVolume: Float = 0f,
        val personalRecords: Int = 0
    )

    data class WorkoutSummary(
        val sessionId: String,
        val duration: Long,
        val totalVolume: Float,
        val exercisesCompleted: Int,
        val personalRecords: Int,
        val averageRPE: Float,
        val caloriesBurned: Int,
        val sessionRating: Int? = null
    )

    /**
     * Start a new workout flow with comprehensive tracking
     */
    suspend fun startWorkoutFlow(
        planId: Long,
        exercises: List<ExerciseStep>
    ): WorkoutExecutionFlow {
        val sessionId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis() / 1000
        
        // Create workout session in database
        val session = WorkoutSessionEntity(
            id = sessionId,
            planId = planId,
            userId = getCurrentUserId(),
            startTime = startTime
        )
        
        database.workoutSessionDao().insert(session)
        
        // Initialize workout steps with suggestions
        val workoutSteps = exercises.mapIndexed { index, exercise ->
            createWorkoutStep(exercise, index, sessionId)
        }
        
        val flow = WorkoutExecutionFlow(
            sessionId = sessionId,
            planId = planId,
            exercises = exercises,
            workoutSteps = workoutSteps,
            startTime = startTime
        )
        
        _workoutFlow.value = flow
        _isInWorkout.value = true
        _currentStep.value = workoutSteps.firstOrNull()
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.USER_ACTION,
            TAG,
            "Started workout flow for plan $planId with ${exercises.size} exercises"
        )
        
        return flow
    }

    /**
     * Navigate to the next step in the workout
     */
    suspend fun navigateToNextStep(): WorkoutStep? {
        val currentFlow = _workoutFlow.value ?: return null
        val currentStepValue = _currentStep.value ?: return null
        
        // Check if current set is completed, if not complete current set
        val currentStep = currentStepValue.copy(
            sets = currentStepValue.sets.mapIndexed { index, set ->
                if (index == currentStepValue.currentSet && !set.isCompleted) {
                    set.copy(isCompleted = true)
                } else set
            }
        )
        
        // Move to next set or next exercise
        val nextStep = if (currentStep.currentSet < currentStep.sets.size - 1) {
            // Next set in same exercise
            currentStep.copy(currentSet = currentStep.currentSet + 1)
        } else {
            // Next exercise
            val nextExerciseIndex = currentFlow.currentExerciseIndex + 1
            if (nextExerciseIndex < currentFlow.exercises.size) {
                val nextExercise = currentFlow.exercises[nextExerciseIndex]
                withContext(Dispatchers.IO) {
                    createWorkoutStep(nextExercise, nextExerciseIndex, currentFlow.sessionId)
                }
            } else {
                null // Workout completed
            }
        }
        
        if (nextStep != null) {
            _currentStep.value = nextStep
            _workoutFlow.value = currentFlow.copy(
                currentExerciseIndex = if (nextStep.exercise != currentStep.exercise) {
                    currentFlow.currentExerciseIndex + 1
                } else {
                    currentFlow.currentExerciseIndex
                }
            )
        }
        
        return nextStep
    }

    /**
     * Log a completed set with performance data
     */
    suspend fun logSet(
        weight: Double,
        reps: Int,
        rpe: Int,
        formQuality: Float? = null,
        notes: String? = null
    ) {
        val currentFlow = _workoutFlow.value ?: return
        val currentStepValue = _currentStep.value ?: return
        
        // Update current set
        val updatedSets = currentStepValue.sets.mapIndexed { index, set ->
            if (index == currentStepValue.currentSet) {
                set.copy(
                    actualWeight = weight.toFloat(),
                    actualReps = reps,
                    rpe = rpe,
                    formQuality = formQuality,
                    isCompleted = true
                )
            } else set
        }
        
        val updatedStep = currentStepValue.copy(sets = updatedSets)
        _currentStep.value = updatedStep
    val now = System.currentTimeMillis() / 1000
    val actualSetDuration = currentSetStartTime?.let { now - it } ?: 60L
    lastSetEndTime = now
        
        // Save performance to database
        val performance = WorkoutPerformanceEntity(
            exerciseId = currentStepValue.exercise.name,
            sessionId = currentFlow.sessionId,
            planId = currentFlow.planId,
            exerciseIndex = currentFlow.currentExerciseIndex,
            reps = reps,
            weight = weight.toFloat(),
            volume = weight.toFloat() * reps,
            restTime = currentStepValue.restTime.toLong(),
            actualRestTime = calculateActualRest(),
            formQuality = formQuality ?: 1.0f,
            perceivedExertion = rpe,
            duration = actualSetDuration,
            notes = notes
        )
        
        database.workoutPerformanceDao().insert(performance)
        
        // Check for personal records
        checkPersonalRecord(currentStepValue.exercise.name, weight.toFloat(), reps)
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.USER_ACTION,
            TAG,
            "Logged set: ${currentStepValue.exercise.name} - ${weight}kg x $reps reps, RPE: $rpe"
        )
    }

    /**
     * Start rest timer with adaptive suggestions
     */
    suspend fun startRestTimer(duration: Int) {
        val currentStepValue = _currentStep.value ?: return
        val currentSet = currentStepValue.sets.getOrNull(currentStepValue.currentSet)

    // Mark set end and compute rest start
    lastSetEndTime = System.currentTimeMillis() / 1000
        
        // Calculate adaptive rest based on performance
        val adaptiveRest = calculateAdaptiveRest(
            baseRest = duration,
            rpe = currentSet?.rpe,
            formQuality = currentSet?.formQuality
        )
        
        smartRestTimer.startAdaptiveRest(
            exerciseId = currentStepValue.exercise.name,
            intensity = (currentSet?.rpe ?: 5) / 10f,
            perceivedExertion = currentSet?.rpe
        )
    }

    private fun calculateActualRest(): Long {
        val now = System.currentTimeMillis() / 1000
        val lastEnd = lastSetEndTime ?: return 0
        return now - lastEnd
    }

    private fun getCurrentUserId(): String = "current_user" // Platzhalter für UserSessionProvider

    /**
     * Skip current rest period
     */
    fun skipRestPeriod() {
        smartRestTimer.skipRest()
    }

    /**
     * Finish the current workout and generate summary
     */
    suspend fun finishWorkout(): WorkoutSummary {
        val currentFlow = _workoutFlow.value ?: throw IllegalStateException("No active workout")
        val endTime = System.currentTimeMillis() / 1000
        
        // Update session in database
        val session = database.workoutSessionDao().getById(currentFlow.sessionId)
        session?.let { s ->
            val updatedSession = s.copy(
                endTime = endTime,
                completionPercentage = 100f,
                totalVolume = calculateTotalVolume(currentFlow),
                personalRecordsAchieved = currentFlow.personalRecords
            )
            database.workoutSessionDao().update(updatedSession)
        }
        
        // Calculate summary
        val summary = WorkoutSummary(
            sessionId = currentFlow.sessionId,
            duration = endTime - currentFlow.startTime,
            totalVolume = calculateTotalVolume(currentFlow),
            exercisesCompleted = currentFlow.completedExercises.size,
            personalRecords = currentFlow.personalRecords,
            averageRPE = calculateAverageRPE(currentFlow),
            caloriesBurned = estimateCaloriesBurned(currentFlow)
        )
        
        // Reset state
        _workoutFlow.value = null
        _currentStep.value = null
        _isInWorkout.value = false
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.USER_ACTION,
            TAG,
            "Finished workout ${currentFlow.sessionId}: ${summary.exercisesCompleted} exercises, ${summary.totalVolume}kg volume"
        )
        
        return summary
    }

    // Private helper methods
    
    private suspend fun createWorkoutStep(
        exercise: ExerciseStep,
        exerciseIndex: Int,
        sessionId: String
    ): WorkoutStep {
        // Get previous performance for suggestions
        val previousPerformances = database.workoutPerformanceDao()
            .getRecentByExerciseId(exercise.name, 5)
        
        // Create sets based on exercise type
        val sets = when (exercise.type) {
            "reps" -> {
                val targetReps = exercise.value.split(" ").firstOrNull()?.toIntOrNull() ?: 10
                val suggestedWeight = suggestWeight(previousPerformances)
                List(3) { setIndex ->
                    WorkoutSet(
                        setNumber = setIndex + 1,
                        targetReps = targetReps,
                        targetWeight = suggestedWeight
                    )
                }
            }
            "time" -> {
                List(1) { setIndex ->
                    WorkoutSet(setNumber = setIndex + 1)
                }
            }
            else -> {
                List(3) { setIndex ->
                    WorkoutSet(setNumber = setIndex + 1)
                }
            }
        }
        
        return WorkoutStep(
            exercise = exercise,
            sets = sets,
            currentSet = 0,
            restTime = exercise.restTime,
            instructions = exercise.description,
            formTips = getFormTips(exercise.name),
            progressionHint = generateProgressionHint(previousPerformances),
            autoWeightSuggestion = suggestWeight(previousPerformances)
        )
    }

    private fun calculateAdaptiveRest(
        baseRest: Int,
        rpe: Int?,
        formQuality: Float?
    ): Int {
        var adaptedRest = baseRest
        
        // Increase rest if RPE is high
        rpe?.let { rpeValue ->
            when {
                rpeValue >= 9 -> adaptedRest = (adaptedRest * 1.5).toInt()
                rpeValue >= 7 -> adaptedRest = (adaptedRest * 1.2).toInt()
                rpeValue <= 3 -> adaptedRest = (adaptedRest * 0.8).toInt()
            }
        }
        
        // Increase rest if form quality is poor
        formQuality?.let { quality ->
            if (quality < 0.7f) {
                adaptedRest = (adaptedRest * 1.3).toInt()
            }
        }
        
        return max(30, min(300, adaptedRest)) // Clamp between 30s and 5min
    }

    private suspend fun checkPersonalRecord(
        exerciseName: String,
        weight: Float,
        reps: Int
    ) {
        val volume = weight * reps
        val currentRecord = database.personalRecordDao()
            .getRecord(exerciseName, "volume")
        
        if (currentRecord == null || volume > currentRecord.value) {
            val record = PersonalRecordEntity(
                exerciseName = exerciseName,
                recordType = "volume",
                value = volume.toDouble(),
                unit = "kg",
                previousRecord = currentRecord?.value
            )
            database.personalRecordDao().insert(record)
            
            // Update workout flow with PR
            _workoutFlow.value?.let { flow ->
                _workoutFlow.value = flow.copy(
                    personalRecords = flow.personalRecords + 1
                )
            }
        }
    }

    private fun calculateTotalVolume(flow: WorkoutExecutionFlow): Float {
        return flow.workoutSteps.sumOf { step ->
            step.sets.sumOf { set ->
                ((set.actualWeight ?: 0f) * (set.actualReps ?: 0)).toDouble()
            }
        }.toFloat()
    }

    private fun calculateAverageRPE(flow: WorkoutExecutionFlow): Float {
        val allRPEs = flow.workoutSteps.flatMap { step ->
            step.sets.mapNotNull { it.rpe }
        }
        return if (allRPEs.isNotEmpty()) {
            allRPEs.average().toFloat()
        } else 0f
    }

    private fun estimateCaloriesBurned(flow: WorkoutExecutionFlow): Int {
        // Simple estimation based on volume and duration
        val totalVolume = calculateTotalVolume(flow)
        val duration = (System.currentTimeMillis() / 1000) - flow.startTime
        return ((totalVolume * 0.5) + (duration * 0.1)).toInt()
    }

    private fun suggestWeight(performances: List<WorkoutPerformanceEntity>): Float? {
        if (performances.isEmpty()) return null
        
        val lastPerformance = performances.first()
        val avgWeight = performances.take(3).map { it.weight }.average().toFloat()
        
        // Suggest slight increase if recent performance was good
        return if (lastPerformance.perceivedExertion != null && lastPerformance.perceivedExertion!! <= 7) {
            avgWeight * 1.025f // 2.5% increase
        } else {
            avgWeight
        }
    }

    private fun generateProgressionHint(performances: List<WorkoutPerformanceEntity>): String? {
        if (performances.isEmpty()) return null
        
        val lastPerformance = performances.first()
        return when {
            lastPerformance.reps > (lastPerformance.weight * 0.8).toInt() -> 
                "Letztes Mal: ${lastPerformance.reps} Reps - versuch ${lastPerformance.reps + 1}!"
            lastPerformance.perceivedExertion != null && lastPerformance.perceivedExertion!! <= 6 ->
                "Form war gut! Zeit für mehr Gewicht?"
            else -> null
        }
    }

    private fun getFormTips(exerciseName: String): List<String> {
        // Basic form tips - could be expanded with AI-generated tips
        return when (exerciseName.lowercase()) {
            "push-ups", "pushups" -> listOf(
                "Körper in gerader Linie halten",
                "Langsam und kontrolliert bewegen",
                "Volle Bewegungsamplitude nutzen"
            )
            "squats", "kniebeugen" -> listOf(
                "Knie nicht über Zehenspitzen",
                "Gewicht auf den Fersen",
                "Aufrechte Körperhaltung"
            )
            else -> listOf(
                "Kontrollierte Bewegung",
                "Volle Bewegungsamplitude",
                "Gleichmäßige Atmung"
            )
        }
    }
}