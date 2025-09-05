package com.example.fitapp.ui.screens.enhanced

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
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.*
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.PlanEntity
import com.example.fitapp.data.db.WorkoutPerformanceEntity
import com.example.fitapp.data.db.WorkoutSessionEntity
import com.example.fitapp.network.healthconnect.HealthConnectManager
import com.example.fitapp.services.WorkoutSensorService
import com.example.fitapp.services.VideoManager
import com.example.fitapp.services.SmartRestTimer
import com.example.fitapp.services.VoiceCommandManager
import com.example.fitapp.services.VoiceCommand
import com.example.fitapp.services.VideoResource
import com.example.fitapp.services.RestTimerState
import com.example.fitapp.services.SetData
import com.example.fitapp.ui.components.advanced.*
import com.example.fitapp.ui.components.adaptive.*
import com.example.fitapp.ui.screens.ExerciseStep
// Remove parseTrainingContent import as it's private
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Enhanced Training Execution Screen - Phase 1 Integration
 * Demonstrates the integration of advanced workout tracking features
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
    
    // Basic state
    var plan by remember { mutableStateOf<PlanEntity?>(null) }
    var exercises by remember { mutableStateOf<List<ExerciseStep>>(emptyList()) }
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var isInTraining by remember { mutableStateOf(false) }
    var completedExercises by remember { mutableStateOf(setOf<Int>()) }
    var showTrainingOverview by remember { mutableStateOf(true) }
    
    // Advanced state using our new state management
    var advancedState by remember { mutableStateOf(AdvancedTrainingUiState()) }
    var aiCoachingExpanded by remember { mutableStateOf(false) }
    
    // Services
    val healthConnectManager = remember { HealthConnectManager(context) }
    val sensorService = remember { WorkoutSensorService(context, healthConnectManager) }
    
    // New Pro-Feature Services
    val videoManager = remember { VideoManager(context) }
    val smartRestTimer = remember { SmartRestTimer(context) }
    val voiceCommandManager = remember { VoiceCommandManager(context) }
    
    // Freeletics-style Adaptive Training Engine
    val adaptiveTrainer = remember { FreeleticsStyleAdaptiveTrainer(context) }
    
    // Pro-feature states
    var currentVideo by remember { mutableStateOf<VideoResource?>(null) }
    var isVideoPlaying by remember { mutableStateOf(false) }
    var showVideoOverlays by remember { mutableStateOf(true) }
    var currentVideoAngle by remember { mutableIntStateOf(0) }
    
    // Voice command state
    var isVoiceCommandActive by remember { mutableStateOf(false) }
    val voiceCommandState by voiceCommandManager.isListening.collectAsState()
    val lastVoiceCommand by voiceCommandManager.lastCommand.collectAsState()
    
    // Rest timer state
    val restTimerState by smartRestTimer.timerState.collectAsState()
    val restSuggestions by smartRestTimer.restSuggestions.collectAsState()
    
    // Initialize voice commands
    LaunchedEffect(Unit) {
        voiceCommandManager.initialize()
    }
    
    // Handle voice commands
    LaunchedEffect(lastVoiceCommand) {
        lastVoiceCommand?.let { command ->
            when (command) {
                VoiceCommand.EXERCISE_COMPLETE -> {
                    // Complete current exercise
                    completedExercises = completedExercises + currentExerciseIndex
                    if (currentExerciseIndex < exercises.size - 1) {
                        currentExerciseIndex++
                    }
                }
                VoiceCommand.NEXT_EXERCISE -> {
                    if (currentExerciseIndex < exercises.size - 1) {
                        currentExerciseIndex++
                    }
                }
                VoiceCommand.PAUSE_WORKOUT -> {
                    isInTraining = false
                    smartRestTimer.pauseTimer()
                }
                VoiceCommand.RESUME_WORKOUT -> {
                    isInTraining = true
                    smartRestTimer.resumeTimer()
                }
                VoiceCommand.START_REST_TIMER -> {
                    if (exercises.isNotEmpty()) {
                        smartRestTimer.startAdaptiveRest(
                            exerciseId = exercises[currentExerciseIndex].name,
                            intensity = 0.7f, // Default intensity
                            heartRate = advancedState.currentHeartRate,
                            perceivedExertion = advancedState.currentRPE
                        )
                    }
                }
                VoiceCommand.SKIP_REST -> {
                    smartRestTimer.skipRest()
                }
                is VoiceCommand.SET_WEIGHT -> {
                    // Handle weight setting via voice
                    // This would integrate with existing set logging
                }
                is VoiceCommand.SET_REPS -> {
                    // Handle rep setting via voice
                    // This would integrate with existing set logging  
                }
                else -> {
                    // Handle other commands
                }
            }
        }
    }
    
    // Load plan and initialize
    LaunchedEffect(planId) {
        plan = db.planDao().getPlan(planId)
        plan?.let { p ->
            exercises = parseTrainingContentLocal(p.content)
            advancedState = advancedState.copy(
                plan = p,
                exercises = exercises
            )
        }
    }
    
    // Start workout session when training begins
    LaunchedEffect(isInTraining) {
        if (isInTraining && advancedState.sessionId == null) {
            val sessionId = UUID.randomUUID().toString()
            val session = WorkoutSessionEntity(
                id = sessionId,
                planId = planId,
                userId = "current_user", // In real app, get from user context
                startTime = System.currentTimeMillis() / 1000
            )
            
            try {
                db.workoutSessionDao().insert(session)
                advancedState = advancedState.copy(
                    sessionId = sessionId,
                    isInTraining = true
                )
                
                // Start sensor services
                if (exercises.isNotEmpty()) {
                    sensorService.startMovementTracking(exercises[currentExerciseIndex].name)
                    
                    scope.launch {
                        sensorService.startHeartRateMonitoring().collect { heartReading ->
                            advancedState = advancedState.copy(
                                currentHeartRate = heartReading.bpm,
                                heartRateZone = heartReading.zone,
                                heartRateMonitorConnected = true
                            )
                        }
                    }
                    
                    scope.launch {
                        sensorService.repCountFlow.collect { repCount ->
                            advancedState = advancedState.copy(repCount = repCount)
                        }
                    }
                    
                    scope.launch {
                        sensorService.formQualityFlow.collect { quality ->
                            advancedState = advancedState.copy(formQuality = quality)
                        }
                    }
                }
            } catch (e: Exception) {
                advancedState = advancedState.copy(
                    error = "Fehler beim Starten der Session: ${e.message}"
                )
            }
        }
    }
    
    // Load exercise video when current exercise changes
    LaunchedEffect(currentExerciseIndex, exercises) {
        if (exercises.isNotEmpty() && currentExerciseIndex < exercises.size) {
            val currentExercise = exercises[currentExerciseIndex]
            try {
                val video = videoManager.adaptiveQualityStreaming(currentExercise.name)
                currentVideo = video
                
                // Preload next exercise video
                if (currentExerciseIndex + 1 < exercises.size) {
                    val nextExercise = exercises[currentExerciseIndex + 1]
                    videoManager.preloadWorkoutVideos(listOf(nextExercise.name))
                }
            } catch (e: Exception) {
                // Handle video loading errors gracefully
                currentVideo = null
            }
        }
    }
    
    // Generate AI coaching tips periodically
    LaunchedEffect(isInTraining, currentExerciseIndex) {
        if (isInTraining && exercises.isNotEmpty()) {
            delay(10000) // Wait 10 seconds before generating tips
            
            try {
                // Simulate AI coaching tips generation
                val sampleTips = listOf(
                    CoachingTip(
                        type = CoachingTipType.FORM_IMPROVEMENT,
                        message = "Achte auf eine langsame, kontrollierte Bewegung",
                        priority = Priority.MEDIUM
                    ),
                    CoachingTip(
                        type = CoachingTipType.MOTIVATION,
                        message = "Du machst das großartig! Bleib fokussiert.",
                        priority = Priority.LOW
                    )
                )
                
                advancedState = advancedState.copy(aiCoachingTips = sampleTips)
            } catch (e: Exception) {
                // Handle AI service errors gracefully
            }
        }
    }
    
    // Generate progression suggestions when exercise is completed
    val generateProgressionSuggestion: (Int) -> Unit = { exerciseIndex ->
        scope.launch {
            try {
                val exercise = exercises[exerciseIndex]
                val suggestion = ProgressionSuggestion(
                    exerciseId = exercise.name,
                    exerciseName = exercise.name,
                    currentWeight = 10f,
                    recommendedWeight = 12.5f,
                    currentReps = 10,
                    recommendedReps = 12,
                    reason = "Basierend auf deiner Form-Qualität von ${(advancedState.formQuality * 100).toInt()}% empfehle ich eine Steigerung.",
                    confidence = 0.85f,
                    alternatives = listOf(
                        ProgressionAlternative(
                            type = "weight_increase",
                            description = "Gewicht um 2.5kg erhöhen",
                            weight = 12.5f,
                            reps = 10,
                            difficulty = "harder"
                        )
                    )
                )
                
                advancedState = advancedState.copy(
                    progressionSuggestions = listOf(suggestion)
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Freeletics-style Real-time Adaptive Training
    LaunchedEffect(isInTraining, currentExerciseIndex, advancedState.currentHeartRate, advancedState.formQuality) {
        if (isInTraining && exercises.isNotEmpty() && currentExerciseIndex < exercises.size && advancedState.isAdaptiveTrainingEnabled) {
            
            // Create real-time performance snapshot
            val realTimePerformance = RealTimePerformance(
                formQuality = advancedState.formQuality,
                rpe = advancedState.currentRPE ?: 6,
                heartRate = advancedState.currentHeartRate,
                currentRep = advancedState.repCount,
                movementSpeed = 1.0f // Default speed, would be calculated from sensor data
            )
            
            // Update state with current performance
            advancedState = advancedState.copy(realTimePerformance = realTimePerformance)
            
            try {
                val currentExercise = exercises[currentExerciseIndex]
                val sessionContext = WorkoutSessionContext(
                    sessionId = advancedState.sessionId ?: "unknown",
                    sessionDuration = 30L, // Would calculate actual duration
                    totalExercises = exercises.size,
                    completedExercises = completedExercises.size,
                    overallIntensity = 0.7f,
                    userEnergyLevel = 1.0f - (advancedState.currentRPE ?: 5) / 10f
                )
                
                // Real-time workout adaptation
                val adaptation = adaptiveTrainer.adaptWorkoutRealTime(
                    currentExercise = currentExercise,
                    currentPerformance = realTimePerformance,
                    sessionContext = sessionContext
                )
                
                // Apply adaptation if significant
                if (adaptation.type != AdaptationType.NO_CHANGE) {
                    advancedState = advancedState.copy(
                        currentWorkoutAdaptation = adaptation,
                        adaptationHistory = advancedState.adaptationHistory + adaptation
                    )
                }
                
                // Real-time difficulty adjustment
                val performanceIndicators = PerformanceIndicators(
                    formQuality = advancedState.formQuality,
                    fatigueLevel = (advancedState.currentRPE ?: 5) / 10f,
                    heartRateZone = advancedState.heartRateZone,
                    movementConsistency = 0.8f // Would calculate from sensor data
                )
                
                val difficultyAdjustment = adaptiveTrainer.adjustDifficultyRealTime(
                    currentExercise = currentExercise,
                    performanceIndicators = performanceIndicators
                )
                
                // Apply difficulty adjustment if significant
                if (kotlin.math.abs(difficultyAdjustment.adjustmentFactor - 1.0f) > 0.05f) {
                    advancedState = advancedState.copy(difficultyAdjustment = difficultyAdjustment)
                }
                
                // Check for exercise substitution needs
                val performanceIssues = mutableListOf<PerformanceIssue>()
                
                if (advancedState.formQuality < 0.5f) {
                    performanceIssues.add(PerformanceIssue(
                        type = IssueType.FORM_DEGRADATION,
                        severity = IssueSeverity.HIGH,
                        description = "Signifikante Verschlechterung der Bewegungsqualität",
                        detectionConfidence = 0.9f
                    ))
                }
                
                if ((advancedState.currentRPE ?: 5) > 8) {
                    performanceIssues.add(PerformanceIssue(
                        type = IssueType.EXCESSIVE_FATIGUE,
                        severity = IssueSeverity.MEDIUM,
                        description = "Hohe wahrgenommene Anstrengung",
                        detectionConfidence = 0.8f
                    ))
                }
                
                if (performanceIssues.isNotEmpty()) {
                    val substitution = adaptiveTrainer.suggestExerciseSubstitution(
                        currentExercise = currentExercise,
                        performanceIssues = performanceIssues,
                        availableEquipment = listOf("Körpergewicht", "Hanteln") // Would get from user profile
                    )
                    
                    substitution?.let {
                        advancedState = advancedState.copy(exerciseSubstitution = it)
                    }
                }
                
                // Calculate adaptive rest time for next exercise
                if (currentExerciseIndex > 0) {
                    val lastSetPerformance = SetPerformance(
                        formQuality = advancedState.formQuality,
                        perceivedExertion = advancedState.currentRPE ?: 6,
                        actualReps = advancedState.repCount,
                        targetReps = 10, // Would extract from exercise
                        weight = 10f // Would get from last set
                    )
                    
                    val adaptiveRest = adaptiveTrainer.calculateAdaptiveRestTime(
                        lastSetPerformance = lastSetPerformance,
                        currentFatigueLevel = (advancedState.currentRPE ?: 5) / 10f,
                        targetIntensity = 0.7f,
                        exerciseType = currentExercise.type
                    )
                    
                    advancedState = advancedState.copy(adaptiveRestCalculation = adaptiveRest)
                }
                
            } catch (e: Exception) {
                // Handle adaptive training errors gracefully
                advancedState = advancedState.copy(
                    error = "Adaptive Training Fehler: ${e.message}"
                )
            }
            
            // Update frequency for real-time adaptation
            delay(2000) // Check every 2 seconds during training
        }
    }
    
    // Adaptive coaching feedback flow
    LaunchedEffect(isInTraining) {
        if (isInTraining && exercises.isNotEmpty() && advancedState.isAdaptiveTrainingEnabled) {
            val exerciseId = exercises.getOrNull(currentExerciseIndex)?.name ?: return@LaunchedEffect
            
            // Create a flow of real-time performance updates
            val realTimeFlow = kotlinx.coroutines.flow.flow {
                while (isInTraining) {
                    advancedState.realTimePerformance?.let { emit(it) }
                    delay(1000) // Emit every second
                }
            }
            
            // Collect adaptive coaching feedback
            adaptiveTrainer.adaptiveCoachingFlow(exerciseId, realTimeFlow).collect { feedback ->
                advancedState = advancedState.copy(adaptiveCoachingFeedback = feedback)
            }
        }
    }
    
    // UI
    Column(Modifier.fillMaxSize()) {
        // Enhanced Top App Bar
        TopAppBar(
            title = { 
                Column {
                    Text("Advanced Training", style = MaterialTheme.typography.titleMedium)
                    plan?.let { Text(it.title, style = MaterialTheme.typography.bodyMedium) }
                    if (isInTraining) {
                        Text(
                            "Session aktiv",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                Row {
                    // Sensor status indicators
                    if (advancedState.heartRateMonitorConnected) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "HR Connected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (advancedState.movementTrackingActive) {
                        Icon(
                            Icons.Filled.FitnessCenter,
                            contentDescription = "Sensors Active",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )
        
        if (showTrainingOverview) {
            // Training Overview with advanced preview
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "🚀 Advanced Training Ready",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Dieses Training nutzt KI-gestützte Form-Analyse, Herzfrequenz-Monitoring und intelligente Progression.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                FeatureIndicator("❤️", "HR Monitor")
                                FeatureIndicator("📊", "Form Analysis")
                                FeatureIndicator("🤖", "AI Coaching")
                                FeatureIndicator("📈", "Smart Progress")
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    showTrainingOverview = false
                                    isInTraining = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Advanced Training starten")
                            }
                        }
                    }
                }
                
                items(exercises.mapIndexed { index, exercise -> index to exercise }) { (index, exercise) ->
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Übung ${index + 1}: ${exercise.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                exercise.value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } else if (isInTraining) {
            // Active Training Mode with Advanced Features
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Live Performance Dashboard
                item {
                    LivePerformanceDashboard(
                        heartRate = advancedState.currentHeartRate,
                        heartRateZone = advancedState.heartRateZone,
                        repCount = advancedState.repCount,
                        volume = advancedState.sessionMetrics.totalVolume,
                        efficiency = advancedState.sessionMetrics.workoutEfficiency
                    )
                }
                
                // Exercise Video Player - NEW PRO FEATURE
                if (currentVideo != null && exercises.isNotEmpty() && currentExerciseIndex < exercises.size) {
                    item {
                        ExerciseVideoPlayer(
                            videoResource = currentVideo,
                            exerciseTitle = exercises[currentExerciseIndex].name,
                            isPlaying = isVideoPlaying,
                            showOverlays = showVideoOverlays,
                            currentAngle = currentVideoAngle,
                            availableAngles = listOf("Front", "Side", "Back"),
                            onPlayPause = { isVideoPlaying = !isVideoPlaying },
                            onSpeedChange = { speed ->
                                // Handle speed change for slow-motion
                            },
                            onAngleChange = { angle -> currentVideoAngle = angle },
                            onRepeat = { 
                                isVideoPlaying = false
                                // Reset video to beginning
                                isVideoPlaying = true
                            }
                        )
                    }
                }
                
                // Voice Command Panel - NEW PRO FEATURE
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (voiceCommandState) MaterialTheme.colorScheme.primaryContainer 
                                          else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (voiceCommandState) "🎤 Höre zu..." else "🗣️ Voice Commands",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (voiceCommandState) "Sage: 'Fertig', 'Nächste Übung', 'Timer Start'" 
                                          else "Tap to activate voice control",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                lastVoiceCommand?.let { command ->
                                    Text(
                                        text = "Last: ${command.getDisplayText()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Button(
                                onClick = {
                                    if (voiceCommandState) {
                                        voiceCommandManager.stopListening()
                                    } else {
                                        voiceCommandManager.startListening { command ->
                                            // Commands are handled in LaunchedEffect above
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (voiceCommandState) Icons.Filled.MicOff else Icons.Filled.Mic,
                                    contentDescription = if (voiceCommandState) "Stop listening" else "Start listening"
                                )
                            }
                        }
                    }
                }
                
                // Smart Rest Timer - NEW PRO FEATURE
                if (restTimerState !is RestTimerState.IDLE) {
                    item {
                        SmartRestTimerDisplay(
                            timerState = restTimerState,
                            restSuggestion = restSuggestions,
                            onSkip = { smartRestTimer.skipRest() },
                            onPause = { smartRestTimer.pauseTimer() },
                            onResume = { 
                                scope.launch { 
                                    smartRestTimer.resumeTimer() 
                                }
                            },
                            onStop = { smartRestTimer.stopTimer() }
                        )
                    }
                }
                
                // Freeletics-style Adaptive Training Panel - NEW FEATURE
                if (advancedState.isAdaptiveTrainingEnabled) {
                    item {
                        FreeleticsAdaptiveTrainingPanel(
                            realTimePerformance = advancedState.realTimePerformance,
                            workoutAdaptation = advancedState.currentWorkoutAdaptation,
                            difficultyAdjustment = advancedState.difficultyAdjustment,
                            exerciseSubstitution = advancedState.exerciseSubstitution,
                            adaptiveCoachingFeedback = advancedState.adaptiveCoachingFeedback,
                            adaptiveRestCalculation = advancedState.adaptiveRestCalculation,
                            onAcceptAdaptation = { adaptation ->
                                // Apply the workout adaptation
                                scope.launch {
                                    // Here we would apply the actual changes to the workout
                                    advancedState = advancedState.copy(
                                        currentWorkoutAdaptation = null // Clear after applying
                                    )
                                }
                            },
                            onDeclineAdaptation = {
                                advancedState = advancedState.copy(
                                    currentWorkoutAdaptation = null
                                )
                            },
                            onAcceptSubstitution = { substitution ->
                                // Replace current exercise with substitution
                                scope.launch {
                                    // In a real implementation, this would modify the exercise list
                                    advancedState = advancedState.copy(
                                        exerciseSubstitution = null
                                    )
                                }
                            },
                            onDeclineSubstitution = {
                                advancedState = advancedState.copy(
                                    exerciseSubstitution = null
                                )
                            }
                        )
                    }
                }
                
                // Adaptive Coaching Feedback Card - NEW FEATURE
                advancedState.adaptiveCoachingFeedback?.let { feedback ->
                    item {
                        AdaptiveCoachingFeedbackCard(
                            feedback = feedback,
                            onActionTaken = { action ->
                                // Handle coaching feedback actions
                                scope.launch {
                                    // Apply the suggested action
                                    advancedState = advancedState.copy(
                                        adaptiveCoachingFeedback = null
                                    )
                                }
                            }
                        )
                    }
                }
                
                // Real-time Performance Insights - NEW FEATURE
                advancedState.realTimePerformance?.let { performance ->
                    item {
                        RealTimePerformanceInsights(
                            performance = performance,
                            adaptationHistory = advancedState.adaptationHistory.takeLast(5),
                            sessionProgress = (completedExercises.size.toFloat() / exercises.size) * 100f,
                            onToggleAdaptiveTraining = {
                                advancedState = advancedState.copy(
                                    isAdaptiveTrainingEnabled = !advancedState.isAdaptiveTrainingEnabled
                                )
                            }
                        )
                    }
                }
                
                // Current Exercise with Advanced Display
                if (exercises.isNotEmpty() && currentExerciseIndex < exercises.size) {
                    item {
                        AdvancedExerciseDisplay(
                            exercise = exercises[currentExerciseIndex],
                            performance = null, // Would be loaded from database
                            heartRate = advancedState.currentHeartRate,
                            heartRateZone = advancedState.heartRateZone,
                            formQuality = advancedState.formQuality,
                            coachingTips = advancedState.aiCoachingTips,
                            onRPEChanged = { rpe ->
                                advancedState = advancedState.copy(currentRPE = rpe)
                            },
                            onFormFeedback = {
                                // Show detailed form analysis
                            }
                        )
                    }
                }
                
                // Progression Suggestions
                advancedState.progressionSuggestions.firstOrNull()?.let { suggestion ->
                    item {
                        ProgressionSuggestionCard(
                            suggestion = suggestion,
                            onAccept = {
                                // Apply progression
                                advancedState = advancedState.copy(
                                    progressionSuggestions = emptyList()
                                )
                            },
                            onDecline = {
                                advancedState = advancedState.copy(
                                    progressionSuggestions = emptyList()
                                )
                            },
                            onCustomize = {
                                // Show customization dialog
                            }
                        )
                    }
                }
                
                // AI Coaching Panel
                item {
                    AICoachingPanel(
                        tips = advancedState.aiCoachingTips,
                        isExpanded = aiCoachingExpanded,
                        onToggleExpansion = { aiCoachingExpanded = !aiCoachingExpanded },
                        onTipAction = { tip ->
                            // Handle tip action
                        }
                    )
                }
                
                // Exercise Controls
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Primary exercise controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { 
                                        // Skip exercise
                                        if (currentExerciseIndex < exercises.size - 1) {
                                            currentExerciseIndex++
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Überspringen")
                                }
                                
                                Button(
                                    onClick = {
                                        // Complete exercise and start smart rest timer
                                        completedExercises = completedExercises + currentExerciseIndex
                                        generateProgressionSuggestion(currentExerciseIndex)
                                        
                                        // Start adaptive rest timer with current exercise data
                                        scope.launch {
                                            smartRestTimer.startAdaptiveRest(
                                                exerciseId = exercises[currentExerciseIndex].name,
                                                intensity = advancedState.formQuality,
                                                heartRate = advancedState.currentHeartRate,
                                                perceivedExertion = advancedState.currentRPE,
                                                previousSetData = SetData(
                                                    weight = 20f, // Would come from actual set data
                                                    reps = advancedState.repCount,
                                                    formQuality = advancedState.formQuality,
                                                    rpe = advancedState.currentRPE,
                                                    heartRate = advancedState.currentHeartRate
                                                )
                                            )
                                        }
                                        
                                        if (currentExerciseIndex < exercises.size - 1) {
                                            currentExerciseIndex++
                                        } else {
                                            // Training completed
                                            scope.launch {
                                                advancedState.sessionId?.let { sessionId ->
                                                    val updatedSession = WorkoutSessionEntity(
                                                        id = sessionId,
                                                        planId = planId,
                                                        userId = "current_user",
                                                        startTime = System.currentTimeMillis() / 1000 - 1800, // 30 min ago
                                                        endTime = System.currentTimeMillis() / 1000,
                                                        completionPercentage = 100f
                                                    )
                                                    db.workoutSessionDao().update(updatedSession)
                                                }
                                                sensorService.stopTracking()
                                                voiceCommandManager.cleanup()
                                                onTrainingCompleted()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Fertig")
                                }
                            }
                            
                            // Smart rest timer quick action
                            Button(
                                onClick = {
                                    scope.launch {
                                        smartRestTimer.startAdaptiveRest(
                                            exerciseId = exercises[currentExerciseIndex].name,
                                            intensity = 0.7f, // Default intensity
                                            heartRate = advancedState.currentHeartRate,
                                            perceivedExertion = advancedState.currentRPE
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Filled.Timer, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("🧠 Smart Rest Timer starten")
                            }
                        }
                    }
                }
            }
        }
        
        // Error display
        advancedState.error?.let { error ->
            Snackbar(
                action = {
                    TextButton(onClick = { 
                        advancedState = advancedState.copy(error = null)
                    }) {
                        Text("OK")
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun FeatureIndicator(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Local copy of parseTrainingContent function since the original is private
private fun parseTrainingContentLocal(content: String): List<ExerciseStep> {
    val exercises = mutableListOf<ExerciseStep>()
    val lines = content.lines()
    
    for (line in lines) {
        val trimmed = line.trim()
        when {
            // Match patterns like "3x10 Push-ups" or "10x Squats"
            trimmed.matches(Regex("\\d+x\\d+\\s+.*", RegexOption.IGNORE_CASE)) -> {
                val parts = trimmed.split("\\s+".toRegex(), 2)
                if (parts.size >= 2) {
                    exercises.add(ExerciseStep(
                        name = parts[1],
                        type = "reps",
                        value = parts[0],
                        description = "Führe ${parts[0]} aus",
                        restTime = 30
                    ))
                }
            }
            // Match patterns like "30 Sekunden Plank" or "2 Minuten Laufen"
            trimmed.matches(Regex("\\d+\\s+(Sekunden?|Minuten?)\\s+.*", RegexOption.IGNORE_CASE)) -> {
                val parts = trimmed.split("\\s+".toRegex(), 3)
                if (parts.size >= 3) {
                    exercises.add(ExerciseStep(
                        name = parts.drop(2).joinToString(" "),
                        type = "time",
                        value = "${parts[0]} ${parts[1]}",
                        description = "Halte für ${parts[0]} ${parts[1]}",
                        restTime = 15
                    ))
                }
            }
            // Match patterns like "Laufband: 5 km/h für 10 Min"
            trimmed.contains("km/h", ignoreCase = true) || trimmed.contains("stufe", ignoreCase = true) -> {
                exercises.add(ExerciseStep(
                    name = trimmed.substringBefore(":").trim(),
                    type = "cardio",
                    value = trimmed.substringAfter(":").trim(),
                    description = "Cardio-Training wie angegeben",
                    restTime = 60
                ))
            }
            // General exercise lines
            trimmed.isNotBlank() && !trimmed.startsWith("#") && trimmed.length > 5 -> {
                exercises.add(ExerciseStep(
                    name = trimmed,
                    type = "general",
                    value = "Nach Anweisung",
                    description = "Führe die Übung wie beschrieben aus",
                    restTime = 30
                ))
            }
        }
    }
    
    if (exercises.isEmpty()) {
        exercises.add(ExerciseStep(
            name = "Freies Training",
            type = "general",
            value = "Nach Plan",
            description = "Folge dem Trainingsplan",
            restTime = 0
        ))
    }
    
    return exercises
}